package ru.sstu.vak.periscopeclient.infrastructure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.TextView;

import ru.sstu.vak.periscopeclient.R;

/**
 * Created by Anton on 07.05.2018.
 */

public class StopStreamDialog  extends DialogFragment {

    private TextView loading_message;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.stop_stream_loading_fragment, null));

        return builder.create();
    }
}