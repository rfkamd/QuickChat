package co.energenes.quikchat.views.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Config;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.data.RealmHelper;
import co.energenes.quikchat.models.User;
import co.energenes.quikchat.models.UserResponse;
import co.energenes.quikchat.network.ApiResponse;
import co.energenes.quikchat.network.RestApi;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ProfileActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;
    private ArrayList<String> contacts;
    private List<Uri> uriList = new ArrayList<>();
    private Uri mPhotoUri;
    private boolean storageGranted;

    private CircleImageView imgProfile;
    private AppCompatButton btnRegisterUser;
    private EditText txtName;
    private EditText txtStatus;
    private EditText txtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.text_profile));
        setSupportActionBar(toolbar);

        txtName = (EditText) findViewById(R.id.txtName);
        txtStatus = (EditText) findViewById(R.id.txtStatus);
        txtPhone = (EditText) findViewById(R.id.txtPhone);
        imgProfile = (CircleImageView) findViewById(R.id.imgProfile);
        btnRegisterUser = (AppCompatButton) findViewById(R.id.btnRegisterUser);


        User user = RealmHelper.getInstance(getApplicationContext()).getUser();
        if (user != null) {
            txtPhone.setText(user.getPhone());
            txtName.setText(user.getName());
            txtStatus.setText(user.getStatus());
            String avatar = Config.SOCKET_ADDRESS + RealmHelper.getInstance(getApplicationContext()).getUser().getAvatar().replace("uploads\\", "uploads/");
            if(!Utils.isNullOrEmpty(avatar)){
                Glide.with(this)
                        .load(avatar)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .bitmapTransform(new CropCircleTransformation(this))
                        .into(imgProfile);
            }
        } else {
            txtPhone.setText(Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.USERNAME));
            txtPhone.setEnabled(false);
        }


        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
            }
        });

        btnRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            storageGranted = true;
        }else{
            startPermissionAskingProcedure();
        }


        if (getIntent().hasExtra("openInEditMode")) {
            if (getIntent().getBooleanExtra("openInEditMode", false)) {
                enableWidgets();

            } else {
                disableWidgets();

            }
        } else {
            disableWidgets();
        }

    }

    private void startPermissionAskingProcedure(){
        if (Utils.shouldAskPermission()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //|| ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        }
    }

    private void enableWidgets() {
        txtName.setEnabled(true);
        txtStatus.setEnabled(true);
        txtPhone.setEnabled(false);
        imgProfile.setEnabled(true);
        btnRegisterUser.setEnabled(true);
        btnRegisterUser.setVisibility(View.VISIBLE);
    }

    private void disableWidgets() {
        txtName.setEnabled(false);
        txtStatus.setEnabled(false);
        txtPhone.setEnabled(false);
        imgProfile.setEnabled(false);
        btnRegisterUser.setEnabled(false);
        btnRegisterUser.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storageGranted = true;
                }
                return;
            }
        }
    }

    private void registerUser() {

        if (Utils.isNullOrEmpty(txtName.getText().toString())) {
            txtName.setError(getString(R.string.error_invalid_name));
        }

        User user = new User();
        user.setVerified(true);
        user.setPhone(Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.USERNAME));
        user.setStatus(txtStatus.getText().toString());
        user.setName(txtName.getText().toString());
        submitUser(user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        processImages(requestCode, resultCode, data);
    }

    private void openImageIntent() {
       if(storageGranted){
           String userId = Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.ID);
           final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "QuikChat" + File.separator + "Profile");
           root.mkdirs();
           long date = Utils.getCurrentDateInMillis();
           final String fname = "image_" + userId + "_" + date + ".jpg";
           final File sdImageMainDirectory = new File(root, fname);
           mPhotoUri = Uri.fromFile(sdImageMainDirectory);
           Intent i = Utils.getImageIntent(getApplicationContext(), mPhotoUri);
           startActivityForResult(i, 1);
       }
    }

    private void processImages(int requestCode, int resultCode, Intent data) {
        final boolean isCamera;
        final String action;
//        Uri selectedImageUri;
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == 1) {

                    if (data == null) {
                        isCamera = true;
                    } else {
                        action = data.getAction();
                        if (action == null) {
                            isCamera = false;
                        } else {
                            isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        }
                    }

                    if (isCamera) {
//                        uriList.add(mPhotoUri);
                    } else {
                        ClipData clip = data.getClipData();
                        if (clip != null) {
                            mPhotoUri = clip.getItemAt(0).getUri();
//                            uriList.add(clip.getItemAt(0).getUri());
                        } else {
                            mPhotoUri = data == null ? null : data.getData();
//                            selectedImageUri = data == null ? null : data.getData();//Extras().get("data");//data.getData();
//                            uriList.add(0, selectedImageUri);
                        }
                    }
//                    populateImages(uriList);
                    try {
                        Glide.with(ProfileActivity.this)
                                .load(mPhotoUri)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imgProfile);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void submitUser(User user) {

        final ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage(getString(R.string.text_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        Map<String, RequestBody> map = new HashMap<String, RequestBody>();

        try {

            File file = new File(Utils.compressImage((Utils.getImagePathFromURI(getApplicationContext(), mPhotoUri))));
            RequestBody image = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            String fileName = "media\"; filename=\"" + file.getName();
            map.put(fileName, image);

            RequestBody nameBody = RequestBody.create(MediaType.parse("multipart/form-data"), user.getName());
            RequestBody phoneBody = RequestBody.create(MediaType.parse("multipart/form-data"), user.getPhone());
            RequestBody statusBody = RequestBody.create(MediaType.parse("multipart/form-data"), user.getStatus());
            RequestBody verifiedBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(user.isVerified()));

            map.put("name", nameBody);
            map.put("phone", phoneBody);
            map.put("status", statusBody);
            map.put("verified", verifiedBody);

            RestApi.getInstance().postUser(map, new ApiResponse() {
                @Override
                public void onResponse(boolean isSuccess, Object responseObject) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (isSuccess) {
                        UserResponse res = (UserResponse) responseObject;
                        if ("success".equals(res.getStatus())) {
                            RealmHelper.getInstance(getApplicationContext()).saveUser(res.getData());
                            startActivity(new Intent(ProfileActivity.this, ChatsActivity.class));

                        } else if (res.getStatus() == "error") {
                            Toast.makeText(ProfileActivity.this, "There is an Error, Try Again", Toast.LENGTH_SHORT).show();
                        }

                    }
                }

                @Override
                public void onFailure(Throwable throwable, String message) {
                    throwable.printStackTrace();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

}
