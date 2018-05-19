package ru.sstu.vak.periscopeclient.Retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageModel {
    public MessageModel(String message, UserModel sender, String authToken) {
        this.authToken = authToken;
        this.user = sender;
        this.message = message;
    }

    @SerializedName("authToken")
    @Expose
    private String authToken;

    @SerializedName("user")
    @Expose
    private UserModel user;

    @SerializedName("message")
    @Expose
    private String message;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
