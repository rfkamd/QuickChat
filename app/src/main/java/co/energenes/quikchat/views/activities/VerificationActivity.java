package co.energenes.quikchat.views.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.models.Countries;
import co.energenes.quikchat.models.Country;
import co.energenes.quikchat.network.ApiResponse;
import co.energenes.quikchat.network.RestApi;
import co.energenes.quikchat.views.adapters.HintSpinnerAdapter;

public class VerificationActivity extends AppCompatActivity {

    private static final String TAG = "VerificationActivity";
    private EditText txtPhone;
    private EditText txtCode;
    private ProgressDialog pgDialog;
    private AppCompatSpinner spCountries;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private static String countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if(!Utils.isNullOrEmpty(Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.USERNAME))){
//            if(RealmHelper.getInstance(getApplicationContext()).getUser() != null){
//                gotoNextActivityAndFinish();
//            }else{
//                gotoNextActivityAndFinish(true);
//            }
//        }

        setContentView(R.layout.activity_verification);

        txtPhone = (EditText) findViewById(R.id.txtPhone);
        txtCode = (EditText) findViewById(R.id.txtCode);
        spCountries = (AppCompatSpinner) findViewById(R.id.spCountries);

        pgDialog = new ProgressDialog(VerificationActivity.this);
        pgDialog.setCancelable(false);
        pgDialog.setMessage(getString(R.string.text_wait));

        final View v1 = findViewById(R.id.layout_phone);
        final View v2 = findViewById(R.id.layout_verify);

        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if(pgDialog.isShowing()){
                    pgDialog.dismiss();
                }
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    txtPhone.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                if(pgDialog.isShowing()){
                    pgDialog.dismiss();
                }
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.VISIBLE);
            }
        };

        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getCountries();
    }

    private void gotoNextActivityAndFinish(){
        startActivity(new Intent(VerificationActivity.this, ChatsActivity.class));
        startService(new Intent(VerificationActivity.this, SocketService.class));
        finish();
    }

    private void gotoNextActivityAndFinish(boolean editMode){

        Intent i = new Intent(VerificationActivity.this, ProfileActivity.class);
        i.putExtra("openInEditMode", editMode);
        startActivity(i);
        startService(new Intent(VerificationActivity.this, SocketService.class));
        finish();
    }

    public void sendCode(View v){

        String phone = countryCode;
        if(txtPhone.getText().toString().startsWith("0")){
            phone+=txtPhone.getText().toString().substring(1);
        }else{
            phone+=txtPhone.getText().toString();
        }
        if (!validatePhoneNumber(phone)) {
            return;
        }
        startPhoneNumberVerification(phone);
        pgDialog.show();
    }

    public void verifyCode(View v){
        String code = txtCode.getText().toString();
        if (TextUtils.isEmpty(code)) {
            txtCode.setError("Cannot be empty.");
            return;
        }
        pgDialog.show();
        verifyPhoneNumberWithCode(mVerificationId, code);
    }

    public void resendCode(View view){
        pgDialog.show();
        resendVerificationCode(txtPhone.getText().toString(), mResendToken);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(pgDialog.isShowing()){
                            pgDialog.dismiss();
                        }

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            Prefs.getInstance(getApplicationContext()).saveKey(Prefs.USERNAME, user.getPhoneNumber());
//                            Prefs.getInstance(getApplicationContext()).saveKey(Prefs.USERNAME, user.getPhoneNumber());
                            gotoNextActivityAndFinish(true);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                txtCode.setError("Invalid code.");
                            }
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private boolean validatePhoneNumber(String phoneNumber) {
//        String phoneNumber = txtPhone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            txtPhone.setError("Invalid phone number.");
            return false;
        }
        if(!Utils.validatePhoneNumber(phoneNumber)){
            txtPhone.setError("Invalid phone number.");
            return false;
        }
        return true;
    }

    private void getCountries(){

        final ProgressDialog pgDialog = new ProgressDialog(VerificationActivity.this);
        pgDialog.setMessage(getString(R.string.text_wait));
        pgDialog.show();

        RestApi.getInstance().getCountries(new ApiResponse() {
            @Override
            public void onResponse(boolean isSuccess, Object responseObject) {
                if(pgDialog.isShowing()){
                    pgDialog.dismiss();
                }
                if(isSuccess){
                    final List<Country> countries = ((Countries)responseObject).getCountries();
//                    ArrayAdapter<Country> adapter = ;
                    final HintSpinnerAdapter adapter = new HintSpinnerAdapter(new ArrayAdapter<>(VerificationActivity.this,
                            android.R.layout.simple_list_item_1, android.R.id.text1, countries), R.layout.layout_hint_spinner, VerificationActivity.this);
                    spCountries.setAdapter(adapter);
                    spCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if(adapter.isEnabled(position)){
                                Country country =(Country) adapter.getItem(position);
                                countryCode = "+"+country.getDialCode();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
            }

            @Override
            public void onFailure(Throwable throwable, String message) {
                if(pgDialog.isShowing()){
                    pgDialog.dismiss();
                }
                Toast.makeText(VerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                throwable.printStackTrace();
            }
        });
    }

}
