package ru.sstu.vak.periscopeclient.Retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Anton on 01.05.2018.
 */

public class RoomModel {
    public RoomModel(String roomDescription, UserModel roomOwner, ArrayList<UserModel> observers, String streamName, LocationModel location) {
        this.roomDescription = roomDescription;
        this.roomOwner = roomOwner;
        this.observers = observers;
        this.streamName = streamName;
        this.location = location;
    }

    @SerializedName("roomDescription")
    @Expose
    private String roomDescription;

    @SerializedName("roomOwner")
    @Expose
    private UserModel roomOwner;

    @SerializedName("observers")
    @Expose
    private ArrayList<UserModel> observers;

    @SerializedName("streamName")
    @Expose
    private String streamName;

    @SerializedName("location")
    @Expose
    private LocationModel location;

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public UserModel getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(UserModel roomOwner) {
        this.roomOwner = roomOwner;
    }

    public ArrayList<UserModel> getObservers() {
        return observers;
    }

    public void setObservers(ArrayList<UserModel> observers) {
        this.observers = observers;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public LocationModel getLocation() {
        return location;
    }

    public void setLocation(LocationModel location) {
        this.location = location;
    }
}