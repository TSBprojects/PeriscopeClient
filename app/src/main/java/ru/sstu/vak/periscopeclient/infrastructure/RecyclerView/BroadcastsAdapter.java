package ru.sstu.vak.periscopeclient.infrastructure.RecyclerView;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.sstu.vak.periscopeclient.R;
import ru.sstu.vak.periscopeclient.liveVideoPlayer.LivePlayer;

public class BroadcastsAdapter extends RecyclerView.Adapter<BroadcastsAdapter.BroadcastsViewHolder> {

    private Context context;
    private View.OnClickListener clickListener;
    //private HashMap<BroadcastsModel,LivePlayer> broadcastsList = new HashMap<BroadcastsModel, LivePlayer>();
    private ArrayList<BroadcastsModel> broadcastsList = new ArrayList<>();

    //private ArrayList<LivePlayer> previewList;
    public BroadcastsAdapter(Context context, View.OnClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    public void refreshBroadcasts(ArrayList<BroadcastsModel> newBroadcastsList) {
        boolean match = false;
        for (int i = 0; i < newBroadcastsList.size(); i++) {
            BroadcastsModel newBroadcast = newBroadcastsList.get(i);
            for (int j = 0; j < broadcastsList.size(); j++) {
                BroadcastsModel oldBroadcast = broadcastsList.get(j);
                if (newBroadcast.getStreamName().equals(oldBroadcast.getStreamName())) {
                    updateItem(j);
                    match = true;
                    break;
                }
            }
            if (!match) {
                addItem(newBroadcast);
            }
            match = false;
        }

        System.out.println();

        for (int i = 0; i < broadcastsList.size(); i++) {
            BroadcastsModel broadcast = broadcastsList.get(i);
            if (!isBroadcastExist(broadcast, newBroadcastsList)) {
                removeItem(i);
            }
        }
    }


    private boolean isBroadcastExist(BroadcastsModel broadcast, ArrayList<BroadcastsModel> broadcastsList) {
        for (int i = 0; i < broadcastsList.size(); i++) {
            if (broadcastsList.get(i).getStreamName().equals(broadcast.getStreamName())) {
                return true;
            }
        }
        return false;
    }


    private void updateItem(int position) {
        BroadcastsModel broadcast = broadcastsList.get(position);
        LivePlayer streamPreview = broadcast.getStreamPreview();
        streamPreview.onStop();
        broadcast.setStreamPreview(new LivePlayer(streamPreview.getSimpleExoPlayerView(), broadcast.getStreamName(), context));
    }

    private void addItem(BroadcastsModel broadcast) {
        broadcastsList.add(broadcast);
        notifyItemInserted(broadcastsList.size() - 1);
    }

    private void removeItem(int position) {
        broadcastsList.get(position).getStreamPreview().onStop();
        broadcastsList.remove(position);
        notifyItemRemoved(position);
    }

    public void setItems(Collection<BroadcastsModel> broadcasts) {
        broadcastsList.addAll(broadcasts);
        notifyDataSetChanged();
    }

    public void clearItems() {
        for (int i = 0; i < broadcastsList.size(); i++) {
            broadcastsList.get(i).getStreamPreview().onStop();
        }
        broadcastsList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BroadcastsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new BroadcastsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BroadcastsViewHolder holder, int position) {
        holder.bind(broadcastsList.get(position));
    }

    @Override
    public int getItemCount() {
        return broadcastsList.size();
    }


    class BroadcastsViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout broadcastLayout;
        private SimpleExoPlayerView stream_preview;
        private TextView observers_count_text_view;
        private TextView description_text_view;
        private TextView user_login_text_view;


        BroadcastsViewHolder(View itemView) {
            super(itemView);
            broadcastLayout = itemView.findViewById(R.id.broadcast);
            stream_preview = itemView.findViewById(R.id.stream_preview);
            observers_count_text_view = itemView.findViewById(R.id.observers_count_text_view);
            description_text_view = itemView.findViewById(R.id.description_text_view);
            user_login_text_view = itemView.findViewById(R.id.user_login_text_view);
        }

        void bind(BroadcastsModel broadcast) {
            broadcastLayout.setTag(broadcast.getStreamName());
            broadcastLayout.setOnClickListener(clickListener);
            stream_preview.setUseController(false);
            broadcast.setStreamPreview(new LivePlayer(stream_preview, broadcast.getStreamName(), context));
            observers_count_text_view.setText(broadcast.getObserversCount());
            if (broadcast.getDescription().equals("")) {
                description_text_view.setVisibility(View.GONE);
            } else {
                description_text_view.setText(broadcast.getDescription());
            }
            user_login_text_view.setText(broadcast.getUserLogin());
        }
    }
}