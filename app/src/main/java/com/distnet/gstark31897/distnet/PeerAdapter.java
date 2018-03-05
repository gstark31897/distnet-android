package com.distnet.gstark31897.distnet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.PeerViewHolder> {

    private AppDatabase database;

    public class PeerViewHolder extends RecyclerView.ViewHolder {
        public TextView peerUri;
        public Button peerDelete;

        public PeerViewHolder(View view) {
            super(view);
            peerUri = (TextView) view.findViewById(R.id.peer_uri);
            peerDelete = (Button) view.findViewById(R.id.peer_delete);
        }
    }

    public PeerAdapter(AppDatabase database) {
        this.database = database;
    }

    @Override
    public void onBindViewHolder(PeerViewHolder holder, int position) {
        final Peer peer = database.peerDao().get(position);
        holder.peerUri.setText(peer.getUri());
        holder.peerDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.peerDao().delete(peer);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return database.peerDao().count();
    }

    @Override
    public PeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.peer, parent, false);
        return new PeerViewHolder(v);
    }
}