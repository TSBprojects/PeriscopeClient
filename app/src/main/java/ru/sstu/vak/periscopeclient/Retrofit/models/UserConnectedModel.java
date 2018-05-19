package ru.sstu.vak.periscopeclient.Retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserConnectedModel {
    public UserConnectedModel(String userLogin, String profileImagePath, int userColor, int observersCount, String token) {
        this.userLogin = userLogin;
        this.profileImagePath = profileImagePath;
        this.userColor = userColor;
        this.observersCount = observersCount;
        this.token = token;
    }

    @SerializedName("userLogin")
    @Expose
    private String userLogin;

    @SerializedName("profileImagePath")
    @Expose
    private String profileImagePath;

    @SerializedName("userColor")
    @Expose
    private int userColor;

    @SerializedName("observersCount")
    @Expose
    private int observersCount;

    @SerializedName("token")
    @Expose
    private String token;

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public int getUserColor() {
        return userColor;
    }

    public void setUserColor(int userColor) {
        this.userColor = userColor;
    }

    public int getObserversCount() {
        return observersCount;
    }

    public void setObserversCount(int observersCount) {
        this.observersCount = observersCount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
