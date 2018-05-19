package ru.sstu.vak.periscopeclient.Retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Anton on 30.04.2018.
 */

public class LoginModel {

    public LoginModel(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @SerializedName("login")
    @Expose
    private String login;

    @SerializedName("password")
    @Expose
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        password = password;
    }
}
