package com.distnet.gstark31897.distnet;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private AppDatabase database;
    private String contact = "";

    /**
     * View holder class
     * */
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout messageLayout;
        public TextView messageBody;

        public MessageViewHolder(View view) {
            super(view);
            messageLayout = (LinearLayout) view.findViewById(R.id.message_layout);
            messageBody = (TextView) view.findViewById(R.id.message_body);
        }
    }

    public MessageAdapter(AppDatabase database) {
        this.database = database;
    }

    public void setContact(String contact) {
        this.contact = contact;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = database.messageDao().getMessage(contact, position);
        holder.messageLayout.setGravity(message.getOutgoing() ? Gravity.RIGHT : Gravity.LEFT);
        holder.messageBody.setText(message.getBody());
    }

    @Override
    public int getItemCount() {
        return database.messageDao().countMessages(contact);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new MessageViewHolder(v);
    }
}