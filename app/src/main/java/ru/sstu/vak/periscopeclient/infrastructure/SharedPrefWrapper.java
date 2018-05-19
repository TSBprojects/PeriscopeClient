package ru.sstu.vak.periscopeclient.infrastructure;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Anton on 06.05.2018.
 */

public class SharedPrefWrapper {
    public SharedPrefWrapper(Context context) {
        this.context = context;
    }

    private Context context;

    public void setString(String name, String string) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, string);
        editor.apply();
    }

    public String getString(String name) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(name, null);
    }
}
