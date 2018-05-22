package ru.sstu.vak.periscopeclient.viewPager.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.R;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.RetrofitWrapper;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.infrastructure.RecyclerView.BroadcastsAdapter;
import ru.sstu.vak.periscopeclient.infrastructure.RecyclerView.BroadcastsModel;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;
import ru.sstu.vak.periscopeclient.liveVideoPlayer.LiveVideoPlayerActivity;

/**
 * Created by Anton on 24.04.2018.
 */

public class BroadcastsFragment extends Fragment implements View.OnClickListener {

    private static final String ARGUMENT_PAGE_NUMBER = "page_number";
    private int pageNumber;

    private RetrofitWrapper retrofitWrapper;
    private PeriscopeApi periscopeApi;
    private TokenUtils tokenUtils;

    private BroadcastsAdapter broadcastsAdapter;

    private SwipeRefreshLayout swipe_refresh;
    private LinearLayout rooms_layout;
    private RecyclerView recycler_view;
    private TextView empty_broadcasts_text_view;
    private ProgressBar progressBar;

    private Timer mTimer;
    private MyTimerTask mMyTimerTask;


    public static BroadcastsFragment newInstance(int pageNum) {
        BroadcastsFragment pageFragment = new BroadcastsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, pageNum);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        initializeServerApi();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.broadcasts_fragment, null);
        setActivitiesItems(currentView);
        initializeRecyclerView();

        return currentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getActivity().getIntent().getBooleanExtra("signOut", false)) {
            stopTimer();
            startTimer(0);
            //refreshRoomsLayout();
            //showMiddleProgressBar();
        } else {
            getActivity().getIntent().putExtra("signOut", false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    private void showMiddleProgressBar() {
        empty_broadcasts_text_view.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        swipe_refresh.setEnabled(false);
    }

    private void hideMiddleProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        swipe_refresh.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        playStream(view.getTag().toString());
    }

    private void refreshRooms(final boolean swipe) {
        retrofitWrapper.getRooms(true, new RetrofitWrapper.Callback<ArrayList<RoomModel>>() {
            @Override
            public void onSuccess(ArrayList<RoomModel> rooms) {
                swipe_refresh.setRefreshing(false);
                hideMiddleProgressBar();

                ArrayList<BroadcastsModel> broadcastsModels = convertRoomModels(rooms);
                broadcastsAdapter.refreshBroadcasts(convertRoomModels(rooms));
                if (broadcastsModels.size() == 0) {
                    empty_broadcasts_text_view.setVisibility(View.VISIBLE);
                } else {
                    empty_broadcasts_text_view.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                swipe_refresh.setRefreshing(false);
                showMiddleProgressBar();
                refreshRoomsLayout();
                if (swipe) {
                    showMessage(t.getMessage());
                }
            }
        });
    }

    private void playStream(final String streamName) {
        retrofitWrapper.getRoom(streamName, new RetrofitWrapper.Callback<RoomModel>() {
            @Override
            public void onSuccess(RoomModel room) {
                swipe_refresh.setRefreshing(false);

                if (room == null) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(getString(R.string.user_finished_broadcasting))
                            .setPositiveButton(android.R.string.yes, null).show();
                    stopTimer();
                    startTimer(0);
                } else {
                    Intent intent = new Intent(getContext(), LiveVideoPlayerActivity.class);
                    intent.putExtra("streamName", streamName);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                swipe_refresh.setRefreshing(false);
                showMessage(t.getMessage());
            }
        });
    }

    private void refreshRoomsLayout() {
        broadcastsAdapter.clearItems();
    }


    private void setActivitiesItems(View currentView) {
        recycler_view = currentView.findViewById(R.id.recycler_view);
        swipe_refresh = currentView.findViewById(R.id.swipe_refresh);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRooms(true);
                stopTimer();
                startTimer(15000);
            }
        });
        tokenUtils = new TokenUtils(getActivity());
        retrofitWrapper = new RetrofitWrapper(getContext());
        rooms_layout = currentView.findViewById(R.id.rooms_layout);
        empty_broadcasts_text_view = currentView.findViewById(R.id.empty_broadcasts_textview);
        progressBar = currentView.findViewById(R.id.progressBar);
        mMyTimerTask = new MyTimerTask();
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%1$s:%2$s", getString(R.string.server_domain_name), getString(R.string.server_port)))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

    private void initializeRecyclerView() {
        recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        broadcastsAdapter = new BroadcastsAdapter(getContext(), this);
        recycler_view.setAdapter(broadcastsAdapter);
        recycler_view.setItemAnimator(new SlideInDownAnimator());
    }

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<BroadcastsModel> convertRoomModels(ArrayList<RoomModel> roomModels) {
        ArrayList<BroadcastsModel> broadcastsModels = new ArrayList<BroadcastsModel>();

        for (RoomModel roomModel : roomModels) {
            broadcastsModels.add(new BroadcastsModel(Integer.toString(roomModel.getObservers().size()),
                    roomModel.getRoomDescription(),
                    roomModel.getRoomOwner().getLogin(),
                    roomModel.getStreamName(), null));
        }
        return broadcastsModels;
    }


    private void startTimer(int delay) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, delay, 15000);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            refreshRooms(false);
        }
    }

}
