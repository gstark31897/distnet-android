package com.distnet.gstark31897.distnet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class InterfaceAdapter extends RecyclerView.Adapter<InterfaceAdapter.InterfaceViewHolder> {

    private AppDatabase database;

    /**
     * View holder class
     * */
    public class InterfaceViewHolder extends RecyclerView.ViewHolder {
        public TextView interfaceUri;
        public Button deleteButton;

        public InterfaceViewHolder(View view) {
            super(view);
            interfaceUri = (TextView) view.findViewById(R.id.interface_uri);
            deleteButton = (Button) view.findViewById(R.id.interface_delete);
        }
    }

    public InterfaceAdapter(AppDatabase database) {
        this.database = database;
    }

    @Override
    public void onBindViewHolder(InterfaceViewHolder holder, int position) {
        final Interface inter = database.interfaceDao().get(position);
        holder.interfaceUri.setText(inter.getUri());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.interfaceDao().delete(inter);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return database.interfaceDao().count();
    }

    @Override
    public InterfaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inter, parent, false);
        return new InterfaceViewHolder(v);
    }
}