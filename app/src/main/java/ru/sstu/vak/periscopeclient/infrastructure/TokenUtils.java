package ru.sstu.vak.periscopeclient.infrastructure;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.sstu.vak.periscopeclient.R;

/**
 * Created by Anton on 30.04.2018.
 */

public class TokenUtils {
    public TokenUtils(Context context) {
        this.context = context;
    }

    private Context context;

    public void setToken(String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.token_name), token);
        editor.apply();
    }

    public String getToken() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.token_name), null);
    }
}
