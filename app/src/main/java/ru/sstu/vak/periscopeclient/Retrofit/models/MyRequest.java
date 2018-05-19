package ru.sstu.vak.periscopeclient.Retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Anton on 30.04.2018.
 */

public class MyRequest<T> {
    public MyRequest(T data, String authToken) {
        this.data = data;
        this.authToken = authToken;
    }

    @SerializedName("data")
    @Expose
    private T data;

    @SerializedName("authToken")
    @Expose
    private String authToken;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}