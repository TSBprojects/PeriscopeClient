package ru.sstu.vak.periscopeclient.infrastructure.RecyclerView;

import android.widget.TextView;

import ru.sstu.vak.periscopeclient.liveVideoPlayer.LivePlayer;

public class BroadcastsModel {
    private String observersCount;
    private String description;
    private String userLogin;
    private String streamName;
    private LivePlayer streamPreview;

    public BroadcastsModel(String observersCount, String description, String userLogin, String streamName, LivePlayer streamPreview) {
        this.observersCount = observersCount;
        this.description = description;
        this.userLogin = userLogin;
        this.streamName = streamName;
        this.streamPreview = streamPreview;
    }

    public String getObserversCount() {
        return observersCount;
    }

    public void setObserversCount(String observersCount) {
        this.observersCount = observersCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public LivePlayer getStreamPreview() {
        return streamPreview;
    }

    public void setStreamPreview(LivePlayer streamPreview) {
        this.streamPreview = streamPreview;
    }
}
