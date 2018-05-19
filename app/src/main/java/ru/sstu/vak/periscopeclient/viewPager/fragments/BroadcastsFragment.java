package ru.sstu.vak.periscopeclient.viewPager.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.MainActivity;
import ru.sstu.vak.periscopeclient.R;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.RetrofitWrapper;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyRequest;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyResponse;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;
import ru.sstu.vak.periscopeclient.liveVideoPlayer.LivePlayer;
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

    private SwipeRefreshLayout swipe_refresh;
    private LinearLayout rooms_layout;
    private TextView empty_broadcasts_textview;
    private ProgressBar progressBar;

    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    private ArrayList<LivePlayer> previewList;

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
        previewList = new ArrayList<>();
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        initializeServerApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.broadcasts_fragment, null);
        setActivitiesItems(currentView);

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRooms(true);
                stopTimer();
                startTimer(15000);
            }
        });
        return currentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getActivity().getIntent().getBooleanExtra("signOut", false)) {
            stopTimer();
            startTimer(0);
            clearRoomsLayout();
            showMiddleProgressBar();
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
        empty_broadcasts_textview.setVisibility(View.INVISIBLE);
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
        retrofitWrapper.refreshRooms(new RetrofitWrapper.Callback< ArrayList<RoomModel>>() {
            @Override
            public void onSuccess(ArrayList<RoomModel> rooms) {
                swipe_refresh.setRefreshing(false);
                hideMiddleProgressBar();
                clearRoomsLayout();
                empty_broadcasts_textview.setVisibility(View.VISIBLE);
                for (RoomModel room : rooms) {
                    empty_broadcasts_textview.setVisibility(View.INVISIBLE);
                    addStreamToScreen(room.getStreamName(), room.getRoomOwner().getLogin(), room.getRoomDescription(), room.getObservers().size());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                swipe_refresh.setRefreshing(false);
                showMiddleProgressBar();
                clearRoomsLayout();
                if (swipe) {
                    showMessage(t.getMessage());
                }
            }
        });
    }

    private void playStream(final String streamName) {
        retrofitWrapper.playStream(streamName, new RetrofitWrapper.Callback<RoomModel>() {
            @Override
            public void onSuccess(RoomModel room) {
                swipe_refresh.setRefreshing(false);

                if (room == null) {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Эта трансляция уже закончилась")
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


    private void clearRoomsLayout() {
        for (int i = 0; i < previewList.size(); i++) {
            previewList.get(i).onStop();
        }
        previewList.clear();
        rooms_layout.removeAllViews();
    }

    private void addStreamToScreen(String streamName, String userLogin, String description, int observersCount) {
        TextView userLoginTV = createUserLoginTextView(userLogin);
        TextView streamDescrTV = createStreamDescrTextView(description);
        Space space = createSpace();
        TextView observersCountTV = createObserversCountTextView(Integer.toString(observersCount));
        ImageView observersImage = createObserversImage();
        TextView liveStreamTV = createLiveStreamTextView();
        LinearLayout observersCountLayout = createObserversCountLayout();
        LinearLayout mainInfoLayout = createMainInfoLayout();
        ProgressBar streamPreviewProgressBar = createStreamPreviewProgressBar();
        SimpleExoPlayerView streamPreview = createStreamPreview(streamName, streamPreviewProgressBar);
        LinearLayout mainLayout = createMainLayout(streamName);

        observersCountLayout.addView(liveStreamTV);
        observersCountLayout.addView(observersImage);
        observersCountLayout.addView(observersCountTV);
        observersCountLayout.addView(space);
        mainInfoLayout.addView(observersCountLayout);
        mainInfoLayout.addView(streamDescrTV);
        mainInfoLayout.addView(userLoginTV);
        mainLayout.addView(streamPreview);
        mainLayout.addView(streamPreviewProgressBar);
        mainLayout.addView(mainInfoLayout);
        rooms_layout.addView(mainLayout);
    }

    private LinearLayout createMainLayout(String streamName) {
        int PXmargin = convertDpToPixel(4);
        LinearLayout newLinearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams newLinearLayout_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        newLinearLayout.setBackgroundResource(backgroundResource);
        typedArray.recycle();

        newLinearLayout.setTag(streamName);
        newLinearLayout.setOnClickListener(this);
        newLinearLayout.setFocusable(true);
        newLinearLayout.setClickable(true);
        newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        newLinearLayout_params.setMargins(0, PXmargin, 0, 0);
        newLinearLayout.setLayoutParams(newLinearLayout_params);
        return newLinearLayout;
    }

    private SimpleExoPlayerView createStreamPreview(String streamName, ProgressBar progressBar) {
        SimpleExoPlayerView imageView = new SimpleExoPlayerView(getContext());
        LinearLayout.LayoutParams imageView_params =
                new LinearLayout.LayoutParams(
                        convertDpToPixel(100),
                        convertDpToPixel(100)
                );

        //stream preview
        //imageView.setBackgroundColor(getResources().getColor(R.color.middle_gray));
        //stream preview

        //imageView.setVisibility(View.GONE);
        imageView.setLayoutParams(imageView_params);

        imageView.setUseController(false);
        previewList.add(new LivePlayer(imageView, streamName, getContext(), progressBar));

        return imageView;
    }

    private ProgressBar createStreamPreviewProgressBar() {
        int PXpadding = convertDpToPixel(35);
        ProgressBar progressBar = new ProgressBar(getContext());
        LinearLayout.LayoutParams imageView_params =
                new LinearLayout.LayoutParams(
                        convertDpToPixel(100),
                        convertDpToPixel(100)
                );

        progressBar.setPadding(PXpadding, PXpadding, PXpadding, PXpadding);
        progressBar.setVisibility(View.GONE);
        progressBar.setLayoutParams(imageView_params);

        return progressBar;
    }

    private LinearLayout createMainInfoLayout() {
        int PXpadding = convertDpToPixel(7);
        LinearLayout newLinearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams newLinearLayout_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        3
                );
        newLinearLayout.setOrientation(LinearLayout.VERTICAL);
        newLinearLayout.setPadding(PXpadding, PXpadding, PXpadding, 0);
        newLinearLayout.setLayoutParams(newLinearLayout_params);
        return newLinearLayout;
    }

    private LinearLayout createObserversCountLayout() {
        LinearLayout newLinearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams newLinearLayout_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0
                );
        newLinearLayout.setGravity(Gravity.CENTER);
        newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        newLinearLayout.setLayoutParams(newLinearLayout_params);
        return newLinearLayout;
    }

    private TextView createLiveStreamTextView() {
        int PXpadding = convertDpToPixel(2);
        TextView newTextView = new TextView(getContext());
        LinearLayout.LayoutParams newTextView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        newTextView.setText("Прямой эфир");
        newTextView.setTextSize(12);
        newTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        newTextView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.record_lable));
        newTextView.setGravity(Gravity.CENTER);
        newTextView.setPadding(PXpadding, 0, PXpadding, PXpadding);
        newTextView.setLayoutParams(newTextView_params);
        return newTextView;
    }

    private ImageView createObserversImage() {
        int PXpadding = convertDpToPixel(4);
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        imageView.setImageResource(R.drawable.ic_people_black_24dp);
        imageView.setPadding(0, 0, -PXpadding, 0);
        imageView_params.setMargins(PXpadding, 0, 0, 0);
        imageView.setLayoutParams(imageView_params);
        return imageView;
    }

    private TextView createObserversCountTextView(String count) {
        int PXpadding = convertDpToPixel(4);
        TextView newTextView = new TextView(getContext());
        LinearLayout.LayoutParams newTextView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        newTextView.setText(count);
        newTextView.setTextSize(14);
        newTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.ic_people_color));
        newTextView.setGravity(Gravity.CENTER);
        newTextView.setPadding(-PXpadding, 0, 0, 0);
        newTextView.setLayoutParams(newTextView_params);
        return newTextView;
    }

    private Space createSpace() {
        Space space = new Space(getContext());
        LinearLayout.LayoutParams space_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        12
                );
        space.setLayoutParams(space_params);
        return space;
    }

    private TextView createStreamDescrTextView(String description) {
        TextView newTextView = new TextView(getContext());
        LinearLayout.LayoutParams newTextView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0
                );

        if (description.equals("")) {
            newTextView.setVisibility(View.GONE);
        } else {
            newTextView.setText(description);
        }

        newTextView.setTextSize(14);
        newTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        newTextView.setEllipsize(TextUtils.TruncateAt.END);
        newTextView.setMaxLines(3);
        newTextView.setLayoutParams(newTextView_params);
        return newTextView;
    }

    private TextView createUserLoginTextView(String userLogin) {
        TextView newTextView = new TextView(getContext());
        LinearLayout.LayoutParams newTextView_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        newTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.user_name_color));
        newTextView.setTextSize(12);
        newTextView.setText(userLogin);
        newTextView.setLayoutParams(newTextView_params);
        return newTextView;
    }


    private void setActivitiesItems(View currentView) {
        swipe_refresh = (SwipeRefreshLayout) currentView.findViewById(R.id.swipe_refresh);
        tokenUtils = new TokenUtils((MainActivity) getActivity());
        retrofitWrapper = new RetrofitWrapper(getContext());
        rooms_layout = (LinearLayout) currentView.findViewById(R.id.rooms_layout);
        empty_broadcasts_textview = (TextView) currentView.findViewById(R.id.empty_broadcasts_textview);
        progressBar = (ProgressBar) currentView.findViewById(R.id.progressBar);
        mMyTimerTask = new MyTimerTask();
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://anton-var.ddns.net:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private int convertDpToPixel(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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
