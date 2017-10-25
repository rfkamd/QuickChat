package co.energenes.quikchat.views.application;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

/**
 * Created by rfkamd on 7/20/2017.
 */

public class QuickChatApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        EmojiManager.install(new GoogleEmojiProvider()); // This line needs to be executed before any usage of EmojiTextView, EmojiEditText or EmojiButton.

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);

        FirebaseApp.initializeApp(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

}
