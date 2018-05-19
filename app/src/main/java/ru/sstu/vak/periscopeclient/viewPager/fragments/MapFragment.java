package ru.sstu.vak.periscopeclient.viewPager.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.MainActivity;
import ru.sstu.vak.periscopeclient.R;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.models.LocationModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.MessageModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyRequest;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyResponse;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserConnectedModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserModel;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;
import ru.sstu.vak.periscopeclient.liveVideoPlayer.LiveVideoPlayerActivity;
import ru.sstu.vak.periscopeclient.viewPager.MyFragmentPagerAdapter;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Anton on 24.04.2018.
 */

public class MapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String ARGUMENT_PAGE_NUMBER = "page_number";

    private ArrayList<Marker> markers = new ArrayList<>();

    View currentView;

    private PeriscopeApi periscopeApi;
    private TokenUtils tokenUtils;

    private final String TAG = "MapFragment";
    private final String ANDROID_EMULATOR_LOCALHOST = "anton-var.ddns.net";
    private final String SERVER_PORT = "8080";
    private StompClient mStompClient;
    private Gson mGson = new GsonBuilder().create();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager locationManager;
    private MapView gMapView;
    private GoogleMap mMap;
    private AlertDialog turnOnGPSDialog;

    private int pageNumber;

    public static MapFragment newInstance(int pageNum) {
        MapFragment pageFragment = new MapFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, pageNum);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivitiesItems();
        initializeServerApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.map_fragment, null);
        gMapView = (MapView) currentView.findViewById(R.id.map);
        gMapView.getMapAsync(this);
        gMapView.onCreate(getArguments());
        MapsInitializer.initialize(getContext());
        connectStomp();
        return currentView;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        mMap = googleMap;
        setMarkers();

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(getContext(), LiveVideoPlayerActivity.class);
        intent.putExtra("streamName", marker.getTag().toString());
        startActivity(intent);
        return false;
    }

    private void connectStomp() {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.JWS,
                "ws://" + ANDROID_EMULATOR_LOCALHOST + ":" + SERVER_PORT + "/chat");

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

        mStompClient.topic("/roomLocationUpdated")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    RoomModel roomModel = mGson.fromJson(topicMessage.getPayload(), RoomModel.class);
                    LocationModel roomLocation = roomModel.getLocation();
                    refreshRoomMarker(roomModel.getStreamName(), roomLocation);
                });

        mStompClient.connect();
    }

    private void setMarkers() {
        String token = tokenUtils.getToken();
        Call<MyResponse<ArrayList<RoomModel>>> call = periscopeApi.getRooms(new MyRequest<Void>(null, token));
        call.enqueue(new Callback<MyResponse<ArrayList<RoomModel>>>() {
            @Override
            public void onResponse(Call<MyResponse<ArrayList<RoomModel>>> call, Response<MyResponse<ArrayList<RoomModel>>> response) {
                if (response.isSuccessful()) {
                    MyResponse<ArrayList<RoomModel>> resp = response.body();

                    if (resp.getError() == null) {
                        ArrayList<RoomModel> rooms = resp.getData();
                        for (int i = 0; i < rooms.size(); i++) {
                            refreshRoomMarker(rooms.get(i).getStreamName(), rooms.get(i).getLocation());
                        }
                    } else if (resp.getError().equals("invalid authToken")) {
                        Intent intent = new Intent(getContext(), ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
                        startActivityForResult(intent, 1);
                    }
                } else {
                    showMessage("Сервер вернул ошибку");
                }
            }

            @Override
            public void onFailure(Call<MyResponse<ArrayList<RoomModel>>> call, Throwable t) {
                showMessage("Сервер не отвечает");
            }
        });
    }


    private void refreshRoomMarker(String streamName, LocationModel location) {
        if (location == null) {
            removeMarker(streamName);
        } else if (isMarkerExist(streamName)) {
            updateMarker(streamName, location);
        } else {
            addMarker(streamName, location);
        }
    }

    private void updateMarker(String streamName, LocationModel location) {
        for (int i = 0; i < markers.size(); i++) {
            if (markers.get(i).getTag().equals(streamName)) {
                markers.get(i).setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                return;
            }
        }
    }

    private void removeMarker(String streamName) {
        for (int i = 0; i < markers.size(); i++) {
            if (markers.get(i).getTag().equals(streamName)) {
                markers.get(i).remove();
                markers.remove(i);
                return;
            }
        }
    }

    private void addMarker(String streamName, LocationModel location) {
        Marker marker = mMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).position(new LatLng(location.getLatitude(), location.getLongitude())).icon(
                BitmapDescriptorFactory.fromBitmap(getMapMarker(100, 100, R.mipmap.map_circle))));
        marker.setTag(streamName);
        markers.add(marker);
    }

    private boolean isMarkerExist(String streamName) {
        for (int i = 0; i < markers.size(); i++) {
            Object s = markers.get(i).getTag();
            if (markers.get(i).getTag().toString().equals(streamName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (gMapView != null) {
            gMapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        if (gMapView != null) {
            gMapView.onStop();
        }
        super.onStop();
    }

    @Override
    public void onStart() {
        if (gMapView != null) {
            gMapView.onStart();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (gMapView != null) {
            gMapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onLowMemory() {
        if (gMapView != null) {
            gMapView.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onPause() {
        if (gMapView != null) {
            gMapView.onPause();
        }
        super.onPause();

    }

    @Override
    public void onDestroy() {
        if (gMapView != null) {
            gMapView.onDestroy();
        }
        super.onDestroy();
    }


    private Bitmap getMapMarker(int height, int width, @DrawableRes int vectorDrawableResourceId) {
        BitmapDrawable bitmapdraw = (BitmapDrawable)  currentView.getResources().getDrawable(vectorDrawableResourceId);
        Bitmap g = bitmapdraw.getBitmap();
        return Bitmap.createScaledBitmap(g, width, height, false);
    }

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setActivitiesItems() {
        mFusedLocationClient = new FusedLocationProviderClient(getContext());
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        tokenUtils = new TokenUtils((MainActivity) getActivity());
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://anton-var.ddns.net:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

}