package ru.sstu.vak.periscopeclient.infrastructure;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.sstu.vak.periscopeclient.R;
//
//public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
//
//    private LayoutInflater inflater;
//    private List<ContactsContract.CommonDataKinds.Phone> phones;
//
//    RecyclerViewAdapter(Context context, List<ContactsContract.CommonDataKinds.Phone> phones) {
//        this.phones = phones;
//        this.inflater = LayoutInflater.from(context);
//    }
//    @Override
//    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//        View view = inflater.inflate(R.layout.list_item, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
//        ContactsContract.CommonDataKinds.Phone phone = phones.get(position);
//        holder.imageView.setImageResource(phone.getImage());
//        holder.nameView.setText(phone.getName());
//        holder.companyView.setText(phone.getCompany());
//    }
//
//    @Override
//    public int getItemCount() {
//        return phones.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        final ImageView imageView;
//        final TextView nameView, companyView;
//        ViewHolder(View view){
//            super(view);
//            imageView = (ImageView)view.findViewById(R.id.image);
//            nameView = (TextView) view.findViewById(R.id.name);
//            companyView = (TextView) view.findViewById(R.id.company);
//        }
//    }
//}