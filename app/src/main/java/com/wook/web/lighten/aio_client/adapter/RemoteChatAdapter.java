package com.wook.web.lighten.aio_client.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wook.web.lighten.aio_client.R;
import com.wook.web.lighten.aio_client.data.RemoteChatData;

import java.util.ArrayList;

public class RemoteChatAdapter extends RecyclerView.Adapter<RemoteChatAdapter.ViewHolder>{
    private ArrayList<RemoteChatData> mChatList = new ArrayList<>();

    @NonNull
    @Override
    public RemoteChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RemoteChatAdapter.ViewHolder holder, int position) {
        RemoteChatData remoteChatData = mChatList.get(position);
        holder.setItem(remoteChatData);
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.chat_name);
            message = itemView.findViewById(R.id.chat_message);
        }

        public void setItem(RemoteChatData remoteChatData){
            name.setText(remoteChatData.getName());
            message.setText(remoteChatData.getMessage());
        }
    }

    public void setChatList(ArrayList<RemoteChatData> chatList){
        mChatList = chatList;
        notifyDataSetChanged();
    }

    public void addChatItem(RemoteChatData remoteChatData){
        mChatList.add(remoteChatData);
        notifyDataSetChanged();
    }
}
