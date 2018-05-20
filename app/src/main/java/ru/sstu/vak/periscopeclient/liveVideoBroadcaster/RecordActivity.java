package ru.sstu.vak.periscopeclient.liveVideoBroadcaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.util.Hex;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.antmedia.android.broadcaster.ILiveVideoBroadcaster;
import io.antmedia.android.broadcaster.LiveVideoBroadcaster;
import io.antmedia.android.broadcaster.utils.Resolution;
import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.R;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.RetrofitWrapper;
import ru.sstu.vak.periscopeclient.Retrofit.models.LocationModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.MessageModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserConnectedModel;
import ru.sstu.vak.periscopeclient.infrastructure.MessageFactory;
import ru.sstu.vak.periscopeclient.infrastructure.StopStreamDialog;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;
import tyrantgit.widget.HeartLayout;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

import static ru.sstu.vak.periscopeclient.MainActivity.RTMP_BASE_URL;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {


    private ViewGroup mRootView;
    boolean mIsRecording = false;
    private EditText mStreamDescriptionEditText;
    private Timer mTimer;
    private long mElapsedTime;
    public TimerHandler mTimerHandler;
    private ImageButton mSettingsButton;
    private CameraResolutionsFragment mCameraResolutionsDialog;
    private Intent mLiveVideoBroadcasterServiceIntent;
    private TextView mStreamLiveStatus;
    private GLSurfaceView mGLView;
    private ILiveVideoBroadcaster mLiveVideoBroadcaster;
    private Button mBroadcastControlButton;
    private Button mStopBroadcasting;
    private CoordinatorLayout bottom_sheet_layout;
    private LinearLayout llBottomSheet;
    private TextView bottom_sheet_header;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private RetrofitWrapper retrofitWrapper;

    private Point SCREEN_SIZE;

    private TokenUtils tokenUtils;
    private PeriscopeApi periscopeApi;
    private MessageFactory messageFactory;
    private LinearLayout heart_add_layout;
    private HeartLayout heartLayout;

    private final String TAG = "RecordActivity";
    private String HOST;
    private String SERVER_PORT;
    private String streamName;
    private StompClient mStompClient;
    private Gson mGson = new GsonBuilder().create();

    private LinearLayout messages_layout;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView close_record_activity_btn;
    private LinearLayout observers_count_layout;
    private TextView observers_count_tv;

    private boolean finish = false;
    private boolean legalStopRecord = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager locationManager;
    private AlertDialog turnOnGPSDialog;
    private LocationModel currentLocation;


    private OnCompleteListener<Location> locationOnCompleteListener = new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
            Location location = task.getResult();
            if (location != null) {
                updateRoomLocation(new LocationModel(location.getLatitude(), location.getLongitude()));
            }
        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getRepeatLocation();
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (turnOnGPSDialog == null) {
                turnOnGPSDialog = turnOnGPSDialog();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (turnOnGPSDialog != null) {
                turnOnGPSDialog.dismiss();
                turnOnGPSDialog = null;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    private AlertDialog turnOnGPSDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.turn_on_gps_message));
            builder.setNegativeButton(R.string.no, null);
            builder.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            return builder.show();
        } catch (Exception e) {
            return null;
        }
    }

    private void initializeLocationManager() {
        askForPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);
    }

    @SuppressLint("MissingPermission")
    private void getRepeatLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(locationOnCompleteListener);
    }


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
        updateRoomLocation(currentLocation);
    }

    private void updateRoomLocation(LocationModel locationModel) {
        if (mStompClient != null) {
            RoomModel roomModel = new RoomModel("", null, null, streamName, locationModel);
            String messageModel = mGson.toJson(roomModel);
            mStompClient.send("/updateRoomLocation", messageModel)
                    .compose(applySchedulers())
                    .subscribe(() -> {
                        Log.d(TAG, "STOMP echo send successfully");
                    }, throwable -> {
                        Log.e(TAG, "Error send STOMP echo", throwable);
                    });
        }
    }

    private void removeRoomLocation() {
        if (mStompClient != null) {
            RoomModel roomModel = new RoomModel("", null, null, streamName, null);
            String messageModel = mGson.toJson(roomModel);
            mStompClient.send("/updateRoomLocation", messageModel)
                    .compose(applySchedulers())
                    .subscribe(() -> {
                        Log.d(TAG, "STOMP echo send successfully");
                        mStompClient.disconnect();
                    }, throwable -> {
                        Log.e(TAG, "Error send STOMP echo", throwable);
                    });
        }
    }

    private CompletableTransformer applySchedulers() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }



    private void pressStartRecordButton() {
        mBroadcastControlButton.setEnabled(false);
        mBroadcastControlButton.setText(R.string.thread_initialization);
        mBroadcastControlButton.setBackgroundColor(getResources().getColor(R.color.stream_initialization));
    }

    private void streamStarted() {
        mStopBroadcasting.setEnabled(false);
        bottom_sheet_layout.setVisibility(View.VISIBLE);
        observers_count_layout.setVisibility(View.VISIBLE);

        close_record_activity_btn.setVisibility(View.GONE);
        mBroadcastControlButton.setVisibility(View.GONE);
        mBroadcastControlButton.setEnabled(true);


        CountDownTimer timer = new CountDownTimer(2000, 2000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                mStopBroadcasting.setEnabled(true);
            }
        }.start();
        //mBroadcastControlButton.setText(R.string.stop_broadcasting);
        //mBroadcastControlButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    private void pressStopRecordButton() {
        mBroadcastControlButton.setEnabled(false);
        mBroadcastControlButton.setVisibility(View.VISIBLE);

        observers_count_layout.setVisibility(View.GONE);
        bottom_sheet_layout.setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mStopBroadcasting.setEnabled(true);


        mBroadcastControlButton.setText(R.string.start_broadcasting);
        mBroadcastControlButton.setBackgroundColor(getResources().getColor(R.color.start_record));
        CountDownTimer timer = new CountDownTimer(2000, 2000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                mBroadcastControlButton.setEnabled(true);
            }
        }.start();
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(RecordActivity.this)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, null)
                .show();
    }

    private String generateUniqueStreamName() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.digest();
        messageDigest.update(Long.toString(System.currentTimeMillis()).getBytes());
        byte[] digest = messageDigest.digest();
        return Hex.bytesToStringUppercase(digest).toUpperCase();
    }


    private void createRoom(String description, final String streamName, final AsyncTask<String, String, Boolean> recordTask) {
        pressStartRecordButton();
        retrofitWrapper.createRoom(description, streamName, currentLocation, new RetrofitWrapper.Callback() {
            @Override
            public void onSuccess(Object data) {
                connectStomp();
                recordTask.execute(RTMP_BASE_URL + streamName);
            }

            @Override
            public void onFailure(Throwable t) {
                Snackbar.make(mRootView, t.getMessage(), Snackbar.LENGTH_LONG).show();
                pressStopRecordButton();
            }
        });
    }

    private void removeRoom(String streamName) {
        final StopStreamDialog dialog = showLoadDialog();
        retrofitWrapper.removeRoom(streamName, new RetrofitWrapper.Callback() {
            @Override
            public void onSuccess(Object data) {
                removeRoomLocation();
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (finish) {
                    finish();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showMessage(t.getMessage());
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }


    private StopStreamDialog showLoadDialog() {
        if (legalStopRecord) {
            FragmentManager fm = getSupportFragmentManager();
            StopStreamDialog dialog = new StopStreamDialog();
            dialog.setCancelable(false);
            dialog.show(fm, TAG);
            return dialog;
        } else {
            return null;
        }
    }

    private void initializeScreenSize() {
        SCREEN_SIZE = new Point();
        getWindowManager().getDefaultDisplay().getSize(SCREEN_SIZE);
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%1$s:%2$s",HOST,SERVER_PORT))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

    private void stopStreamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.stop_stream_dialog_message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish = false;
                        legalStopRecord = true;
                        toggleBroadcasting(null);
                    }
                });
        builder.show();
    }

    private void setActivitiesItems() {
        HOST = getString(R.string.server_domain_name);
        SERVER_PORT = getString(R.string.servers_port);

        mFusedLocationClient = new FusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        tokenUtils = new TokenUtils(this);
        retrofitWrapper = new RetrofitWrapper(this);

        messages_layout = (LinearLayout) findViewById(R.id.messages_layout);
        messageFactory = new MessageFactory(this, messages_layout, SCREEN_SIZE);

        heart_add_layout = (LinearLayout) findViewById(R.id.heart_add_layout);
        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);

        observers_count_tv = (TextView) findViewById(R.id.observers_count_tv);
        observers_count_layout = (LinearLayout) findViewById(R.id.observers_count_layout);
        bottom_sheet_layout = (CoordinatorLayout) findViewById(R.id.bottom_sheet_layout);
        llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottom_sheet_header = (TextView) findViewById(R.id.bottom_sheet_header);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottom_sheet_header.setVisibility(View.INVISIBLE);
                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    bottom_sheet_header.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(RecordActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    changeCamera(null);
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mIsRecording) {
            stopStreamDialog();

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_record_activity_btn: {
                if (mIsRecording) {
                    stopStreamDialog();
                } else {
                    finish();
                }
                break;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LiveVideoBroadcaster.LocalBinder binder = (LiveVideoBroadcaster.LocalBinder) service;
            if (mLiveVideoBroadcaster == null) {
                mLiveVideoBroadcaster = binder.getService();
                mLiveVideoBroadcaster.init(RecordActivity.this, mGLView);
                mLiveVideoBroadcaster.setAdaptiveStreaming(true);
            }
            mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLiveVideoBroadcaster = null;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide title
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //binding on resume not to having leaked service connection
        mLiveVideoBroadcasterServiceIntent = new Intent(this, LiveVideoBroadcaster.class);
        //this makes service do its job until done
        startService(mLiveVideoBroadcasterServiceIntent);

        setContentView(R.layout.activity_record);

        mTimerHandler = new TimerHandler();
        mStreamDescriptionEditText = (EditText) findViewById(R.id.stream_description_edit_text);

        mRootView = (ViewGroup) findViewById(R.id.root_layout);
        mSettingsButton = (ImageButton) findViewById(R.id.settings_button);
        mStreamLiveStatus = (TextView) findViewById(R.id.stream_live_status);
        close_record_activity_btn = (ImageView) findViewById(R.id.close_record_activity_btn);
        close_record_activity_btn.setOnClickListener(this);

        mBroadcastControlButton = (Button) findViewById(R.id.start_broadcasting);
        mStopBroadcasting = (Button) findViewById(R.id.stop_broadcasting);

        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        if (mGLView != null) {
            mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        initializeScreenSize();
        setActivitiesItems();
        initializeServerApi();
        initializeLocationManager();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    public void changeCamera(View v) {
        if (mLiveVideoBroadcaster != null) {
            mLiveVideoBroadcaster.changeCamera();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //this lets activity bind
        bindService(mLiveVideoBroadcasterServiceIntent, mConnection, 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LiveVideoBroadcaster.PERMISSIONS_REQUEST: {
                if (mLiveVideoBroadcaster.isPermissionGranted()) {
                    mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.RECORD_AUDIO)) {
                        mLiveVideoBroadcaster.requestPermission();
                    } else {
                        new AlertDialog.Builder(RecordActivity.this)
                                .setTitle(R.string.permission)
                                .setMessage(getString(R.string.app_doesnot_work_without_permissions))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                            startActivity(intent);

                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();

                                            //Open the generic Apps page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                            startActivity(intent);

                                        }
                                    }
                                })
                                .show();
                    }
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //hide dialog if visible not to create leaked window exception
        if (mCameraResolutionsDialog != null && mCameraResolutionsDialog.isVisible()) {
            mCameraResolutionsDialog.dismiss();
        }
        mLiveVideoBroadcaster.pause();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLiveVideoBroadcaster.setDisplayOrientation();
        }

    }

    public void showSetResolutionDialog(View v) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragmentDialog = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragmentDialog != null) {

            ft.remove(fragmentDialog);
        }

        ArrayList<Resolution> sizeList = mLiveVideoBroadcaster.getPreviewSizeList();


        if (sizeList != null && sizeList.size() > 0) {
            mCameraResolutionsDialog = new CameraResolutionsFragment();

            mCameraResolutionsDialog.setCameraResolutions(sizeList, mLiveVideoBroadcaster.getPreviewSize());
            mCameraResolutionsDialog.show(ft, "resolutiton_dialog");
        } else {
            Snackbar.make(mRootView, "No resolution available", Snackbar.LENGTH_LONG).show();
        }

    }

    @SuppressLint("MissingPermission")
    public void toggleBroadcasting(View v) {
        if (!mIsRecording) {
            if (mLiveVideoBroadcaster != null) {
                if (!mLiveVideoBroadcaster.isConnected()) {

                    this.streamName = generateUniqueStreamName();
                    //final String streamName = this.streamName;

                    mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location != null) {
                                currentLocation = new LocationModel(location.getLatitude(), location.getLongitude());
                            }
                            AsyncTask<String, String, Boolean> recordTask = new AsyncTask<String, String, Boolean>() {
                                ContentLoadingProgressBar progressBar;

                                @Override
                                protected void onPreExecute() {
                                    progressBar = new ContentLoadingProgressBar(RecordActivity.this);
                                    progressBar.show();
                                }

                                @Override
                                protected Boolean doInBackground(String... url) {
                                    return mLiveVideoBroadcaster.startBroadcasting(url[0]);

                                }

                                @Override
                                protected void onPostExecute(Boolean result) {
                                    progressBar.hide();
                                    mIsRecording = result;

                                    if (mIsRecording) {
                                        mStreamLiveStatus.setVisibility(View.VISIBLE);
                                        //mBroadcastControlButton.setText(R.string.stop_broadcasting);
                                        mSettingsButton.setVisibility(View.GONE);
                                        streamStarted();
                                        startTimer();//start the recording duration
                                        mStreamDescriptionEditText.setVisibility(View.GONE);

                                    } else {
                                        Snackbar.make(mRootView, R.string.stream_not_started, Snackbar.LENGTH_LONG).show();
                                        pressStopRecordButton();
                                        triggerStopRecording();
                                    }
                                }
                            };
                            createRoom(mStreamDescriptionEditText.getText().toString(), streamName, recordTask);
                        }
                    });
                } else {
                    Snackbar.make(mRootView, R.string.streaming_not_finished, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(mRootView, R.string.oopps_shouldnt_happen, Snackbar.LENGTH_LONG).show();
            }
        } else {
            legalStopRecord = true;
            triggerStopRecording();
        }
    }


    public void triggerStopRecording() {
        if (mIsRecording) {
            //mBroadcastControlButton.setText(R.string.start_broadcasting);
            pressStopRecordButton();

            observers_count_tv.setText("0");
            mStreamLiveStatus.setVisibility(View.GONE);
            mStreamLiveStatus.setText(R.string.live_indicator);
            //mSettingsButton.setVisibility(View.VISIBLE);

            mStreamDescriptionEditText.setVisibility(View.VISIBLE);
            close_record_activity_btn.setVisibility(View.VISIBLE);
            stopTimer();
            mLiveVideoBroadcaster.stopBroadcasting();
        }

        removeRoom(streamName);
        mIsRecording = false;
    }

    //This method starts a mTimer and updates the textview to show elapsed time for recording
    public void startTimer() {

        if (mTimer == null) {
            mTimer = new Timer();
        }

        mElapsedTime = 0;
        mTimer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                mElapsedTime += 1; //increase every sec
                mTimerHandler.obtainMessage(TimerHandler.INCREASE_TIMER).sendToTarget();

                if (mLiveVideoBroadcaster == null || !mLiveVideoBroadcaster.isConnected()) {
                    mTimerHandler.obtainMessage(TimerHandler.CONNECTION_LOST).sendToTarget();
                }
            }
        }, 0, 1000);
    }


    public void stopTimer() {
        if (mTimer != null) {
            this.mTimer.cancel();
        }
        this.mTimer = null;
        this.mElapsedTime = 0;
    }

    public void setResolution(Resolution size) {
        mLiveVideoBroadcaster.setResolution(size);
    }

    private class TimerHandler extends Handler {
        static final int CONNECTION_LOST = 2;
        static final int INCREASE_TIMER = 1;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INCREASE_TIMER:
                    mStreamLiveStatus.setText(getString(R.string.live_indicator) + " - " + getDurationString((int) mElapsedTime));
                    break;
                case CONNECTION_LOST:
                    triggerStopRecording();
                    new AlertDialog.Builder(RecordActivity.this)
                            .setMessage(R.string.broadcast_connection_lost)
                            .setPositiveButton(android.R.string.yes, null)
                            .show();

                    break;
            }
        }
    }

    public static String getDurationString(int seconds) {

        if (seconds < 0 || seconds > 2000000)//there is an codec problem and duration is not set correctly,so display meaningfull string
            seconds = 0;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if (hours == 0)
            return twoDigitString(minutes) + " : " + twoDigitString(seconds);
        else
            return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    public static String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
