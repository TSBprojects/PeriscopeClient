package ru.sstu.vak.periscopeclient.Retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Anton on 30.04.2018.
 */

public class MyResponse<T> {
    @SerializedName("error")
    @Expose
    private String error;

    @SerializedName("data")
    @Expose
    private T data;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
