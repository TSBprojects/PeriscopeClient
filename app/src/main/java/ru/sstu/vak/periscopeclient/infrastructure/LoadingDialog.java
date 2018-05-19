package ru.sstu.vak.periscopeclient.infrastructure;

/**
 * Created by Anton on 29.04.2018.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.TextView;

import ru.sstu.vak.periscopeclient.R;

public class LoadingDialog extends DialogFragment {

    private TextView loading_message;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.loading_fragment, null));

        return builder.create();
    }
}