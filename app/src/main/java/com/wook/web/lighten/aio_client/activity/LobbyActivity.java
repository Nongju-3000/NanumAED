package com.wook.web.lighten.aio_client.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wook.web.lighten.aio_client.R;
import com.wook.web.lighten.aio_client.adapter.RemoteChatAdapter;
import com.wook.web.lighten.aio_client.adapter.RemoteRoomAdapter;
import com.wook.web.lighten.aio_client.data.RemoteChatData;
import com.wook.web.lighten.aio_client.data.RoomData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class LobbyActivity extends Activity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private RecyclerView room_recyclerview, chat_recyclerview;
    private EditText chat_edittext;
    private String token, name;
    private ImageButton chat_sendbutton;
    private RemoteRoomAdapter remoteRoomAdapter;
    private RemoteChatAdapter remoteChatAdapter;
    private BackPressCloseHandler backPressCloseHandler;
    private boolean trainercheck = false;


    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lobby);

        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                // 23 버전 이상일 때 상태바 하얀 색상에 회색 아이콘 색상을 설정
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#6178e3"));
            }
        }else if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            getWindow().setStatusBarColor(Color.BLACK);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        this.backPressCloseHandler = new LobbyActivity.BackPressCloseHandler(this);

        chat_edittext = findViewById(R.id.chat_edittext);
        room_recyclerview = findViewById(R.id.room_recyclerview);
        chat_recyclerview = findViewById(R.id.chat_recyclerview);
        remoteRoomAdapter = new RemoteRoomAdapter();
        room_recyclerview.setAdapter(remoteRoomAdapter);
        room_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        remoteChatAdapter = new RemoteChatAdapter();
        chat_recyclerview.setAdapter(remoteChatAdapter);
        chat_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        chat_sendbutton = findViewById(R.id.chat_sendbutton);

        token = getIntent().getStringExtra("Token");
        name = getIntent().getStringExtra("Name");

        Log.e("Name", name);

        databaseReference.child("Chat").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RemoteChatData remoteChatData = dataSnapshot.getValue(RemoteChatData.class);
                remoteChatAdapter.addChatItem(remoteChatData);
                chat_recyclerview.scrollToPosition(remoteChatAdapter.getItemCount()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chat_sendbutton.setOnClickListener(v -> {
            if(chat_edittext.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), "메세지를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                String getTime = sdf.format(date);
                RemoteChatData chatData = new RemoteChatData(name, chat_edittext.getText().toString(), getTime);  // 유저 이름과 메세지로 chatData 만들기
                databaseReference.child("Chat").push().setValue(chatData);
                chat_edittext.setText("");
            }
        });

        remoteRoomAdapter.setOnItemClickListener(new RemoteRoomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                final String getTime = sdf.format(date);
                String room = remoteRoomAdapter.getItem(pos).getName();
                databaseReference.child("Room").child(room).child("trainer").orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null){
                            trainercheck = true;
                        } else {
                            trainercheck = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if(!trainercheck){
                    NameData nameData = new NameData(name, token);
                    databaseReference.child("Room").child(room).child("user").push().setValue(nameData);
                    ChatData chatData = new ChatData("입장했습니다.", getTime, name);  // 유저 이름과 메세지로 chatData 만들기
                    databaseReference.child("Room").child(room).child("message").push().setValue(chatData);
                    DatabaseReference into = databaseReference.child("Room").child(room).child("into").push();
                    String intokey = into.getKey();
                    into.setValue(chatData.getUserName());
                    Intent intent = new Intent(LobbyActivity.this, CPRActivity.class);
                    intent.putExtra("room", room);
                    intent.putExtra("name", name);
                    intent.putExtra("intokey", intokey);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Name" + name + " is already in room", Toast.LENGTH_SHORT).show();
                }
            }
        });

        databaseReference.child("Room").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<RoomData> roomDataArrayList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String roomname = dataSnapshot.getKey();
                    int num = Integer.parseInt(dataSnapshot.child("into").getChildrenCount() + "");
                    RoomData roomData = new RoomData(roomname, num);
                    roomDataArrayList.add(roomData);
                }
                remoteRoomAdapter.setRoomList(roomDataArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }
        public void onBackPressed() {
            Intent main = new Intent(LobbyActivity.this, RoomActivity.class);
            startActivity(main);

            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
            finish();
        }
    }
}
