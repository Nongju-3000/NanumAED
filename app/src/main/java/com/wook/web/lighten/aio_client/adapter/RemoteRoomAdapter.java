package com.wook.web.lighten.aio_client.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wook.web.lighten.aio_client.R;
import com.wook.web.lighten.aio_client.data.RoomData;

import java.util.ArrayList;

import soup.neumorphism.NeumorphButton;

public class RemoteRoomAdapter extends RecyclerView.Adapter<RemoteRoomAdapter.ViewHolder>{
    private ArrayList<RoomData> mRoomList = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    private static OnItemClickListener onItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public RemoteRoomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RemoteRoomAdapter.ViewHolder holder, int position) {

        RoomData roomData = mRoomList.get(position);

        holder.setItem(roomData);
    }

    @Override
    public int getItemCount() {
        return mRoomList.size();
    }

    public void setRoomList(ArrayList<RoomData> roomList) {
        mRoomList = roomList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        TextView num_of_people;
        NeumorphButton enterbutton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName_remote);
            num_of_people = itemView.findViewById(R.id.numofpeople_remote);
            enterbutton = itemView.findViewById(R.id.enter_remote);

            enterbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(pos);
                        }
                    }
                }
            });
        }

        public void setItem(RoomData roomData) {
            roomName.setText(roomData.getName());
            num_of_people.setText(roomData.getNum_of_people() + "");
        }
    }

    public RoomData getItem(int position) {
        return mRoomList.get(position);
    }

    public boolean duplicateCheck(String roomName) {
        for (int i = 0; i < mRoomList.size(); i++) {
            if (mRoomList.get(i).getName().equals(roomName)) {
                return true;
            }
        }
        return false;
    }
}
