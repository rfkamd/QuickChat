package co.energenes.quikchat.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rfkamd on 7/20/2017.
 */

public class Prefs {

    public static String USERNAME = "username";
    public static String NUMBER = "number";
    public static String OTHER = "other";
    public static String ID = "id";
    public static String PUBLIC_KEY = "public_key";
    public static String PRIVATE_KEY = "private_key";
    public static String SHOW_NOTIFICATION = "show_notification";


    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static Prefs p;

    public static Prefs getInstance(Context context){
        if(p ==null)
            p = new Prefs(context);

        return p;
    }

    private Prefs(Context context){
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }


    public void saveKey(String key, String val){
        editor = prefs.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public void saveKey(String key, int val){
        editor = prefs.edit();
        editor.putInt(key, val);
        editor.apply();
    }

    public void saveKey(String key, boolean val){
        editor = prefs.edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    public String getStringValue(String key){
        return prefs.getString(key, null);
    }

    public int getIntValue(String key){
        return prefs.getInt(key, -1);
    }

    public boolean getBooleanValue(String key){
        return prefs.getBoolean(key, false);
    }


}

