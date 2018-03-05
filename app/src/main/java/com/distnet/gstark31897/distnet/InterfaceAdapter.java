package com.distnet.gstark31897.distnet;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class InterfaceAdapter extends RecyclerView.Adapter<InterfaceAdapter.InterfaceViewHolder> {

    private AppDatabase database;
    private SettingsActivity parent;

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

    public InterfaceAdapter(AppDatabase database, SettingsActivity parent) {
        this.database = database;
        this.parent = parent;
    }

    @Override
    public void onBindViewHolder(InterfaceViewHolder holder, int position) {
        final Interface inter = database.interfaceDao().get(position);
        holder.interfaceUri.setText(inter.getUri());
        holder.deleteButton.setText(inter.getPermanent() ? R.string.button_delete : R.string.button_remove);
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.makeIntent("remove_interface", inter.getUri());
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