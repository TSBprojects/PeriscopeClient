/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.sstu.vak.periscopeclient.liveVideoPlayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Random;

import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.R;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.RetrofitWrapper;
import ru.sstu.vak.periscopeclient.Retrofit.models.MessageModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserConnectedModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserModel;
import ru.sstu.vak.periscopeclient.infrastructure.KeyboardUtil;
import ru.sstu.vak.periscopeclient.infrastructure.MessageFactory;
import ru.sstu.vak.periscopeclient.infrastructure.SharedPrefWrapper;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;
import tyrantgit.widget.HeartLayout;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static ru.sstu.vak.periscopeclient.MainActivity.RTMP_BASE_URL;

/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class LiveVideoPlayerActivity extends AppCompatActivity implements OnClickListener, ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {

    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private EventLogger eventLogger;
    private SimpleExoPlayerView simpleExoPlayerView;
    private LinearLayout debugRootView;
    private TextView debugTextView;
    private Button retryButton;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private DebugTextViewHelper debugViewHelper;
    private boolean needRetrySource;

    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;
    private RtmpDataSource.RtmpDataSourceFactory rtmpDataSourceFactory;
    protected String userAgent;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private RetrofitWrapper retrofitWrapper;

    private static final String TAG = "LiveVideoPlayerActivity";
    public String HOST;
    public String SERVER_PORT;
    private int userColor;
    private int userAlphaColor;
    private StompClient mStompClient;
    private Gson mGson = new GsonBuilder().create();

    private CardView joined_label;
    private LinearLayout messages_layout;
    private ScrollView chat_scroll_view;
    private ProgressBar observers_count_progressbar;
    private TextView observers_count_tv;
    private TextView say_something_tv;
    private LinearLayout text_view_layout;
    private LinearLayout edit_text_layout;
    private ImageView chat_profile_img;
    private EditText chat_edit_text;
    private ImageView hide_chat_edit_text_btn;
    private ScrollView player_view_scroll_view;

    private Point SCREEN_SIZE;
    private TokenUtils tokenUtils;
    private PeriscopeApi periscopeApi;
    private String streamName;
    private LinearLayout heart_add_layout;
    private HeartLayout heartLayout;
    private boolean isLeave;

    private ImageLoader imageLoader;
    private SharedPrefWrapper sharedPrefWrapper;
    private MessageFactory messageFactory;


    private void connectStomp() {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.JWS,
                "ws://" + HOST + ":" + SERVER_PORT + "/chat");

        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.e(TAG, "Stomp connection opened", lifecycleEvent.getException());
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.e(TAG, "Stomp connection closed", lifecycleEvent.getException());
                    }
                });

        mStompClient.topic("/disconnectedUser/" + streamName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    String token = tokenUtils.getToken();
                    UserConnectedModel userConnectedModel = mGson.fromJson(topicMessage.getPayload(), UserConnectedModel.class);
                    observers_count_tv.setText(Integer.toString(userConnectedModel.getObserversCount()));
                    messageFactory.addJoinedLabelToScreen(userConnectedModel.getUserLogin() + " вышел(-ла)", userConnectedModel.getUserColor());
                });

        mStompClient.topic("/userConnected/" + streamName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    String token = tokenUtils.getToken();
                    UserConnectedModel userConnectedModel = mGson.fromJson(topicMessage.getPayload(), UserConnectedModel.class);

                    String profileImgPath = sharedPrefWrapper.getString("profileImgPath");
                    if (profileImgPath == null) {
                        setProfileImg(userConnectedModel.getProfileImagePath());
                    } else {
                        imageLoader.displayImage(sharedPrefWrapper.getString("profileImgPath"), chat_profile_img);
                    }

                    if (!token.equals(userConnectedModel.getToken())) {
                        observers_count_tv.setText(Integer.toString(userConnectedModel.getObserversCount()));
                        messageFactory.addJoinedLabelToScreen(userConnectedModel.getUserLogin() + " присоединился(-лась)", userConnectedModel.getUserColor());
                    }
                });

        mStompClient.topic("/receiveMessage/" + streamName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    MessageModel messageModel = mGson.fromJson(topicMessage.getPayload(), MessageModel.class);
                    messageFactory.addMessageToScreen(messageModel.getUser().getLogin(),
                            messageModel.getMessage(),
                            messageModel.getUser().getProfileImagePath(),
                            messageModel.getUser().getUserAlphaColor());
                });

        mStompClient.topic("/heartAdded/" + streamName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    int color = mGson.fromJson(topicMessage.getPayload(), Integer.class);
                    heartLayout.addHeart(color);
                });

        mStompClient.connect();
        connectUser();
    }

    private void disconnectUser() {
        String token = tokenUtils.getToken();
        String userConnectedModel = mGson.toJson(new UserConnectedModel(null, null, userColor, -1, token));
        mStompClient.send("/disconnectUser/" + streamName, userConnectedModel)
                .compose(applySchedulers())
                .subscribe(() -> {
                    mStompClient.disconnect();
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                });
    }

    private void connectUser() {
        String token = tokenUtils.getToken();
        String userConnectedModel = mGson.toJson(new UserConnectedModel(null, null, userColor, -1, token));
        mStompClient.send("/connectUser/" + streamName, userConnectedModel)
                .compose(applySchedulers())
                .subscribe(() -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                });
    }

    private void sendMessage(String message) {
        // send message
        String token = tokenUtils.getToken();
        UserModel userModel = new UserModel();
        userModel.setUserAlphaColor(userAlphaColor);
        String messageModel = mGson.toJson(new MessageModel(message, userModel, token));
        mStompClient.send("/sendMessage/" + streamName, messageModel)
                .compose(applySchedulers())
                .subscribe(() -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                });
    }

    private void addHeart() {
        // send message
        String message = mGson.toJson(userColor);
        mStompClient.send("/addHeart/" + streamName, message)
                .compose(applySchedulers())
                .subscribe(() -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                });
    }

    private CompletableTransformer applySchedulers() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    private void openChatEditText(int duration) {
        edit_text_layout.animate()
                .setDuration(0)
                .alpha(1);
        say_something_tv.animate()
                .setDuration(duration)
                .alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        say_something_tv.setVisibility(View.GONE);
                        text_view_layout.setVisibility(View.GONE);
                        edit_text_layout.setVisibility(View.VISIBLE);
                        openKeyBoard(chat_edit_text);
                    }
                });
    }

    private void closeChatTextView(int duration) {
        say_something_tv.animate()
                .setDuration(0)
                .alpha(1).
                setListener(null);
        edit_text_layout.animate()
                .setDuration(duration)
                .alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        say_something_tv.setVisibility(View.VISIBLE);
                        text_view_layout.setVisibility(View.VISIBLE);
                        edit_text_layout.setVisibility(View.GONE);
                        chat_edit_text.setText("");
                    }
                });
    }


    private void joinRoom(String streamName) {
        isLeave = false;
        observers_count_progressbar.setVisibility(View.VISIBLE);
        observers_count_tv.setVisibility(View.GONE);
        retrofitWrapper.joinRoom(streamName, new RetrofitWrapper.Callback<RoomModel>() {
            @Override
            public void onSuccess(RoomModel data) {
                connectStomp();
                String observersCount = Integer.toString(data.getObservers().size());
                observers_count_tv.setText(observersCount);
                observers_count_progressbar.setVisibility(View.GONE);
                observers_count_tv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Throwable t) {
                showMessage(t.getMessage());
            }
        });
    }

    private void leaveRoom(String streamName) {
        isLeave = true;
        retrofitWrapper.leaveRoom(streamName, new RetrofitWrapper.Callback<RoomModel>() {
            @Override
            public void onSuccess(RoomModel data) {
                disconnectUser();
            }

            @Override
            public void onFailure(Throwable t) {
                showMessage(t.getMessage());
            }
        });
    }

    private void refreshObserversCount(String streamName) {
        retrofitWrapper.getRoom(streamName, new RetrofitWrapper.Callback<RoomModel>() {
            @Override
            public void onSuccess(RoomModel room) {
                if (room != null) {
                    String observersCount = Integer.toString(room.getObservers().size());
                    observers_count_tv.setText(observersCount);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showMessage(t.getMessage());
            }
        });
    }


    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%1$s:%2$s", getString(R.string.server_domain_name), getString(R.string.servers_port)))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

    private void initializeImageLoader() {
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder f = new ImageLoaderConfiguration.Builder(this);
        imageLoader.init(f.build());
    }

    private void initializeScreenSize() {
        SCREEN_SIZE = new Point();
        getWindowManager().getDefaultDisplay().getSize(SCREEN_SIZE);
    }


    private void disableScrolling(ScrollView scrollView) {
        scrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });

    }

    private void openKeyBoard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeKeyboard() {
        if (getCurrentFocus() != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private int convertDpToPixel(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, null)
                .show();
    }

    private int randomColor() {
        Random mRandom = new Random();
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
    }

    private void setUserAlphaColor(int color) {
        userAlphaColor = Color.argb(99, Color.red(color), Color.green(color), Color.blue(color));
    }

    private void setProfileImg(final String url) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).build();
        imageLoader.displayImage(url, chat_profile_img, defaultOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                sharedPrefWrapper.setString(getString(R.string.profile_img_path), url);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    private void setActivitiesItems() {
        HOST = getString(R.string.server_domain_name);
        SERVER_PORT = getString(R.string.servers_port);
        streamName = getIntent().getStringExtra("streamName");
        retrofitWrapper = new RetrofitWrapper(this);
        tokenUtils = new TokenUtils(this);
        sharedPrefWrapper = new SharedPrefWrapper(this);

        heart_add_layout = findViewById(R.id.heart_add_layout);
        messages_layout = findViewById(R.id.messages_layout);
        chat_scroll_view = findViewById(R.id.chat_scroll_view);
        disableScrolling(chat_scroll_view);
        text_view_layout = findViewById(R.id.text_view_layout);
        edit_text_layout = findViewById(R.id.edit_text_layout);

        chat_profile_img = findViewById(R.id.chat_profile_img);
        chat_edit_text = findViewById(R.id.chat_edit_text);
        hide_chat_edit_text_btn = findViewById(R.id.hide_chat_edit_text_btn);
        hide_chat_edit_text_btn.setOnClickListener(this);

        observers_count_progressbar = findViewById(R.id.observers_count_progressbar);
        observers_count_tv = findViewById(R.id.observers_count_tv);
        say_something_tv = findViewById(R.id.say_something_tv);
        say_something_tv.setOnClickListener(this);

        player_view_scroll_view = findViewById(R.id.player_view_scroll_view);
        disableScrolling(player_view_scroll_view);

        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            LinearLayout.LayoutParams newLinearLayout_params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            SCREEN_SIZE.y - convertDpToPixel(SCREEN_SIZE.y * 0.122f)
                    );
            simpleExoPlayerView.setLayoutParams(newLinearLayout_params);
        } else {
            LinearLayout.LayoutParams newLinearLayout_params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            SCREEN_SIZE.y
                    );
            simpleExoPlayerView.setLayoutParams(newLinearLayout_params);
        }

        chat_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!chat_edit_text.getText().toString().equals("")) {
                    hide_chat_edit_text_btn.setImageResource(R.drawable.ic_send_white_24dp);
                } else {
                    hide_chat_edit_text_btn.setImageResource(R.drawable.ic_close_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        userColor = randomColor();
        setUserAlphaColor(userColor);
        heartLayout = findViewById(R.id.heart_layout);
        heart_add_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                addHeart();
                return false;
            }
        });

        chat_profile_img.setColorFilter(userAlphaColor);

        KeyboardUtil keyboardUtil = new KeyboardUtil(this, findViewById(android.R.id.content));
        keyboardUtil.enable();

        messageFactory = new MessageFactory(this, messages_layout, SCREEN_SIZE);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // Activity lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        shouldAutoPlay = true;
        clearResumePosition();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        rtmpDataSourceFactory = new RtmpDataSource.RtmpDataSourceFactory();
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        setContentView(R.layout.activity_live_video_player);

        joined_label = findViewById(R.id.joined_label);
        debugRootView = findViewById(R.id.controls_root);
        debugTextView = findViewById(R.id.debug_text_view);
        retryButton = findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);

        simpleExoPlayerView = findViewById(R.id.player_view);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        initializeScreenSize();
        setActivitiesItems();
        initializeServerApi();
        initializeImageLoader();

        play(false);
        joinRoom(streamName);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        setIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        play(true);
        refreshObserversCount(streamName);
    }

    @Override
    protected void onDestroy() {
        leaveRoom(streamName);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            play(false);
        } else {
            showToast(R.string.storage_permission_denied);
            finish();
        }
    }

    // Activity input

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        simpleExoPlayerView.showController();
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchMediaKeyEvent(event);
    }

    // OnClickListener methods

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.retry_button: {
                play(false);
                break;
            }
            case R.id.say_something_tv: {
                openChatEditText(300);
                break;
            }
            case R.id.hide_chat_edit_text_btn: {
                if (!chat_edit_text.getText().toString().equals("")) {
                    sendMessage(chat_edit_text.getText().toString());
                    chat_edit_text.setText("");
                    hide_chat_edit_text_btn.setImageResource(R.drawable.ic_close_24dp);
                } else {
                    closeKeyboard();
                    closeChatTextView(300);
                }
                break;
            }
        }
    }


    // PlaybackControlView.VisibilityListener implementation

    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }

    // Internal methods

    private void initializePlayer(String rtmpUrl) {
        Intent intent = getIntent();
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {

            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenderers()
                            ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                    null, extensionRendererMode);
            //   player = ExoPlayerFactory.newSimpleInstance(this, trackSelector,
            //           new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  500, 1500, 500, 1500),
            //           null, extensionRendererMode);
            player.addListener(this);

            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);

            simpleExoPlayerView.setPlayer(player);
            player.setPlayWhenReady(shouldAutoPlay);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
        }
        if (needNewPlayer || needRetrySource) {
            //  String action = intent.getAction();
            Uri[] uris;
            String[] extensions;

            uris = new Uri[1];
            uris[0] = Uri.parse(rtmpUrl);
            extensions = new String[1];
            extensions[0] = "";
            if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                if (uri.getScheme().equals("rtmp")) {
                    return new ExtractorMediaSource(uri, rtmpDataSourceFactory, new DefaultExtractorsFactoryForFLV(),
                            mainHandler, eventLogger);
                } else {
                    return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                            mainHandler, eventLogger);
                }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    private void releasePlayer() {
        if (player != null) {
            debugViewHelper.stop();
            debugViewHelper = null;
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            //trackSelectionHelper = null;
            eventLogger = null;
        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            //showControls();
            //showControls();
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.user_finished_broadcasting))
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        if (needRetrySource) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing, or...
    }

    @Override
    @SuppressLint("StringFormatInvalid")
    public void onPlayerError(ExoPlaybackException e) {
        //videoStartControlLayout.setVisibility(View.VISIBLE);
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        needRetrySource = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            play(false);
        } else {
            updateResumePosition();
            showControls();
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_video);
            }
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_audio);
            }
        }
    }

    private void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }


    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

    public void play(boolean resume) {
        //String URL = RTMP_BASE_URL + videoNameEditText.getText().toString();
        String URL = RTMP_BASE_URL + streamName + " live=1";
        //String URL = "http://192.168.1.34:5080/vod/streams/test_adaptive.m3u8";
        initializePlayer(URL);
        //videoStartControlLayout.setVisibility(View.GONE);
    }
}
