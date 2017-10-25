package co.energenes.quikchat.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Utils;
import co.energenes.quikchat.data.Prefs;
import co.energenes.quikchat.data.RealmHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isNullOrEmpty(Prefs.getInstance(getApplicationContext()).getStringValue(Prefs.USERNAME)) &&
                (RealmHelper.getInstance(getApplicationContext()).getUser() != null)) {
            gotoNextActivityAndFinish();
        }else{
            setContentView(R.layout.activity_splash);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashActivity.this, VerificationActivity.class);
                    startActivity(i);
                }
            }, 2500);
        }
    }

    private void gotoNextActivityAndFinish() {
        startActivity(new Intent(SplashActivity.this, ChatsActivity.class));
        startService(new Intent(SplashActivity.this, SocketService.class));
        finish();
    }
}
