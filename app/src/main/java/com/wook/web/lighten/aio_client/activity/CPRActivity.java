package com.wook.web.lighten.aio_client.activity;

import static com.wook.web.lighten.aio_client.activity.RoomActivity.prefs;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.wook.web.lighten.aio_client.R;
import com.wook.web.lighten.aio_client.adapter.RemoteChatAdapter;
import com.wook.web.lighten.aio_client.ble.BluetoothLeServiceCPR;
import com.wook.web.lighten.aio_client.data.RemoteChatData;
import com.wook.web.lighten.aio_client.db.AppDatabase;
import com.wook.web.lighten.aio_client.db.Converters;
import com.wook.web.lighten.aio_client.db.Report;
import com.wook.web.lighten.aio_client.utils.FinishService;
import com.wook.web.lighten.aio_client.utils.LocationPermission;
import com.wook.web.lighten.aio_client.utils.Print;
import com.wook.web.lighten.aio_client.utils.ScanExceptionHandler;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import soup.neumorphism.NeumorphButton;
import soup.neumorphism.NeumorphCardView;
import soup.neumorphism.NeumorphImageButton;


public class CPRActivity extends AppCompatActivity {

    private final static String TAG = CPRActivity.class.getSimpleName();
    private static int depth_true;
    private static int depth_false;
    private final static int depth_ready = 15;
    private static int depth_over;
    private final static int cycle_set = 30;
    private final static int breath_set = 2;
    private boolean isout = false;

    private static ToBinary ToBinary;

    private SharedPreferences sharedPreferences;
    private HashMap<String, String> Devices = new HashMap<>();

    private BackPressCloseHandler backPressCloseHandler;

    private BluetoothLeServiceCPR bluetoothLeServiceCPR;
    private FinishService finishService;

    private boolean mConnected = false;

    private BluetoothGattCharacteristic mNotifyCharacteristic = null;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    private ArrayList<UserItem> cprItem_01 = new ArrayList<>();

    private Activity activity;

    public static ListViewAdapterCPR listViewAdapterCPR;
    public static ListView listviewCPR;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter02;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 20000; //scan time 4000

    private Button depth_btn01;
    private Button press_ave_btn01;

    private View view01;

    private Button standard_btn01;

    private ImageView lung01;
    private ImageView test_lung01;
    private ImageView babyCircle;
    ClipDrawable lung_clip01;
    private LinearLayout cpr_timer_layout, user_select_layout;

    private Switch start_mode_cpr;
    private Switch mode_cpr;

    private boolean start_mode;
    private boolean check_mode;
    private boolean start_check = true;

    private ImageView cpr_arrow01, cpr_arrow01_;


    private TextView mode_cpr_name;
    private TextView mode_cpr_value;
    private TextView cpr_timer;

    private RecyclerView chatroom_recyclerview;
    private EditText chatroom_edittext;
    private ImageButton chatroom_sendbutton;

    private Button cpr_sub, cpr_add, standardCPR_btn01, depth_btn_cpr_01, video_conferenceBtn;

    private long MillisecondTime, StartTime, TimeBuff, UpdateTime, intrval_01, StartTime_L, position_intrval = 0L;

    private Handler handler;

    private int score_01, cycle_01 = 0;

    private int Seconds_, Seconds, Minutes, MilliSeconds, mSeconds_, mSeconds;

    private int handOff_01;

    private String toDay;

    private ArrayList<Float> breathval_01 = new ArrayList<Float>() {{
        add(200.0f);
    }};

    private ArrayList<Float> breathtime_01 = new ArrayList<Float>() {{
        add(0.0f);
    }};

    private ArrayList<Float> presstime_list01 = new ArrayList<Float>();


    private boolean isBreath01 = false;

    private RadioGroup cpr_interval;

    private int angle01;
    private int position01;
    private int breath01;

    private com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView YouTubePlayerView;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private RemoteChatAdapter remoteChatAdapter;

    private String room;
    private String UserName;
    private String[] UsersName;
    private String currentUserName;

    private boolean checkMagnet[] = {true, true, true};

    private boolean device2_connect = false;

    private int depth_correct, depth_num = 0;

    private boolean device1_connect = false;

    private int total_count = 0;

    private ArrayList<Long> peakTimes = new ArrayList<Long>();
    ArrayList<Float> bpm1 = new ArrayList<>();
    ArrayList<Float> tmp_bpm1 = new ArrayList<>();

    private int event_time = 0;

    float position_bpm = 0f;
    private int position_num01 = 0;
    private int position_correct01 = 0;
    private int lung_num01 = 0;
    private int lung_correct01 = 0;

    private int interval = 100;
    private boolean testmode = false;
    private int minDepth = 30;
    private int maxDepth = 60;
    private boolean playCheck = false;

    private TextView remote_depth_text;
    private TextView remote_arrow_down_text;
    private TextView remote_arrow_up_text;
    private LinearLayout count_layout;

    private ImageView cpr_ani01, cpr_ani02;

    private YouTubePlayer youTubePlayer;
    private boolean isInit = false;

    private List<Pair<Date, String>> playerStatesHistory = new ArrayList<>();

    private ImageView ytb_img;

    private ImageView angle_remote, press_position;
    private ImageButton press_point_btn;
    private TextView mainName, correctCount, totalCount, name_first, name_second;

    private int correct_position = 0;
    private int total_position = 0;

    //private AutoFitTextureView cameraView;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION"};

    private ChildEventListener childEventListener, conferenceEventListener, chatEventListener;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimensions;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private boolean isOut = false;

    private ArrayList<String> mac_list;

    private int connected_num = 0;
    private int request_num = 0;
    private int scan_count = 0;
    private boolean isConnect = true;
    private Double enter_time;

    private int frame_width;
    private int frame_interval;
    private float div_interval;
    int press_width;
    String token;

    private int pre_angle = 0;
    private int hand_off_time = 0;

    private boolean isReset = false;

    private Button depth_btn_cpr_up;
    private ArrayList<Integer> lung_list01;
    private ArrayList<Float> lungtime_list01;

    private JitsiMeetConferenceOptions latestOptions = null;

    private String address01;
    String[] address_array01;
    int max_lung01 = 100;
    int min_lung01 = 64;

    private final int BREOVERTIME = 20;

    int ventil_volume_01 = 0;

    int bre_threshold01 = 69;
    private int over_breath01 = 0;
    private int bre_level01;
    private int bre_level02;
    private boolean isBreOver01 = false;
    private boolean isBreBelow01 = false;
    private boolean isImageNormal01 = true;
    double gap_lung01;
    boolean isCali01 = false;
    private ImageView anne;
    private View depthCPR_view01;
    private ArrayList<Float> bluetoothtime_list01 = new ArrayList<Float>() {{
        add(0f);
    }};
    private boolean isReady = false;
    private LinearLayout cpr_layout_01;
    private ImageView device_disconnect_cpr_01;
    private Handler handler_cpr01_disconnect = new Handler(Looper.getMainLooper());
    private boolean isAdult = true;
    private boolean isReversed = false;
    private boolean isLungDrawing = false;

    //TODO BLE SERVICE CONNECTION
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            bluetoothLeServiceCPR = ((BluetoothLeServiceCPR.LocalBinder) service).getService();

            if (!bluetoothLeServiceCPR.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeServiceCPR = null;
        }
    };

    //TODO FINISH SERVICE CONNECTION
    private final ServiceConnection finishConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            finishService = ((FinishService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            finishService = null;
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    //TODO Broadcast_Receiver
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e("action", action);
            if (bluetoothLeServiceCPR.ACTION_BLE_CONNECTED.equals(action)) {
                connection_on(intent.getStringExtra(bluetoothLeServiceCPR.EXTRA_BLE_DEVICE_ADDRESS));
            } else if (bluetoothLeServiceCPR.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(bluetoothLeServiceCPR.EXTRA_DATA));
            } else if (bluetoothLeServiceCPR.ACTION_NEW_STATE.equals(action)) {
                String status = intent.getStringExtra(BluetoothLeServiceCPR.EXTRA_NEW_STATE);
                String macAddress = intent.getStringExtra(BluetoothLeServiceCPR.EXTRA_BLE_DEVICE_ADDRESS);

                if (status.equals("connected")) {
                    addBluetoothTime(macAddress);
                    showNoti(macAddress, true);
                } else if (status.equals("disconnected")) {
                    addBluetoothTime(macAddress);
                    showNoti(macAddress, false);
                }
            }
        }
    };

    private void addBluetoothTime(String mac) {
        if (!start_check) {
            long now = System.currentTimeMillis();
            now = now - (now % 10);

            float bletime = (float) ((now - StartTime_L) / 1000.0);
            if (!device2_connect) {
                if (Objects.equals(Devices.get("Device_01"), mac)) {
                    bluetoothtime_list01.add(bletime);
                }
            } else {
                if (Objects.equals(Devices.get("Device_02"), mac)) {
                    bluetoothtime_list01.add(bletime);
                }
            }
        }
    }

    private void showNoti(String mac, boolean isConnected) {
        if (isReady) {
            long now_ = System.currentTimeMillis();
            Date date_ = new Date(now_);
            SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String time = sdf_.format(date_);
            int connected = 0;
            if (!device2_connect) {
                if (Objects.equals(Devices.get("Device_01"), mac)) {
                    if (isConnected) {
                        connected = 1;
                    } else {
                        connected = 0;
                    }
                    ChatData chatData_ = new ChatData("연결/" + connected + "/" + mac, time, UserName);
                    databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);

                    if (!isConnected) {
                        cpr_layout_01.setAlpha(0.2f);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            handler_cpr01_disconnect.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (device_disconnect_cpr_01.getVisibility() == View.INVISIBLE) {
                                        device_disconnect_cpr_01.setVisibility(View.VISIBLE);
                                    } else {
                                        device_disconnect_cpr_01.setVisibility(View.INVISIBLE);
                                    }
                                    handler_cpr01_disconnect.postDelayed(this, 1000);
                                }
                            }, 0, 1000);
                        }
                    } else {
                        handler_cpr01_disconnect.removeMessages(0);
                        device_disconnect_cpr_01.setVisibility(View.INVISIBLE);
                        cpr_layout_01.setAlpha(1f);
                    }
                }
            } else {
                if (Objects.equals(Devices.get("Device_02"), mac)) {
                    if (isConnected) {
                        connected = 1;
                    } else {
                        connected = 0;
                    }
                    ChatData chatData_ = new ChatData("연결/" + connected, time, UserName);
                    databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);
                }
                if (!isConnected) {
                    cpr_layout_01.setAlpha(0.2f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        handler_cpr01_disconnect.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (device_disconnect_cpr_01.getVisibility() == View.INVISIBLE) {
                                    device_disconnect_cpr_01.setVisibility(View.VISIBLE);
                                } else {
                                    device_disconnect_cpr_01.setVisibility(View.INVISIBLE);
                                }
                                handler_cpr01_disconnect.postDelayed(this, 1000);
                            }
                        }, 0, 1000);
                    }
                } else {
                    handler_cpr01_disconnect.removeMessages(0);
                    device_disconnect_cpr_01.setVisibility(View.INVISIBLE);
                    cpr_layout_01.setAlpha(1f);
                }
            }
        }
    }

    public void onBackPressed() {
        this.backPressCloseHandler.onBackPressed();
    }

    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private RxBleDevice bleDevice1, bleDevice2;

    //TODO onCreate
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent destroy = new Intent(CPRActivity.this, FinishService.class);
        destroy.putExtra("room", room);
        destroy.putExtra("UserName", UserName);
        destroy.setAction(FinishService.ACTION_INITIALIZE);
        startService(destroy);
        setContentView(R.layout.activity_mode_cpr);
        cpr_timer_layout = findViewById(R.id.cpr_timer_layout);
        user_select_layout = findViewById(R.id.user_select_layout);
        name_first = findViewById(R.id.name_first);
        name_second = findViewById(R.id.name_second);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        enter_time = Double.parseDouble(sdf.format(date));
        Intent intent = getIntent();
        room = intent.getStringExtra("room");
        UserName = intent.getStringExtra("name");
        mainName = findViewById(R.id.mainName);
        mainName.setText("CPR(" + room + "," + UserName + ")");
        if (UserName.contains("&")) {
            UsersName = UserName.split("&");
            currentUserName = UsersName[0];
            cpr_timer_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));
            user_select_layout.setVisibility(View.VISIBLE);
            name_first.setText(UsersName[0]);
            name_second.setText(UsersName[1]);
            name_first.setTextColor(Color.parseColor("#FF6A0D"));
            mainName.setText("CPR(" + room + "," + currentUserName + ")");
            long mnow = System.currentTimeMillis();
            Date mdate = new Date(mnow);
            SimpleDateFormat msdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            final String getTime = msdf.format(mdate);
            ChatData chatData = new ChatData("사용자전환/" + currentUserName, getTime, UserName);  // 유저 이름과 메세지로 chatData 만들기
            databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
        } else {
            currentUserName = UserName;
            long mnow = System.currentTimeMillis();
            Date mdate = new Date(mnow);
            SimpleDateFormat msdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            final String getTime = msdf.format(mdate);
            ChatData chatData = new ChatData("사용자전환/" + currentUserName, getTime, UserName);  // 유저 이름과 메세지로 chatData 만들기
            databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
        }

        name_first.setOnClickListener(v -> {
            if (currentUserName.equals(UsersName[1])) {
                name_first.setTextColor(Color.parseColor("#FF6A0D"));
                name_second.setTextColor(Color.parseColor("#FFFFFF"));
                currentUserName = UsersName[0];
                mainName.setText("CPR(" + room + "," + currentUserName + ")");
                long mnow = System.currentTimeMillis();
                Date mdate = new Date(mnow);
                SimpleDateFormat msdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                final String getTime = msdf.format(mdate);
                ChatData chatData = new ChatData("사용자전환/" + currentUserName, getTime, UserName);  // 유저 이름과 메세지로 chatData 만들기
                databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
            }
        });

        name_second.setOnClickListener(v -> {
            if (currentUserName.equals(UsersName[0])) {
                name_first.setTextColor(Color.parseColor("#FFFFFF"));
                name_second.setTextColor(Color.parseColor("#FF6A0D"));
                currentUserName = UsersName[1];
                mainName.setText("CPR(" + room + "," + currentUserName + ")");
                long mnow = System.currentTimeMillis();
                Date mdate = new Date(mnow);
                SimpleDateFormat msdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                final String getTime = msdf.format(mdate);
                ChatData chatData = new ChatData("사용자전환/" + currentUserName, getTime, UserName);  // 유저 이름과 메세지로 chatData 만들기
                databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
            }
        });

        setChatEventListener();

        rxBleClient = RxBleClient.create(this);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Log.e(TAG, e.getMessage());
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Log.e(TAG, e.getMessage());
                return;
            }
            Log.e("Undeliverable exception received, not sure what to do", e.getMessage());
        });

        lung_list01 = new ArrayList<>();
        lungtime_list01 = new ArrayList<>();

        mac_list = new ArrayList<>();


        //cameraView = findViewById(R.id.cameraView);
        bluetoothtime_list01 = new ArrayList<>();

        databaseReference.child("Room").child(room).child("user").orderByChild("name").equalTo(UserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datas : snapshot.getChildren()) {
                    token = datas.getRef().getKey();
                    Intent serviceIntent = new Intent(CPRActivity.this, FinishService.class);
                    bindService(serviceIntent, finishConnection, BIND_AUTO_CREATE);
                    Intent sender2 = new Intent(CPRActivity.this, FinishService.class);
                    sender2.putExtra("room", room);
                    sender2.putExtra("UserName", UserName);
                    sender2.putExtra("token", datas.getRef().getKey());
                    sender2.setAction(FinishService.ACTION_INITIALIZE);
                    startService(sender2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        device_disconnect_cpr_01 = findViewById(R.id.device_disconnect_cpr_01);
        cpr_layout_01 = findViewById(R.id.cpr_layout_01);


        //ytb_img = findViewById(R.id.ytb_img);
        //YouTubePlayerView = findViewById(R.id.you_tube_player_view);
        //getLifecycle().addObserver(YouTubePlayerView);
        press_position = findViewById(R.id.press_position);
        press_point_btn = findViewById(R.id.press_point_btn);

        chatroom_recyclerview = findViewById(R.id.chatroom_recyclerview);
        chatroom_edittext = findViewById(R.id.chatroom_edittext);
        chatroom_sendbutton = findViewById(R.id.chatroom_sendbutton);

        sharedPreferences = getApplication().getSharedPreferences("DeviceCPR", MODE_PRIVATE);

        prefs.edit().putString("room", room).apply();

        handler = new Handler(Looper.getMainLooper());

        anne = findViewById(R.id.anne);

        start_mode_cpr = findViewById(R.id.start_mode_cpr);
        mode_cpr = findViewById(R.id.mode_cpr);

        start_mode = sharedPreferences.getBoolean("starModeCPR", false);
        start_mode_cpr.setChecked(start_mode);

        check_mode = sharedPreferences.getBoolean("checkModeCPR", false);
        mode_cpr.setChecked(check_mode);

        cpr_sub = findViewById(R.id.cpr_sub);
        cpr_add = findViewById(R.id.cpr_add);

        mode_cpr_name = findViewById(R.id.mode_cpr_name);
        mode_cpr_value = findViewById(R.id.mode_cpr_value);

        depth_btn_cpr_up = findViewById(R.id.depth_btn_cpr_up);

        standardCPR_btn01 = findViewById(R.id.standardCPR_btn01);

        depth_btn_cpr_01 = findViewById(R.id.depth_btn_cpr_01);

        correctCount = findViewById(R.id.correctCount);
        totalCount = findViewById(R.id.totalCount);
        count_layout = findViewById(R.id.countlayout);

        remoteChatAdapter = new RemoteChatAdapter();
        chatroom_recyclerview.setAdapter(remoteChatAdapter);
        chatroom_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        video_conferenceBtn = findViewById(R.id.video_conferenceBtn);

        databaseReference.child("Chat").child(room).addChildEventListener(chatEventListener);

        chatroom_sendbutton.setOnClickListener(v -> {
            if (chatroom_edittext.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), R.string.input_message_here, Toast.LENGTH_SHORT).show();
                return;
            } else {
                long mnow = System.currentTimeMillis();
                Date mdate = new Date(mnow);
                SimpleDateFormat msdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                String getTime = msdf.format(mdate);
                RemoteChatData chatData = new RemoteChatData(UserName, chatroom_edittext.getText().toString(), getTime);  // 유저 이름과 메세지로 chatData 만들기
                databaseReference.child("Chat").child(room).push().setValue(chatData);
                chatroom_edittext.setText("");
            }
        });

        start_mode_cpr.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("starModeCPR", isChecked).apply();
            start_mode = isChecked;
        });

        if (!check_mode) {
            mode_cpr_name.setText("TIME");
            mode_cpr_value.setText("30");
        } else {
            mode_cpr_name.setText("CYCLE");
            mode_cpr_value.setText("2");
        }

        mode_cpr.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("checkModeCPR", isChecked).apply();
            check_mode = isChecked;
            if (!isChecked) {
                mode_cpr_name.setText("TIME");
                mode_cpr_value.setText("30");
            } else {
                mode_cpr_name.setText("CYCLE");
                mode_cpr_value.setText("2");
            }
        });

        cpr_interval = findViewById(R.id.cpr_interval);
        cpr_interval.check(sharedPreferences.getInt("intervalkModeCPR", 0));
        cpr_interval.setOnCheckedChangeListener((group, checkedId) -> sharedPreferences.edit().putInt("intervalkModeCPR", group.getCheckedRadioButtonId()).apply());


        cpr_timer = findViewById(R.id.cpr_timer);
        cpr_arrow01 = findViewById(R.id.cpr_arrow01);
        cpr_arrow01_ = findViewById(R.id.cpr_arrow01_);
        depthCPR_view01 = findViewById(R.id.depthCPR_view01);
        depth_btn01 = findViewById(R.id.depth_btn_cpr_01);
        press_ave_btn01 = findViewById(R.id.press_ave_btn_cpr_01);
        view01 = findViewById(R.id.depthCPR_view01);

        view01.post(() -> {
            depth_true = view01.getHeight();
            depth_over = view01.getHeight() + 50;
            depth_false = view01.getHeight() / 3;
        });

        LinearLayout layout100 = findViewById(R.id.cpr_layout100);
        LinearLayout layout120 = findViewById(R.id.cpr_layout120);

        FrameLayout positionLayout = findViewById(R.id.position_layout);
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.position_press);
        positionLayout.post(() -> {
            int layout2_width = layout120.getWidth();
            frame_width = positionLayout.getWidth();
            ViewGroup.LayoutParams btn_params = (ViewGroup.LayoutParams) press_ave_btn01.getLayoutParams();
            btn_params.width = frame_width / 9;
            press_ave_btn01.setLayoutParams(btn_params);
            press_width = press_ave_btn01.getWidth();

            frame_interval = (frame_width - press_width) / 4;
            int text_interval = (int) frame_interval + layout2_width / 2;

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout100.getLayoutParams();
            params.setMargins(text_interval, 0, 0, 0);
            layout100.setLayoutParams(params);
            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) layout120.getLayoutParams();
            params2.setMargins(0, 0, text_interval, 0);
            layout120.setLayoutParams(params2);
           /* LinearLayout.LayoutParams params_layout01 = (LinearLayout.LayoutParams)positionLayout.getLayoutParams();
            params_layout01.setMargins(frame_interval,0,frame_interval,0);
            positionLayout.setLayoutParams(params_layout01);*/

            div_interval = (float) frame_interval / (float) 10;

            //layerDrawable.setLayerInset(2, frame_interval, 5, frame_interval, 5);
            positionLayout.setBackground(layerDrawable);
        });

        depthCPR_view01.post(() -> {
            view01.setTranslationY(depthCPR_view01.getHeight() * -1.0f);
        });

        standard_btn01 = findViewById(R.id.standardCPR_btn01);

        lung01 = findViewById(R.id.lung01);
        test_lung01 = findViewById(R.id.test_lung01);
        lung_clip01 = (ClipDrawable) test_lung01.getDrawable();
        lung_clip01.setLevel(0);

        cpr_ani01 = findViewById(R.id.cpr_ani01);
        cpr_ani02 = findViewById(R.id.cpr_ani02);

        babyCircle = findViewById(R.id.babyCircle);

        backPressCloseHandler = new BackPressCloseHandler(this);

        activity = this;
        mHandler = new Handler(Looper.getMainLooper());

        setChildEventListener();
        setConferenceEventListener();

        locationPermissionCheck();

        if (permissionCheck()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1001);
        }

        String Device01 = sharedPreferences.getString("DeviceCPR_01", "-");
        String Device02 = sharedPreferences.getString("DeviceCPR_02", "-");

        if (!Device01.equals("-"))
            Devices.put("Device_01", Device01);
        if (!Device02.equals("-"))
            Devices.put("Device_02", Device02);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeServiceCPR.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        if (Device02.equals("-")) {
            showAlertDialog();
        } else {
            showScanDialog(room, UserName);
        }

        remote_arrow_down_text = findViewById(R.id.remote_arrow_down_text);
        remote_arrow_up_text = findViewById(R.id.remote_arrow_up_text);
        remote_depth_text = findViewById(R.id.remote_depth_text);

        angle_remote = findViewById(R.id.angle_remote);
        // Initialize default options for Jitsi Meet conferences.
        initialize_jitsi();

        databaseReference.child("Room").child(room).child("트레이너").limitToLast(10).addChildEventListener(childEventListener);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        video_conferenceBtn.setOnClickListener(v -> {
            JitsiMeetActivity.launch(CPRActivity.this, latestOptions);
        });

    }

    private int mLevel01 = 0;
    private int fromLevel01 = 0;
    private int toLevel01 = 0;
    private boolean isPeak = false;
    private boolean isPeakfinish = true;
    public static final int MAX_LEVEL = 10000;
    public static final int LEVEL_DIFF_UP = 500;
    public static final int LEVEL_DIFF_DOWN = 250;
    public static final int DELAY = 2;

    private Handler mUpHandler01 = new Handler(Looper.getMainLooper());

    private Runnable animateUpImage01 = () -> doTheUpAnimation01(fromLevel01, toLevel01);

    private Handler mDownHandler01 = new Handler(Looper.getMainLooper());
    private Runnable animateDownImage01 = () -> doTheDownAnimation01(fromLevel01, toLevel01);

    private void doTheUpAnimation01(int fromLevel, int toLevel) {
        mLevel01 += LEVEL_DIFF_UP;
        lung_clip01.setLevel(mLevel01);
        if (mLevel01 <= toLevel) {
            mUpHandler01.postDelayed(animateUpImage01, DELAY);
        } else {
            mUpHandler01.removeCallbacks(animateUpImage01);
            fromLevel01 = toLevel01;
        }
    }

    private void doTheDownAnimation01(int fromLevel, int toLevel) {
        mLevel01 -= LEVEL_DIFF_DOWN;
        lung_clip01.setLevel(mLevel01);
        if (mLevel01 >= toLevel) {
            mDownHandler01.postDelayed(animateDownImage01, DELAY);
        } else {
            mDownHandler01.removeCallbacks(animateDownImage01);
            fromLevel01 = toLevel01;
            isPeakfinish = true;
        }
    }

    public void moveLungClip01(int percent) {
        int temp_level = (percent * MAX_LEVEL) / 100;
        if (toLevel01 == temp_level || temp_level > MAX_LEVEL) {
            return;
        }
        toLevel01 = temp_level;
        if (toLevel01 > fromLevel01 && toLevel01 > 4000) {
            // cancel previous process first
            mDownHandler01.removeCallbacks(animateDownImage01);
            fromLevel01 = toLevel01;
            mUpHandler01.post(animateUpImage01);
        } else {
            // cancel previous process first
            mUpHandler01.removeCallbacks(animateUpImage01);
            fromLevel01 = toLevel01;
            mDownHandler01.post(animateDownImage01);
        }
    }

    public void peakLungClip01() {
        isPeakfinish = false;
        Handler handler = new Handler(Looper.getMainLooper());
        Handler handler2 = new Handler(Looper.getMainLooper());

        handler.postDelayed(() -> {
            mUpHandler01.removeCallbacks(animateUpImage01);
            toLevel01 = 0;
            fromLevel01 = toLevel01;
            mDownHandler01.post(animateDownImage01);
        }, 500);

        handler2.postDelayed(() -> {
            isPeak = false;
        }, 1000);
    }

    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);
            switch (event.getType()) {
                case CONFERENCE_TERMINATED:
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        //Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
        JitsiMeetActivityDelegate.onBackPressed();
        /*bluetoothLeServiceCPR.notiDisposable.clear();
        bluetoothLeServiceCPR.isCharDRegistereds.set(0, false);
        bluetoothLeServiceCPR.isCharDRegistereds.set(1, false);
        bluetoothLeServiceCPR.isCharARegistereds.set(0, false);
        bluetoothLeServiceCPR.isCharARegistereds.set(1, false);*/
        try {
            bluetoothLeServiceCPR.broadCastRxConnectionUpdate(bleDevice1, 0);
            Log.e(TAG, "bleDevice1_broadCastRxConnectionUpdate");
        } catch (Exception e) {

        }
        try {
            bluetoothLeServiceCPR.broadCastRxConnectionUpdate(bleDevice2, 1);
            Log.e(TAG, "bleDevice2_broadCastRxConnectionUpdate");
        } catch (Exception e) {

        }
    }

    private void disconnectJitsi() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }

    private void initialize_jitsi() {
        JitsiMeetUserInfo info = new JitsiMeetUserInfo();
        info.setDisplayName(UserName);
        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                // Different features flags can be set
                // .setFeatureFlag("toolbox.enabled", false)
                // .setFeatureFlag("filmstrip.enabled", false)
                .setUserInfo(info)
                .setFeatureFlag("call-integration.enabled", false)
                .setFeatureFlag("prejoinpage.enabled", false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        registerForBroadcastMessages();
    }

    class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String getTime = sdf.format(date);
            /*ChatData chatData = new ChatData("아웃/" + UserName, getTime, UserName);
            databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
            databaseReference.child("Room").child(room).child("into").child(UserName).setValue(null);
            databaseReference.child("Room").child(room).child("message").child(UserName).setValue(null);*/
            e.printStackTrace();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        startBackgroundThread();

        /*if (cameraView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            cameraView.setSurfaceTextureListener(textureListener);
        }*/
    }

    private final static String expression = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

    public static String getYoutubeId(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().length() <= 0) {
            return null;
        }
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(videoUrl);
        try {
            if (matcher.find())
                return matcher.group();
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void showLung00() {
        lung01.setImageResource(R.drawable.lung0);
    }

    private void showLung01() {
        lung01.setImageResource(R.drawable.lung1);
    }

    private void showLung02() {
        lung01.setImageResource(R.drawable.lung2);
    }

    private void showLung03() {
        lung01.setImageResource(R.drawable.lung3);
    }

    private void showLung04() {
        lung01.setImageResource(R.drawable.lung4);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void displayData(String data) { //TODO DATA
        if (data != null) {
            String[] spil = data.split(",");
            if (Devices.get("Device_01").equals(spil[2])) {
                switch (spil[1]) {
                    case "0000fff1-0000-1000-8000-00805f9b34fb":
                        if (!device2_connect) {
                            long now_ = System.currentTimeMillis();
                            Date date_ = new Date(now_);
                            SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                            String getTime_ = sdf_.format(date_);
                            ChatData chatData_ = new ChatData("Depth/" + getHexToDec(spil[0]), getTime_, UserName);
                            databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);
                            lung01.setVisibility(View.INVISIBLE);

                            final int depthSet = Integer.parseInt(getHexToDec(spil[0]));
                            final int viewDepth = depthSet;

                            //String[] timer_sec = cpr_timer.getText().toString().split(":");

                            intrval_01 = System.currentTimeMillis();

                            Animation animation = new TranslateAnimation(0, 0, 0, depth_true);

                            new Thread(() -> runOnUiThread(() -> {
                                int value;
                                if (depthSet >= 70)
                                    value = 70;
                                else
                                    value = depthSet;
                                remote_depth_text.setText(String.valueOf(value));
                                if ((0 < depthSet && depthSet < minDepth) || (maxDepth < depthSet)) {
                                    if (!start_check)
                                        cprItem_01.add(new UserItem(Seconds_, depthSet, 0, angle01, position01));
                                    view01.setBackgroundColor(Color.parseColor("#FF4D4D"));
                                } else if (viewDepth >= minDepth && viewDepth <= maxDepth) {
                                    if (!start_check)
                                        cprItem_01.add(new UserItem(Seconds_, 0, depthSet, angle01, position01));
                                    view01.setBackgroundColor(Color.parseColor("#4AFF5E"));
                                }
                            })).start();

                            if (!start_check) {
                                int Depth_correct_sum01 = 0;
                                int Depth_size = 0;

                                for (UserItem userItem : cprItem_01) {
                                    if (userItem.getDepth_correct() != 0)
                                        Depth_correct_sum01 = Depth_correct_sum01 + 1;
                                    if (userItem.getDepth_correct() != 0 || userItem.getDepth() != 0)
                                        Depth_size = Depth_size + 1;
                                }

                                correctCount.setText(String.valueOf(Depth_correct_sum01));
                                totalCount.setText(String.valueOf(Depth_size));
                                total_count = Depth_size;
                                while (peakTimes.size() > 2) {
                                    peakTimes.remove(0);
                                }
                                long now = System.currentTimeMillis();
                                now -= now % 10;
                                peakTimes.add(now);
                                setBpm();

                                float presstime = (float) ((now - StartTime_L) / 1000.0f);
                                presstime_list01.add(presstime);

                                if (!mode_cpr.isChecked()) {
                                    cycle_01 = Depth_size / cycle_set;
                                }
                                if (cprItem_01.size() != 0) {
                                    if (!start_check) {
                                        score_01 = (int) (((double) Depth_correct_sum01 / (double) Depth_size) * 100);
                                        depth_correct = Depth_correct_sum01;
                                        depth_num = Depth_size;
                                    }
                                }
                            }

                            if (!isReversed) {
                                if ((0 < depthSet) && (depthSet <= minDepth)) {
                                    animation = new TranslateAnimation(0, 0, 0, depth_false);
                                }
                                if (depthSet >= maxDepth) {
                                    animation = new TranslateAnimation(0, 0, 0, depth_over);
                                }


                                animation.setDuration(350);
                                animation.setFillAfter(false);
                                animation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        if (depthSet > minDepth && depthSet < maxDepth) {
                                            standard_btn01.setBackground(getDrawable(R.drawable.anne_point_green));
                                        } else if ((0 < depthSet && depthSet <= minDepth) || (maxDepth <= depthSet)) {
                                            standard_btn01.setBackground(getDrawable(R.drawable.anne_point_red));
                                        }
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        final Animation animation1 = new TranslateAnimation(0, 0, 0, 0);
                                        animation1.setDuration(200);
                                        animation1.setFillAfter(false);

                                        view01.setBackgroundColor(Color.parseColor("#777777"));
                                        standard_btn01.setBackground(getDrawable(R.drawable.anne_point));
                                        depth_btn01.startAnimation(animation1);
                                        view01.setAnimation(animation1);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                if (depthSet >= minDepth && depthSet <= maxDepth) {
                                    Animation animation1 = new TranslateAnimation(0, cpr_ani01.getWidth() + 100, 0, 0);
                                    animation1.setDuration(550);
                                    animation1.setFillAfter(false);
                                    animation1.setInterpolator(new AccelerateDecelerateInterpolator());

                                    Animation animation2 = new TranslateAnimation(0, -cpr_ani02.getWidth() - 100, 0, 0);
                                    animation2.setDuration(550);
                                    animation2.setFillAfter(false);
                                    animation2.setInterpolator(new AccelerateDecelerateInterpolator());

                                    cpr_ani01.startAnimation(animation1);
                                    cpr_ani02.startAnimation(animation2);
                                }
                                depth_btn01.startAnimation(animation);
                                view01.startAnimation(animation);
                            }
                        }

                        break;

                    case "0000fff2-0000-1000-8000-00805f9b34fb":
                        angle01 = Integer.parseInt(getHexToDec(spil[0]));
                        if (angle01 <= 30) {
                            angle_remote.setImageResource(R.drawable.angle_green);
                        } else if (angle01 <= 60) {
                            angle_remote.setImageResource(R.drawable.angle_orange);
                        } else {
                            angle_remote.setImageResource(R.drawable.angle_red);
                        }

                        if (angle01 > pre_angle + 2 || angle01 < pre_angle - 2) {
                            long now_ = System.currentTimeMillis();
                            Date date_ = new Date(now_);
                            SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                            String getTime_ = sdf_.format(date_);
                            ChatData chatData_ = new ChatData("Angle/" + angle01, getTime_, UserName);
                            databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);
                            pre_angle = angle01;
                        }

                        break;
                }

            } else if (Devices.get("Device_02").equals(spil[2])) {
                long now = System.currentTimeMillis();
                if (!device2_connect)
                    device2_connect = true;
                switch (spil[1]) {
                    case "0000fff1-0000-1000-8000-00805f9b34fb":
                        if (!isReversed) {
                            lung01.setVisibility(View.INVISIBLE);
                            press_position.setVisibility(View.VISIBLE);
                            test_lung01.setVisibility(View.INVISIBLE);
                        }
                        intrval_01 = System.currentTimeMillis();
                        position01 = Integer.parseInt(getHexToDec(spil[0]));
                        Log.e(TAG, "position = " + position01);

                        Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                        String getTime = sdf.format(date);
                        ChatData chatData = new ChatData("Position/" + position01, getTime, UserName);
                        databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);

                        //isBreath01 = false;
                        switch (position01) {
                            case 1:
                                final Animation animation0 = new TranslateAnimation(0, -100, 0, 0);
                                animation0.setDuration(200);
                                animation0.setFillAfter(false);
                                press_point_btn.startAnimation(animation0);
                                if (!start_check) {
                                    position_num01++;
                                }
                                cpr_arrow01.setVisibility(View.INVISIBLE);
                                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                                break;
                            case 2:
                                final Animation animation2 = new TranslateAnimation(0, 100, 0, 0);
                                animation2.setDuration(200);
                                animation2.setFillAfter(false);
                                press_point_btn.startAnimation(animation2);
                                if (!start_check) {
                                    position_num01++;
                                }
                                cpr_arrow01.setVisibility(View.INVISIBLE);
                                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                                break;
                            case 3:
                                final Animation animation3 = new TranslateAnimation(0, 0, 0, -100);
                                animation3.setDuration(200);
                                animation3.setFillAfter(false);
                                press_point_btn.startAnimation(animation3);
                                if (!start_check) {
                                    position_num01++;
                                }
                                cpr_arrow01.setVisibility(View.INVISIBLE);
                                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                                break;
                            case 4:
                                final Animation animation4 = new TranslateAnimation(0, 0, 0, 100);
                                animation4.setDuration(200);
                                animation4.setFillAfter(false);
                                press_point_btn.startAnimation(animation4);
                                if (!start_check) {
                                    position_num01++;
                                }
                                cpr_arrow01.setVisibility(View.INVISIBLE);
                                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                                break;
                            case 5:
                                final Animation animation5 = new TranslateAnimation(0, 0, 0, 0);
                                animation5.setDuration(200);
                                animation5.setFillAfter(false);
                                press_point_btn.startAnimation(animation5);
                                if (!start_check) {
                                    position_num01++;
                                    position_correct01++;
                                }
                                cpr_arrow01.setVisibility(View.INVISIBLE);
                                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                                break;
                            case 7:
                                final Animation animation7 = new TranslateAnimation(0, -100, 0, 0);
                                animation7.setDuration(200);
                                animation7.setFillAfter(false);
                                press_point_btn.startAnimation(animation7);
                                if (!start_check)
                                    position_num01++;
                                cpr_arrow01.setVisibility(View.VISIBLE);
                                remote_arrow_up_text.setVisibility(View.VISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                                break;
                            case 8:
                                final Animation animation8 = new TranslateAnimation(0, 100, 0, 0);
                                animation8.setDuration(200);
                                animation8.setFillAfter(false);
                                press_point_btn.startAnimation(animation8);
                                if (!start_check)
                                    position_num01++;
                                cpr_arrow01.setVisibility(View.VISIBLE);
                                remote_arrow_up_text.setVisibility(View.VISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                                break;
                            case 9:
                                final Animation animation9 = new TranslateAnimation(0, 0, 0, -100);
                                animation9.setDuration(200);
                                animation9.setFillAfter(false);
                                press_point_btn.startAnimation(animation9);
                                if (!start_check)
                                    position_num01++;
                                cpr_arrow01.setVisibility(View.VISIBLE);
                                remote_arrow_up_text.setVisibility(View.VISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                                break;
                            case 10:
                                final Animation animation10 = new TranslateAnimation(0, 0, 0, 100);
                                animation10.setDuration(200);
                                animation10.setFillAfter(false);
                                press_point_btn.startAnimation(animation10);
                                if (!start_check)
                                    position_num01++;
                                cpr_arrow01.setVisibility(View.VISIBLE);
                                remote_arrow_up_text.setVisibility(View.VISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                                break;
                            case 11:
                                final Animation animation11 = new TranslateAnimation(0, 0, 0, 0);
                                animation11.setDuration(200);
                                animation11.setFillAfter(false);
                                press_point_btn.startAnimation(animation11);
                                if (!start_check) {
                                    position_num01++;
                                    position_correct01++;
                                }
                                cpr_arrow01.setVisibility(View.VISIBLE);
                                remote_arrow_up_text.setVisibility(View.VISIBLE);
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                                break;
                            case 225:
                                isAdult = true;
                                anne.setImageResource(R.drawable.anne);
                                babyCircle.setVisibility(View.GONE);
                                remote_depth_text.setVisibility(View.VISIBLE);
                                break;
                            case 226:
                                isAdult = false;
                                anne.setImageResource(R.drawable.baby01);
                                babyCircle.setVisibility(View.GONE);
                                remote_depth_text.setVisibility(View.INVISIBLE);
                                break;
                            case 233:
                                anne.setImageResource(R.drawable.baby02);
                                babyCircle.setVisibility(View.GONE);
                                isReversed = true;
                                anne.setVisibility(View.VISIBLE);
                                remote_depth_text.setVisibility(View.INVISIBLE);
                                cpr_ani01.setVisibility(View.INVISIBLE);
                                cpr_ani02.setVisibility(View.INVISIBLE);
                                standardCPR_btn01.setVisibility(View.INVISIBLE);
                                depth_btn01.setVisibility(View.INVISIBLE);
                                depth_btn_cpr_up.setVisibility(View.INVISIBLE);
                                depthCPR_view01.setVisibility(View.INVISIBLE);
                                lung01.setVisibility(View.INVISIBLE);
                                test_lung01.setVisibility(View.INVISIBLE);
                                break;
                            case 234:
                                anne.setImageResource(R.drawable.baby01);
                                babyCircle.setVisibility(View.GONE);
                                isReversed = false;
                                break;
                            case 235:
                                if (isReversed) {
                                    babyCircle.setVisibility(View.VISIBLE);
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> babyCircle.setVisibility(View.GONE), 1000);
                                }
                                break;

                        }
                        break;

                    case "0000fff2-0000-1000-8000-00805f9b34fb":
                        if (!start_check) {
                            long secs = System.currentTimeMillis() - intrval_01;
                            now = now - (now % 10);
                            float current_time = (float) ((now - StartTime_L) / 1000.0f);

                            double breath = breath01 - min_lung01;
                            double percent = ((float) breath / gap_lung01) * 100;
                            if (secs >= 1500) {
                                breath01 = Integer.parseInt(getHexToDec(spil[0]));
                                if (isPeak) {
                                    breath01 = bre_threshold01 - 3;
                                }

                                if (breath01 > bre_threshold01 && !isBreath01) {
                                    breathval_01.add(71.0f);
                                    breathtime_01.add(current_time);
                                    isBreath01 = true;
                                }

                                if (cycle_01 == Integer.parseInt(mode_cpr_value.getText().toString())) {
                                    reset(1);
                                }

                                if (breath01 > min_lung01 + 5) {
                                    if (!isReversed) {
                                        cpr_arrow01_.setVisibility(View.INVISIBLE);
                                        remote_arrow_down_text.setVisibility(View.INVISIBLE);
                                        lung01.setVisibility(View.VISIBLE);
                                        test_lung01.setVisibility(View.VISIBLE);
                                        anne.setVisibility(View.INVISIBLE);
                                        remote_depth_text.setVisibility(View.INVISIBLE);
                                        cpr_ani01.setVisibility(View.INVISIBLE);
                                        cpr_ani02.setVisibility(View.INVISIBLE);
                                        standardCPR_btn01.setVisibility(View.INVISIBLE);
                                        depth_btn01.setVisibility(View.INVISIBLE);
                                        depth_btn_cpr_up.setVisibility(View.INVISIBLE);
                                        depthCPR_view01.setVisibility(View.INVISIBLE);
                                        press_position.setVisibility(View.INVISIBLE);
                                    }
                                }

                                if (breath01 < bre_threshold01) {
                                    isBreOver01 = false;
                                    isBreBelow01 = false;
                                    over_breath01 = 0;
                                } else if (breath01 >= bre_threshold01 && breath01 < bre_level01) {
                                    isBreOver01 = false;
                                    isBreBelow01 = false;
                                    over_breath01 = 0;
                                } else if (breath01 >= bre_level01 && breath01 < bre_level02) {
                                    isBreOver01 = false;
                                    isBreBelow01 = false;
                                    over_breath01 = 0;
                                } else if (breath01 >= bre_level02 && over_breath01 < BREOVERTIME) {
                                    isBreOver01 = false;
                                    isBreBelow01 = false;
                                    over_breath01++;
                                } else if (breath01 >= bre_level02 && over_breath01 >= BREOVERTIME) {
                                    isBreOver01 = true;
                                    isBreBelow01 = false;
                                }

                                if (isAdult) {
                                    int size = lung_list01.size();
                                    if (size != 0) {
                                        if (breath01 < lung_list01.get(size - 1) - 1 || breath01 > lung_list01.get(size - 1) + 1) {
                                            lung_list01.add(breath01);
                                            lungtime_list01.add(current_time);
                                        }
                                    } else {
                                        lung_list01.add(breath01);
                                        lungtime_list01.add(current_time);
                                    }

                                    if (lung_list01.size() > 3) {
                                        lung_list01.remove(0);
                                        lungtime_list01.remove(0);
                                    }

                                    if (lung_list01.size() == 3) {
                                        int left = lung_list01.get(0);
                                        int center = lung_list01.get(1);
                                        int right = lung_list01.get(2);
                                        if (center > left + 1 && center > right + 1) {
                                            isPeak = true;
                                            int max = lung_list01.get(0);
                                            float time = lungtime_list01.get(0);
                                            for (int i = 1; i < lung_list01.size(); i++) {
                                                if (lung_list01.get(i) > lung_list01.get(i - 1)) {
                                                    max = lung_list01.get(i);
                                                    time = lungtime_list01.get(i);
                                                }
                                            }
                                            setBreath01(max, time);
                                            lung_list01.clear();
                                            lungtime_list01.clear();
                                        }
                                    }

                                    if (breath01 < bre_threshold01 && isBreath01) {
                                        breathval_01.add(71.0f);
                                        breathtime_01.add(current_time);
                                        isBreath01 = false;
                                    }

                                    if (isBreOver01) {
                                        if (isImageNormal01) {
                                            int level = lung_clip01.getLevel();
                                            lung_clip01 = (ClipDrawable) getDrawable(R.drawable.lung_over_clip);
                                            test_lung01.setImageDrawable(lung_clip01);
                                            lung_clip01.setLevel(level);
                                            isImageNormal01 = false;
                                        }
                                    } else if (isBreBelow01) {
                                        if (isImageNormal01) {
                                            int level = lung_clip01.getLevel();
                                            lung_clip01 = (ClipDrawable) getDrawable(R.drawable.lung_over_clip);
                                            test_lung01.setImageDrawable(lung_clip01);
                                            lung_clip01.setLevel(level);
                                            isImageNormal01 = false;
                                        }
                                    } else {
                                        if (!isImageNormal01) {
                                            int level = lung_clip01.getLevel();
                                            lung_clip01 = (ClipDrawable) getDrawable(R.drawable.lung_normal_clip);
                                            test_lung01.setImageDrawable(lung_clip01);
                                            lung_clip01.setLevel(level);
                                            isImageNormal01 = true;
                                        }
                                    }

                                    if (gap_lung01 != 0 && !isPeak) {
                                        if (percent > 100) {
                                            percent = 100;
                                        }
                                        if (percent < 0) {
                                            percent = 0;
                                        }
                                        moveLungClip01((int) percent);
                                    }

                                    if (gap_lung01 != 0 && isPeak) {
                                        if (percent > 100)
                                            percent = 100;

                                        if (isPeakfinish)
                                            peakLungClip01();
                                    }
                                } else {
                                    if (!isLungDrawing) {
                                        if (breath >= 82) {
                                            cpr_arrow01_.setVisibility(View.INVISIBLE);
                                            remote_arrow_down_text.setVisibility(View.INVISIBLE);
                                            lung01.setVisibility(View.VISIBLE);
                                            test_lung01.setVisibility(View.VISIBLE);
                                            anne.setVisibility(View.INVISIBLE);
                                            remote_depth_text.setVisibility(View.INVISIBLE);
                                            cpr_ani01.setVisibility(View.INVISIBLE);
                                            cpr_ani02.setVisibility(View.INVISIBLE);
                                            standardCPR_btn01.setVisibility(View.INVISIBLE);
                                            depth_btn01.setVisibility(View.INVISIBLE);
                                            depth_btn_cpr_up.setVisibility(View.INVISIBLE);
                                            depthCPR_view01.setVisibility(View.INVISIBLE);
                                            press_position.setVisibility(View.INVISIBLE);

                                            lung_clip01 = (ClipDrawable) getDrawable(R.drawable.lung_normal_clip);
                                            test_lung01.setImageDrawable(lung_clip01);
                                            lung_clip01.setLevel(10000);
                                            isLungDrawing = true;
                                            moveLungClip01(100);
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                moveLungClip01(0);
                                                isLungDrawing = false;
                                            }, 1000);
                                        }
                                    }
                                }

                                long now_ = System.currentTimeMillis();
                                Date date_ = new Date(now_);
                                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                                String getTime_ = sdf_.format(date_);
                                ChatData chatData_ = new ChatData("breath/" + breath01, getTime_, UserName);
                                databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);
                            }
                        } else {
                            if (!isAdult) {
                                int breath = Integer.parseInt(getHexToDec(spil[0]));
                                if (!isLungDrawing) {
                                    if (breath >= 82) {
                                        cpr_arrow01_.setVisibility(View.INVISIBLE);
                                        remote_arrow_down_text.setVisibility(View.INVISIBLE);
                                        lung01.setVisibility(View.VISIBLE);
                                        test_lung01.setVisibility(View.VISIBLE);
                                        anne.setVisibility(View.INVISIBLE);
                                        remote_depth_text.setVisibility(View.INVISIBLE);
                                        cpr_ani01.setVisibility(View.INVISIBLE);
                                        cpr_ani02.setVisibility(View.INVISIBLE);
                                        standardCPR_btn01.setVisibility(View.INVISIBLE);
                                        depth_btn01.setVisibility(View.INVISIBLE);
                                        depth_btn_cpr_up.setVisibility(View.INVISIBLE);
                                        depthCPR_view01.setVisibility(View.INVISIBLE);
                                        press_position.setVisibility(View.INVISIBLE);

                                        lung_clip01 = (ClipDrawable) getDrawable(R.drawable.lung_normal_clip);
                                        test_lung01.setImageDrawable(lung_clip01);
                                        lung_clip01.setLevel(10000);
                                        isLungDrawing = true;
                                        moveLungClip01((int) 100);
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            moveLungClip01(0);
                                            isLungDrawing = false;
                                        }, 1000);
                                    }
                                }
                                long now_ = System.currentTimeMillis();
                                Date date_ = new Date(now_);
                                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                                String getTime_ = sdf_.format(date_);
                                ChatData chatData_ = new ChatData("breath/" + breath, getTime_, UserName);
                                databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);
                            }
                        }
                        break;

                    case "0000fff3-0000-1000-8000-00805f9b34fb":
                        if (!isReversed) {
                            anne.setVisibility(View.VISIBLE);
                            if (isAdult) {
                                remote_depth_text.setVisibility(View.VISIBLE);
                            }
                            cpr_ani01.setVisibility(View.VISIBLE);
                            cpr_ani02.setVisibility(View.VISIBLE);
                            standardCPR_btn01.setVisibility(View.VISIBLE);
                            depth_btn01.setVisibility(View.VISIBLE);
                            depth_btn_cpr_up.setVisibility(View.VISIBLE);
                            depthCPR_view01.setVisibility(View.VISIBLE);
                            lung01.setVisibility(View.INVISIBLE);
                            test_lung01.setVisibility(View.INVISIBLE);
                            lung_list01.clear();
                            lungtime_list01.clear();
                        }
                        long now__ = System.currentTimeMillis();
                        Date date__ = new Date(now__);
                        SimpleDateFormat sdf__ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                        String getTime__ = sdf__.format(date__);
                        ChatData chatData__ = new ChatData("Depth/" + getHexToDec(spil[0]), getTime__, UserName);
                        databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData__);


                        lung01.setVisibility(View.INVISIBLE);
                        test_lung01.setVisibility(View.INVISIBLE);
                        lung_list01.clear();
                        lungtime_list01.clear();

                        final int depthSet = Integer.parseInt(getHexToDec(spil[0]));

                        String[] timer_sec = cpr_timer.getText().toString().split(":");

                        intrval_01 = System.currentTimeMillis();

                        if (!isAdult) {
                            minDepth = 30;
                            maxDepth = 40;
                        }

                        Animation animation = new TranslateAnimation(0, 0, 0, depth_true);

                        new Thread(() -> runOnUiThread(() -> {
                            int value;
                            if (depthSet >= 70)
                                value = 70;
                            else
                                value = depthSet;
                            remote_depth_text.setText(String.valueOf(value));
                            if ((0 < depthSet && depthSet < minDepth) || (maxDepth < depthSet)) {
                                if (!start_check) {
                                    cprItem_01.add(new UserItem(Seconds_, depthSet, 0, angle01, position01));
                                }
                                view01.setBackgroundColor(Color.parseColor("#FF4D4D"));
                            } else if (depthSet >= minDepth && depthSet <= maxDepth) {
                                if (!start_check)
                                    cprItem_01.add(new UserItem(Seconds_, 0, depthSet, angle01, position01));
                                view01.setBackgroundColor(Color.parseColor("#4AFF5E"));
                            }

                        })).start();

                        if (!start_check) {
                            int Depth_correct_sum01 = 0;
                            int Depth_size = 0;

                            for (UserItem userItem : cprItem_01) {
                                if (userItem.getDepth_correct() != 0)
                                    Depth_correct_sum01 = Depth_correct_sum01 + 1;
                                if (userItem.getDepth_correct() != 0 || userItem.getDepth() != 0)
                                    Depth_size = Depth_size + 1;
                            }

                            correctCount.setText(String.valueOf(Depth_correct_sum01));
                            totalCount.setText(String.valueOf(Depth_size));
                            total_count = Depth_size;
                            while (peakTimes.size() > 2) {
                                peakTimes.remove(0);
                            }
                            long now2 = System.currentTimeMillis();
                            now2 = now2 - (now2 % 10);
                            peakTimes.add(now2);
                            setBpm();
                            float presstime = (float) ((now2 - StartTime_L) / 1000.0f);
                            presstime_list01.add(presstime);
                            if (!mode_cpr.isChecked()) {
                                cycle_01 = Depth_size / cycle_set;
                            }

                            if (cprItem_01.size() != 0) {
                                if (!start_check) {
                                    score_01 = (int) (((double) Depth_correct_sum01 / (double) Depth_size) * 100);
                                    depth_correct = Depth_correct_sum01;
                                    depth_num = Depth_size;
                                }
                            }
                        }
                        if (!isReversed) {
                            if (depthSet >= minDepth && depthSet <= maxDepth)
                                animation = new TranslateAnimation(0, 0, 0, depth_true);
                            if (depthSet < minDepth) {
                                animation = new TranslateAnimation(0, 0, 0, depth_false);
                            }
                            if (depthSet > maxDepth) {
                                animation = new TranslateAnimation(0, 0, 0, depth_over);
                            }

                            animation.setDuration(350);
                            animation.setFillAfter(true);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    if (depthSet > minDepth && depthSet < maxDepth) {
                                        standard_btn01.setBackground(getDrawable(R.drawable.anne_point_green));
                                    } else if ((0 < depthSet && depthSet <= minDepth) || (maxDepth <= depthSet)) {
                                        standard_btn01.setBackground(getDrawable(R.drawable.anne_point_red));
                                    }
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    final Animation animation1 = new TranslateAnimation(0, 0, 0, 0);
                                    animation1.setDuration(200);
                                    animation1.setFillAfter(false);

                                    view01.setBackgroundColor(Color.parseColor("#777777"));
                                    standard_btn01.setBackground(getDrawable(R.drawable.anne_point));
                                    depth_btn01.startAnimation(animation1);
                                    view01.startAnimation(animation1);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });

                            if (depthSet >= minDepth && depthSet <= maxDepth) {
                                Animation animation1 = new TranslateAnimation(0, cpr_ani01.getWidth() + 100, 0, 0);
                                animation1.setDuration(550);
                                animation1.setFillAfter(false);
                                animation1.setInterpolator(new AccelerateDecelerateInterpolator());

                                Animation animation2 = new TranslateAnimation(0, -cpr_ani02.getWidth() - 100, 0, 0);
                                animation2.setDuration(550);
                                animation2.setFillAfter(false);
                                animation2.setInterpolator(new AccelerateDecelerateInterpolator());

                                cpr_ani01.startAnimation(animation1);
                                cpr_ani02.startAnimation(animation2);
                            }

                            depth_btn01.startAnimation(animation);
                            view01.startAnimation(animation);
                        }
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + spil[1]);
                }
            }
        }
    }

    private void setBreath01(int center, float centertime) {
        int breath = 0;
        if (center < bre_threshold01) {
            breath = 0;
        } else if (center >= bre_threshold01 && center < bre_level01) {
            breath = 1;
        } else if (center >= bre_level01 && center < bre_level02) {
            breath = 2;
        } else if (center >= bre_level02 && over_breath01 < BREOVERTIME) {
            breath = 3;
        } else if (center >= bre_level02 && over_breath01 >= BREOVERTIME) {
            breath = 4;
        }
        if (breath != 0) {
            cprItem_01.add(new UserItem(Seconds_, breath));
            float percent = (float) ((((double) center - min_lung01) / gap_lung01) * 100.0f);
            // 158% = 550ml
            ventil_volume_01 += percent * (550.0f / 158.0f);
            percent = (float) ((((double) center - min_lung01) / gap_lung01) * 60.0f);
            if (percent < 0)
                percent = 0;
            if (percent > 60)
                percent = 60;
            percent = (percent - 70.0f) * (-1.0f);
            switch (breath) {
                case 1:
                    lung_num01++;
                    if (breathval_01.get(breathval_01.size() - 1) != 200.0f && breathval_01.get(breathval_01.size() - 1) != 71.0f) {
                        breathval_01.add(60.0f);
                        breathtime_01.add((breathtime_01.get(breathtime_01.size() - 1) + centertime) / 2);
                    }
                    breathval_01.add(percent * (-1.0f) - 5.0f);
                    breathtime_01.add(centertime);

                    break;
                case 4:
                    lung_num01++;
                    if (breathval_01.get(breathval_01.size() - 1) != 200.0f && breathval_01.get(breathval_01.size() - 1) != 71.0f) {
                        breathval_01.add(60.0f);
                        breathtime_01.add((breathtime_01.get(breathtime_01.size() - 1) + centertime) / 2);
                    }
                    breathval_01.add(percent * (-1.0f));
                    breathtime_01.add(centertime);

                    break;
                case 2:
                case 3:
                    lung_num01++;
                    lung_correct01++;
                    if (breathval_01.get(breathval_01.size() - 1) != 200.0f && breathval_01.get(breathval_01.size() - 1) != 71.0f) {
                        breathval_01.add(60.0f);
                        breathtime_01.add((breathtime_01.get(breathtime_01.size() - 1) + centertime) / 2);
                    }
                    breathval_01.add(percent);
                    breathtime_01.add(centertime);

                    break;
            }

            int breath_sum01 = 0;
            for (UserItem userItem : cprItem_01) {
                if (userItem.getBreath() != 0)
                    breath_sum01 = breath_sum01 + 1;
            }

            if (mode_cpr.isChecked()) {
                cycle_01 = breath_sum01 / breath_set;
            }
        }
    }

    void setBpm() {
        float currentBpm = 0f;
        int peak_size = peakTimes.size();

        if (peakTimes != null && peak_size > 1) {
            long lastPeakTime = peakTimes.get(0);
            if (System.currentTimeMillis() - lastPeakTime <= 1500) {
                float interval = (float) (peakTimes.get(1) - peakTimes.get(0));
                while (tmp_bpm1.size() > 4) {
                    tmp_bpm1.remove(0);
                }
                tmp_bpm1.add((60_000f / interval));
            } else {
                try {
                    if (peakTimes != null) {
                        peakTimes.clear();
                        tmp_bpm1.clear();
                    }
                } catch (Exception e) {
                }
            }
        }

        if (!tmp_bpm1.isEmpty() && tmp_bpm1.size() > 4) {
            float tmp_bpm = 0;
            for (float bpm : tmp_bpm1)
                tmp_bpm += bpm;
            currentBpm = tmp_bpm / tmp_bpm1.size();
            bpm1.add(currentBpm);
        }

        Animation animation = null;
        if (currentBpm != 0) {
            Log.e("bpm", String.valueOf(frame_width));
            if (currentBpm > 140) {
                float XDelta = (float) frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else if (currentBpm >= 120) {
                float XDelta = frame_interval * 3 + (currentBpm - 120) * div_interval;
                if (XDelta > frame_width - press_width)
                    XDelta = frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else if (currentBpm >= 110) {
                float XDelta = frame_interval * 2 + (currentBpm - 110) * div_interval;
                if (XDelta > frame_width - press_width)
                    XDelta = frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else if (currentBpm >= 100) {
                float XDelta = frame_interval + (currentBpm - 100) * div_interval;
                if (XDelta > frame_width - press_width)
                    XDelta = frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else {
                animation = new TranslateAnimation(position_bpm, currentBpm * div_interval / 10, 0, 0);
                position_bpm = currentBpm * div_interval / 10;
            }
            animation.setDuration(200);
            animation.setFillAfter(true);
            press_ave_btn01.startAnimation(animation);

            if (currentBpm > 120 || currentBpm < 100) {
                press_ave_btn01.setBackgroundResource(R.drawable.position_press_red);
            } else {
                press_ave_btn01.setBackgroundResource(R.drawable.position_press_green);
            }

        } else{
            animation = new TranslateAnimation(position_bpm, 0, 0, 0);
            animation.setDuration(400);
            animation.setFillAfter(true);
            position_bpm = 0;
            press_ave_btn01.startAnimation(animation);
            press_ave_btn01.setBackgroundResource(R.drawable.position_press_red);
        }

        long now_ = System.currentTimeMillis();
        Date date_ = new Date(now_);
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String getTime_ = sdf_.format(date_);
        ChatData chatData_ = new ChatData("Bpm/" + currentBpm, getTime_, UserName);
        databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeServiceCPR.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BluetoothLeServiceCPR.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeServiceCPR.ACTION_NEW_STATE);
        return intentFilter;
    }


    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<RxBleDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = CPRActivity.this.getLayoutInflater();
        }

        public void addDevice(RxBleDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public RxBleDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void remove(int position) {
            mLeDevices.remove(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override   //리스트뷰의 아이템을 가져옴
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            CPRActivity.ViewHolder viewHolder;

            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new CPRActivity.ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (CPRActivity.ViewHolder) view.getTag();
            }

            RxBleDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();

            viewHolder.deviceName.setText(deviceName);
            viewHolder.deviceAddress.setText(device.getMacAddress());
            viewHolder.deviceName.setTextColor(Color.parseColor("#ff5b00"));
            viewHolder.deviceAddress.setTextColor(Color.parseColor("#ff5b00"));

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    public static String getHexToDec(String hex) {
        ToBinary = new ToBinary(hex);
        return ToBinary.hexTobin();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public class BackPressCloseHandler {
        private final Activity activity;
        private long backKeyPressedTime;
        private Toast toast;

        public BackPressCloseHandler(Activity context) {
            this.backKeyPressedTime = 0;
            this.activity = context;
        }

        public void onBackPressed() {
            Log.e("mConnected", mConnected + "");
            Log.e("start_check", start_check + "");
            if (mConnected) {
                if (start_check) {
                    Log.e("back", "back");
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View view = inflater.inflate(R.layout.done_layout, null);
                    builder.setView(view);

                    final Button appbtnCancel = (Button) view.findViewById(R.id.appbtnCancel);
                    final Button appbtnExit = (Button) view.findViewById(R.id.appbtnExit);

                    final AlertDialog dialog = builder.create();

                    appbtnCancel.setOnClickListener(v -> dialog.dismiss());
                    appbtnExit.setOnClickListener(v -> {

                        Intent sender = new Intent(CPRActivity.this, BluetoothLeServiceCPR.class);
                        sender.setAction(BluetoothLeServiceCPR.ACTION_READY);
                        startService(sender);

                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                        String getTime = sdf.format(date);

                        hangUp();
                        LocalBroadcastManager.getInstance(CPRActivity.this).unregisterReceiver(broadcastReceiver);

                        ChatData chatData = new ChatData("아웃/" + UserName, getTime, UserName);
                        databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
                        databaseReference.child("Room").child(room).child("into").child(UserName).setValue(null);
                        databaseReference.child("Room").child(room).child("message").child(UserName).setValue(null);

                        reset(1);

                        dialog.dismiss();
                    });
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                } else {
                    Toast toast = Toast.makeText(CPRActivity.this, R.string.disconnect_device, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, Gravity.BOTTOM);
                    toast.show();
                }

            } else {
                if (System.currentTimeMillis() > this.backKeyPressedTime + 2000) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View view = inflater.inflate(R.layout.done_layout, null);
                    builder.setView(view);

                    final Button appbtnCancel = (Button) view.findViewById(R.id.appbtnCancel);
                    final Button appbtnExit = (Button) view.findViewById(R.id.appbtnExit);

                    final AlertDialog dialog = builder.create();

                    appbtnCancel.setOnClickListener(v -> dialog.dismiss());
                    appbtnExit.setOnClickListener(v -> {

                        sharedPreferences.edit().putString("video_uuid", "-").apply();
                        sharedPreferences.edit().putInt("onGoing", 0).apply();
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                        String getTime = sdf.format(date);
                        ChatData chatData = new ChatData("아웃/" + UserName, getTime, UserName);
                        databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
                        databaseReference.child("Room").child(room).child("into").child(UserName).setValue(null);
                        databaseReference.child("Room").child(room).child("message").child(UserName).setValue(null);
                        reset(1);

                        this.backKeyPressedTime = System.currentTimeMillis();
                        Intent main = new Intent(CPRActivity.this, LobbyActivity.class);
                        main.putExtra("Name", UserName);
                        startActivity(main);
                        finish();
                        overridePendingTransition(R.anim.fadeout, R.anim.fadein);

                        dialog.dismiss();
                    });

                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
            }
        }
    }

    NeumorphImageButton device_btn01;
    NeumorphImageButton device_btn02;
    int de1Connect = 0;
    int de2Connect = 0;

    private void connection_on(String address) {
        if (Objects.equals(Devices.get("Device_01"), address)) {
            if (device_btn01 != null)
                device_btn01.setImageResource(R.drawable.band_on);
            de1Connect = 1;
        } else if (Objects.equals(Devices.get("Device_02"), address)) {
            if (device_btn02 != null)
                device_btn02.setImageResource(R.drawable.cpr_on);
            device2_connect = true;
            de2Connect = 1;
        }
        mConnected = true;
    }

    private void showScanDialog(final String room_, final String UserName_) {
        isConnect = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(CPRActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cpr_scan_layout, null);
        builder.setView(view);

        //블루투스 활성화 체크
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                } else {
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        }

        // 어텝터 초기화

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mLeDeviceListAdapter02 = new LeDeviceListAdapter();
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter02.clear();
        scanLeDevice(true);

        String Device01 = sharedPreferences.getString("DeviceCPR_01", "-");
        String Device02 = sharedPreferences.getString("DeviceCPR_02", "-");

        Devices.put("Device_01", Device01);
        Devices.put("Device_02", Device02);

        final NeumorphButton band_dialog_reset = (NeumorphButton) view.findViewById(R.id.cpr_dialog_reset);
        final NeumorphButton band_dialog_layout = (NeumorphButton) view.findViewById(R.id.cpr_dialog_layout);
        device_btn01 = (NeumorphImageButton) view.findViewById(R.id.device_btn_cpr01);
        device_btn02 = (NeumorphImageButton) view.findViewById(R.id.device_btn_aio_01);

        final LinearLayout cpr_scan_reset = (LinearLayout) view.findViewById(R.id.cpr_scan_reset);

        band_dialog_reset.setVisibility(View.VISIBLE);
        band_dialog_layout.setVisibility(View.VISIBLE);

        if (!Devices.isEmpty()) {
            Log.e(TAG, "devices is not empty");
            List<BluetoothDevice> devices = bluetoothManager.getConnectedDevices(BluetoothGatt.GATT);
            int status = -1;
            for (BluetoothDevice device : devices) {
                status = bluetoothManager.getConnectionState(device, BluetoothGatt.GATT);
                if (status == BluetoothProfile.STATE_CONNECTED) {
                    if (Devices.get("Device_01").equals(device.getAddress())) {
                        mConnected = true;
                        device_btn01.setImageResource(R.drawable.band_on);
                        de1Connect = 1;
                    } else if (Devices.get("Device_02").equals(device.getAddress())) {
                        mConnected = true;
                        device_btn02.setImageResource(R.drawable.cpr_on);
                        initialize();
                        de2Connect = 1;
                        device2_connect = true;
                    }
                }
            }

            handler.postDelayed(() -> {
                for (RxBleDevice bluetoothDevice : mLeDeviceListAdapter.mLeDevices) {
                    String address = bluetoothDevice.getMacAddress();
                    Log.e(TAG, "address = " + address);
                    if (Objects.equals(Devices.get("Device_01"), address)) {
                        bluetoothLeServiceCPR.connect(address, 0);
                        bleDevice1 = rxBleClient.getBleDevice(address);
                    }
                }
                for (RxBleDevice bluetoothDevice : mLeDeviceListAdapter02.mLeDevices) {
                    String address = bluetoothDevice.getMacAddress();
                    Log.e(TAG, "address2 = " + address);
                    if (Objects.equals(Devices.get("Device_02"), address)) {
                        bluetoothLeServiceCPR.connect(address, 1);
                        bleDevice2 = rxBleClient.getBleDevice(address);
                    }
                }
            }, 2000);
        }


        final AlertDialog dialog = builder.create();

        device_btn01.setOnClickListener(v -> {
            if ((Devices.get("Device_01") != null))
                if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                    Intent sender = new Intent(CPRActivity.this, BluetoothLeServiceCPR.class);
                    sender.setAction(BluetoothLeServiceCPR.ACTION_CALL);
                    sender.putExtra(BluetoothLeServiceCPR.DATA1_NOT_KEY, 0);
                    startService(sender);
                }
        });

        device_btn02.setOnClickListener(v -> {
            if ((Devices.get("Device_02") != null))
                if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                    Intent sender = new Intent(CPRActivity.this, BluetoothLeServiceCPR.class);
                    sender.setAction(BluetoothLeServiceCPR.ACTION_CALL);
                    sender.putExtra(BluetoothLeServiceCPR.DATA1_NOT_KEY, 1);
                    startService(sender);
                }
        });

        band_dialog_layout.setOnClickListener(v -> {
            isConnect = false;
            scanLeDevice(false);

            long now_ = System.currentTimeMillis();
            Date date_ = new Date(now_);
            SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String time = sdf_.format(date_);
            ChatData chatData_ = new ChatData("준비", time, UserName_);
            databaseReference.child("Room").child(room_).child("message").child(UserName).push().setValue(chatData_);
            ChatData chatData__ = new ChatData("밴드/" + de1Connect + de2Connect, time, UserName_);
            databaseReference.child("Room").child(room_).child("message").child(UserName).push().setValue(chatData__);
            isReady = true;
            databaseReference.child("Room").child(room).child("conference").addChildEventListener(conferenceEventListener);

            try {
                if (!Devices.isEmpty()) { //TODO BAND SET
                    if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                        bluetoothLeServiceCPR.writeCharacteristic(0, "f3");
                    }
                    if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                        bluetoothLeServiceCPR.writeCharacteristic(1, "f3");
                        bluetoothLeServiceCPR.writeCharacteristic(1, "e0");
                        initialize();
                    }
                }
            } catch (Exception e) {
            }

            dialog.dismiss();
        });

        cpr_scan_reset.setOnClickListener(v -> {
            isConnect = false;
            mConnected = false;
            bluetoothLeServiceCPR.disconnect();
            Thread.interrupted();
            showScanDialog(room, UserName);
            dialog.dismiss();
        });

        band_dialog_reset.setOnClickListener(v -> {
            isConnect = false;
            mConnected = false;
            bluetoothLeServiceCPR.disconnect();
            showAlertDialog();
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void showAlertDialog() {
        request_num = 0;
        de1Connect = 0;
        de2Connect = 0;
        device2_connect = false;
        device1_connect = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(CPRActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_main, null);
        builder.setView(view);

        final Button close_btn = (Button) view.findViewById(R.id.close_btn);
        final Button connect = (Button) view.findViewById(R.id.connect);
        final Button scan_btn = (Button) view.findViewById(R.id.scan_btn);
        final ListView List_device = (ListView) view.findViewById(R.id.List_device);
        final ListView List_device02 = (ListView) view.findViewById(R.id.List_device02);

        final TextView mac_address_01 = (TextView) view.findViewById(R.id.mac_address_01);
        final TextView mac_address_02 = (TextView) view.findViewById(R.id.mac_address_02);


        final AlertDialog dialog = builder.create();

        //블루투스 활성화 체크
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    permissionCheck();
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // 어텝터 초기화
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mLeDeviceListAdapter02 = new LeDeviceListAdapter();
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter02.clear();
        List_device.setAdapter(mLeDeviceListAdapter);
        List_device02.setAdapter(mLeDeviceListAdapter02);
        scanLeDevice(true);

        final int[] count = {0, 0};

        List_device.setOnItemClickListener((parent, view12, position, id) -> {
            count[0]++;
            switch (count[0]) {
                case 1:
                    mac_address_01.setText(mLeDeviceListAdapter.getDevice(position).getMacAddress());
                    mLeDeviceListAdapter.remove(position);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    mac_list.add(mac_address_01.getText().toString());
                    break;
            }
        });

        List_device02.setOnItemClickListener((parent, view1, position, id) -> {
            count[1]++;
            switch (count[1]) {
                case 1:
                    mac_address_02.setText(mLeDeviceListAdapter02.getDevice(position).getMacAddress());
                    mLeDeviceListAdapter02.remove(position);
                    mLeDeviceListAdapter02.notifyDataSetChanged();
                    mac_list.add(mac_address_02.getText().toString());
                    break;
            }
        });

        connect.setOnClickListener(v -> {

            if (isScanning()) {
                scanLeDevice(false);
            }

            if (!mac_address_01.getText().toString().equals(""))
                sharedPreferences.edit().putString("DeviceCPR_01", mac_address_01.getText().toString()).apply();
            else
                sharedPreferences.edit().putString("DeviceCPR_01", "-").apply();

            if (!mac_address_02.getText().toString().equals(""))
                sharedPreferences.edit().putString("DeviceCPR_02", mac_address_02.getText().toString()).apply();
            else
                sharedPreferences.edit().putString("DeviceCPR_02", "-").apply();


            dialog.dismiss();
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter02.clear();
            mac_list.clear();
            showScanDialog(room, UserName);

        });
        scan_btn.setOnClickListener(v -> {
            if (isScanning()) {
                scanLeDevice(false);
            }
            mac_list.clear();
            showAlertDialog();
            dialog.dismiss();
        });
        close_btn.setOnClickListener(v -> {
            if (isScanning()) {
                scanLeDevice(false);
            }
            mac_list.clear();
            showScanDialog(room, UserName);
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void showStart(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.show_start_ayout, null);
        builder.setView(view);

        final Button startbtn = (NeumorphButton) view.findViewById(R.id.show_start_01);

        final AlertDialog dialog = builder.create();

        if (!testmode) {
            count_layout.setVisibility(View.VISIBLE);
        } else {
            count_layout.setVisibility(View.INVISIBLE);
        }

        new Thread(() -> {
            for (int i = 5; i >= 0; i--) {
                try {
                    startbtn.setText(String.valueOf(i));
                    if (i == 0) {
                        startbtn.setText(getString(R.string.start));
                        Thread.sleep(1000);
                        String cmd = null;
                        start_check = false;
                        if (!testmode) {
                            cmd = "f3";
                        } else {
                            cmd = "f4";
                        }
                        if (mConnected) {
                            try {
                                if (cmd != null) {
                                    Log.e("cmd", cmd);
                                    if (!testmode) {
                                        Intent sender = new Intent(CPRActivity.this, BluetoothLeServiceCPR.class);
                                        sender.setAction(BluetoothLeServiceCPR.ACTION_SOUND);
                                        sender.putExtra(BluetoothLeServiceCPR.DATA1_NOT_KEY, interval);
                                        startService(sender);
                                    }

                                    if (!Devices.isEmpty()) { //TODO BAND SET
                                        if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                                            bluetoothLeServiceCPR.writeCharacteristic(0, cmd);
                                        }
                                        if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                                            bluetoothLeServiceCPR.writeCharacteristic(1, "f3");
                                        }
                                    }

                                    if (!device2_connect) {
                                        if (!bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                                            bluetoothtime_list01.add(0f);
                                        }
                                    } else {
                                        if (!bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                                            bluetoothtime_list01.add(0f);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("Exception", e.toString());
                                if (playCheck) {
                                    playCheck = false;
                                }
                            }
                            if (!start_check) {
                                total_count = 0;
                                StartTime = SystemClock.uptimeMillis();
                                StartTime_L = System.currentTimeMillis();
                                intrval_01 = System.currentTimeMillis();
                                handler.postDelayed(runnable, 0);
                                intrval_01 = System.currentTimeMillis();
                            }
                        }
                        angle01 = 0;
                        position01 = 0;
                        breath01 = 0;
                        peakTimes.clear();
                        total_count = 0;
                        position_bpm = 0f;
                        Seconds_ = 0;
                        score_01 = 0;
                        cycle_01 = 0;
                        correct_position = 0;
                        total_position = 0;

                        isReset = false;

                        cprItem_01.clear();

                        depth_num = 0;
                        depth_correct = 0;
                        position_num01 = 0;
                        position_correct01 = 0;
                        lung_num01 = 0;
                        lung_correct01 = 0;
                        dialog.dismiss();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (!CPRActivity.this.isFinishing())
            dialog.show();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (isScanning()) {
            } else {
                if (rxBleClient.isScanRuntimePermissionGranted()) {
                    scanBleDevices();
                } else {
                    LocationPermission.requestLocationPermission(this, rxBleClient);
                }
            }
        } else {
            if (scanDisposable != null)
                scanDisposable.dispose();
        }
    }

    private void scanBleDevices() {
        scanDisposable = rxBleClient.scanBleDevices(
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                                .build(),
                        new ScanFilter.Builder()
                                .build()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(this::addScanResult, this::onScanFailure);
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            ScanExceptionHandler.handleException(this, (BleScanException) throwable);
        }
    }

    private void dispose() {
        scanDisposable = null;
    }

    private void addScanResult(com.polidea.rxandroidble2.scan.ScanResult bleScanResult) {
        RxBleDevice bleDevice = bleScanResult.getBleDevice();
        String name = bleDevice.getName();
        String address = bleDevice.getMacAddress();
        if (name != null) {
            if (!mac_list.contains(address)) {
                if (name.contains("BAND")) {
                    mLeDeviceListAdapter.addDevice(bleDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
                if (name.contains("AIO")) {
                    mLeDeviceListAdapter02.addDevice(bleDevice);
                    mLeDeviceListAdapter02.notifyDataSetChanged();
                }
            }
        }
    }

    private void setChatEventListener() {
        chatEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RemoteChatData remoteChatData = snapshot.getValue(RemoteChatData.class);
                remoteChatAdapter.addChatItem(remoteChatData);
                chatroom_recyclerview.scrollToPosition(remoteChatAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    private void setConferenceEventListener() {
        conferenceEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatData chatData = snapshot.getValue(ChatData.class);
                JitsiMeetConferenceOptions options
                        = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(chatData.getMessage())
                        // Settings for audio and video
                        .setAudioMuted(true)
                        //.setVideoMuted(true)
                        .build();
                latestOptions = options;
                video_conferenceBtn.setEnabled(true);
                video_conferenceBtn.setVisibility(View.VISIBLE);
                // Launch the new activity with the given options. The launch() method takes care
                // of creating the required Intent and passing the options.
                JitsiMeetActivity.launch(CPRActivity.this, options);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    private void setChildEventListener() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatData chatData = dataSnapshot.getValue(ChatData.class);

                if (Double.parseDouble(chatData.getPostDate()) < enter_time) {
                } else {
                    Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon);
                    if (chatData.getMessage().contains("시작"))
                        if (mConnected)
                            if (start_check) {
                                showStart(CPRActivity.this);
                            } else {
                                Toast toast = Toast.makeText(CPRActivity.this, R.string.running, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, Gravity.BOTTOM);
                                toast.show();
                            }
                    if (chatData.getMessage().contains("시간/")) {
                        String data[] = chatData.getMessage().split("/");
                        mode_cpr_value.setText(data[1]);
                        event_time = Integer.parseInt(data[1]);
                    }
                    if (chatData.getMessage().contains("starttime/")) {
                        String data[] = chatData.getMessage().split("/");
                        toDay = data[1] + "/" + data[2];
                        Log.d("toDay", toDay);
                    }

                    if (chatData.getMessage().contains("방송/")) {
                        String data = chatData.getMessage().substring(3);
                        String videoId;
                        if (getYoutubeId(data) != null)
                            videoId = getYoutubeId(data);
                        else
                            videoId = data;
                        if (!isInit) {
                            ytb_img.setVisibility(View.GONE);
                            //cameraView.setVisibility(View.GONE);
                            YouTubePlayerView.setVisibility(View.VISIBLE);
                            AbstractYouTubePlayerListener youTubePlayerListener = new AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(YouTubePlayer youTubePlayer2) {
                                    super.onReady(youTubePlayer2);
                                    youTubePlayer = youTubePlayer2;
                                    youTubePlayer.cueVideo(videoId, 0);
                                }
                            };
                            YouTubePlayerView.initializeWithWebUi(youTubePlayerListener, true);
                            isInit = true;
                        } else {
                            youTubePlayer.cueVideo(videoId, 0);
                        }
                        /*if(getYoutubeId(data) != null) {
                            YouTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(YouTubePlayer youTubePlayer2) {
                                    youTubePlayer = youTubePlayer2;
                                    youTubePlayer.cueVideo(getYoutubeId(data), 0);
                                }
                            });

                            //YouTubePlayerView.play(getYoutubeId(data),null);
                        }else{
                            YouTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(YouTubePlayer youTubePlayer2) {
                                    youTubePlayer = youTubePlayer2;
                                    youTubePlayer.cueVideo(data, 0);
                                }
                                });
                            //YouTubePlayerView.play(data, null);
                        }*/
                    }
                    if (chatData.getMessage().contains("화면최소화")) {
                        JitsiMeetActivityDelegate.onBackPressed();
                    }
                    if (chatData.getMessage().contains("pause")) {
                        if (youTubePlayer != null)
                            youTubePlayer.pause();
                    }
                    if (chatData.getMessage().contains("resume")) {
                        if (youTubePlayer != null)
                            youTubePlayer.play();
                    }
                    if (chatData.getMessage().contains("처음으로")) {
                        if (youTubePlayer != null)
                            youTubePlayer.seekTo(0);
                    }
                    if (chatData.getMessage().contains("중지")) {
                        reset(2);
                    }

                    /*if(chatData.getMessage().contains("화상아웃")){
                        hangUp();
                    }*/
                    if (chatData.getMessage().contains("깊이/")) {
                        String data[] = chatData.getMessage().split("/");
                        minDepth = (int) (Float.parseFloat(data[1]) * 10);
                        maxDepth = (int) (Float.parseFloat(data[2]) * 10);
                        changeDepth();
                    }
                    if (chatData.getMessage().contains("모드/")) { // false == practice , true == evaluation
                        String data[] = chatData.getMessage().split("/");
                        testmode = Boolean.parseBoolean(data[1]);
                    }
                    if (chatData.getMessage().contains("간격/")) {
                        String data[] = chatData.getMessage().split("/");
                        interval = Integer.parseInt(data[1]);
                    }
                    if (chatData.getMessage().contains("나갔습니다.")) {
                        Intent sender = new Intent(CPRActivity.this, BluetoothLeServiceCPR.class);
                        sender.setAction(BluetoothLeServiceCPR.ACTION_READY);
                        startService(sender);

                        unbindService(finishConnection);
                        isOut = true;
                        databaseReference.child("Room").child(room).setValue(null);

                        //hangUp();
                        disconnectJitsi();
                        LocalBroadcastManager.getInstance(CPRActivity.this).unregisterReceiver(broadcastReceiver);
                        reset(1);
                    }
                    /*if(chatData.getMessage().contains("카메라/")){
                        String data[] = chatData.getMessage().split("/");
                        if(data[1].equals("on")){
                            ytb_img.setVisibility(View.GONE);
                            YouTubePlayerView.setVisibility(View.GONE);
                            cameraView.setVisibility(View.VISIBLE);
                        }else{
                            if(isInit){
                                ytb_img.setVisibility(View.GONE);
                                YouTubePlayerView.setVisibility(View.VISIBLE);
                            }else{
                                ytb_img.setVisibility(View.VISIBLE);
                                YouTubePlayerView.setVisibility(View.GONE);
                            }
                            cameraView.setVisibility(View.GONE);
                        }
                    }*/
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }

    float max_secs = 0.0f;

    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        bluetoothLeServiceCPR.disconnect();
        bluetoothLeServiceCPR = null;
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }

    //TODO Timer
    private Runnable runnable = new Runnable() {

        public void run() {
            int event_time = CPRActivity.this.event_time;
            //Log.e("event_time", String.valueOf(event_time));

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            //Log.e("update_time", String.valueOf(UpdateTime / 1000));

            //Seconds_ = event_time - (int) (UpdateTime / 1000);

            Seconds_ = (int) (UpdateTime / 1000);

            //MilliSeconds = (int) (UpdateTime % 1000);

            mSeconds_ = event_time - (int) (UpdateTime / 1000);

            Minutes = mSeconds_ / 60;

            Seconds = mSeconds_ % 60;

            /*Log.e("Minutes_time", String.valueOf(Minutes));
            Log.e("Seconds_time", String.valueOf(Seconds));*/

            cpr_timer.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds));

            handler.postDelayed(this, 0);

            // String[] timer_sec = cpr_timer.getText().toString().split(":");

            if (!Devices.isEmpty()) {
                if (bluetoothLeServiceCPR != null) {
                    if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_02")) || bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                        long now = System.currentTimeMillis() - intrval_01;
                        float secs = now / 1000.0f;
                        if (now >= 1500) {
                            if (handOff_01 < Seconds_) {
                                if (max_secs < secs)
                                    max_secs = secs;
                                handOff_01 = Seconds_;
                            }
                            if (!isBreath01) {
                                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                                cpr_arrow01_.setVisibility(View.VISIBLE);
                                remote_arrow_down_text.setVisibility(View.VISIBLE);
                                cpr_arrow01.setVisibility(View.INVISIBLE);
                                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            if (max_secs != 0)
                                cprItem_01.add(new UserItem(Seconds_, max_secs, 0));

                            max_secs = 0.0f;
                            handOff_01 = Seconds_;
                            cpr_arrow01_.setVisibility(View.INVISIBLE);
                            remote_arrow_down_text.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
            if (Seconds_ == Integer.parseInt(mode_cpr_value.getText().toString())) {
                if (!isReset)
                    reset(2);
            }
        }

    };

    private void changeDepth() {
        ArrayList<Integer> newRange = new ArrayList<>();
        newRange.add(245);
        newRange.add(minDepth);
        newRange.add(maxDepth);
        if ((Devices.get("Device_01") != null)) {
            if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                Intent sender = new Intent(this, BluetoothLeServiceCPR.class);
                sender.setAction(BluetoothLeServiceCPR.ACTION_DEPTH_CHANGE);
                sender.putExtra(BluetoothLeServiceCPR.DATA1_NOT_KEY, minDepth);
                sender.putExtra(BluetoothLeServiceCPR.DATA2_NOT_KEY, maxDepth);
                startService(sender);

            }
        }
    }

    private void reset(int set) {
        TimeBuff += MillisecondTime;
        handler.removeCallbacks(runnable);

        lung01.setVisibility(View.INVISIBLE);
        test_lung01.setVisibility(View.INVISIBLE);


        Intent sender = new Intent(CPRActivity.this, BluetoothLeServiceCPR.class);
        sender.setAction(BluetoothLeServiceCPR.ACTION_SOUND);
        sender.putExtra(BluetoothLeServiceCPR.DATA1_NOT_KEY, 0);
        startService(sender);

        // runHander(false);

        if (set == 2) {
            if (max_secs != 0)
                cprItem_01.add(new UserItem(Seconds_, max_secs, 0));

            if (!device2_connect) {
                if (!bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                    bluetoothtime_list01.add((float) Seconds_);
                }
            } else {
                if (!bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                    bluetoothtime_list01.add((float) Seconds_);
                }
            }
            isReset = true;
            try {
                ArrayList<ReportItem> reportItems = new ArrayList<>();
                Log.e("bluetoothtime", String.valueOf(bluetoothtime_list01));

                String reName = UserName;
                if (!testmode) {
                    if (isAdult) {
                        reName += "(연습/성인)";
                    } else {
                        reName += "(연습/소아)";
                    }
                } else {
                    if (isAdult) {
                        reName += "(평가/성인)";
                    } else {
                        reName += "(평가/소아)";
                    }
                }

                reportItems.add(report_setting(cprItem_01, reName, presstime_list01, breathval_01, breathtime_01, String.valueOf(ventil_volume_01),
                        String.valueOf(cycle_01), String.valueOf(score_01),
                        String.valueOf(minDepth), String.valueOf(maxDepth),
                        String.valueOf(depth_num), String.valueOf(depth_correct), String.valueOf(position_num01), String.valueOf(position_correct01),
                        String.valueOf(lung_num01), String.valueOf(lung_correct01), bpm1, bluetoothtime_list01));

                Converters converters = new Converters();
                long now_ = System.currentTimeMillis();
                Date date_ = new Date(now_);
                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                String getTime_ = sdf_.format(date_);

                int depth_count = 0;
                int depth_sum = 0;
                String min = reportItems.get(0).getReport_Min();
                String max = reportItems.get(0).getReport_Max();
                ArrayList<Float> chart_item = reportItems.get(0).getReport_depth_list();
                for (float item : chart_item) {
                    if (item >= Integer.parseInt(min) && item <= Integer.parseInt(max)) {
                        depth_sum += item;
                        depth_count++;
                    } else if ((5 < item && item < Integer.parseInt(min))) {
                        depth_sum += item;
                        depth_count++;
                    } else if ((Integer.parseInt(max) < item && item <= 100)) {
                        depth_sum += item;
                        depth_count++;
                    }
                }
                if (depth_count > 5) {
                    double avg_depth = depth_sum / depth_count;
                    String avg_depth_s = String.format("%.2f", avg_depth);
                    String bpm = reportItems.get(0).getReport_bpm();
                    String score;
                    int sum_depth = Integer.parseInt(reportItems.get(0).getReport_depth_correct())
                            + Integer.parseInt(reportItems.get(0).getReport_up_depth())
                            + Integer.parseInt(reportItems.get(0).getReport_down_depth());

                    int depth_accuracy_ = (int) ((double) sum_depth / (double) 3);
                    int breathScore = (int) ((Double.parseDouble(reportItems.get(0).getReport_lung_correct()) / Double.parseDouble(reportItems.get(0).getReport_lung_num())) * 100);
                    if (Integer.parseInt(reportItems.get(0).getReport_position_num()) != 0) {
                        int all_score = (int) ((breathScore * 0.2) + (depth_accuracy_ * 0.8));
                        score = String.valueOf(all_score);
                    } else {
                        score = reportItems.get(0).getReport_down_depth();
                    }

                    String stopList;
                    if (reportItems.get(0).getStop_time_list().isEmpty()) {
                        stopList = "0";
                    } else {
                        stopList = converters.writingStringFromList(reportItems.get(0).getStop_time_list());
                    }

                    ChatData chatData__ = new ChatData("score/" + score + "/" + avg_depth_s + "/" + bpm, getTime_, UserName);
                    databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData__);
                    ChatData chatData_ = new ChatData("report/" + reportItems.get(0).getReport_end_time() + "/"
                            + reportItems.get(0).getReport_interval_sec() + "/"
                            + reportItems.get(0).getReport_cycle() + "/"
                            + reportItems.get(0).getReport_depth_correct() + "/"
                            + reportItems.get(0).getReport_up_depth() + "/"
                            + reportItems.get(0).getReport_down_depth() + "/"
                            + reportItems.get(0).getReport_bpm() + "/"
                            + reportItems.get(0).getReport_angle() + "/"
                            + converters.writingStringFromList(reportItems.get(0).getReport_depth_list()) + "/"
                            + converters.writingStringFromList(reportItems.get(0).getReport_presstime_list()) + "/"
                            + converters.writingStringFromList(reportItems.get(0).getReport_breathval()) + "/"
                            + converters.writingStringFromList(reportItems.get(0).getReport_breathtime()) + "/"
                            + reportItems.get(0).getReport_ventil_volume() + "/"
                            + reportItems.get(0).getDepth_num() + "/"
                            + reportItems.get(0).getDepth_correct() + "/"
                            + reportItems.get(0).getReport_position_num() + "/"
                            + reportItems.get(0).getReport_position_correct() + "/"
                            + reportItems.get(0).getReport_lung_num() + "/"
                            + reportItems.get(0).getReport_lung_correct() + "/"
                            + stopList + "/"
                            + converters.writingStringFromList(reportItems.get(0).getReport_bletime_list()) + "/"
                            + currentUserName
                            , getTime_
                            , UserName);

                    databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData_);
                    ReportSave(reportItems);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MillisecondTime = 0L;
        StartTime = 0L;
        StartTime_L = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;
        angle01 = 0;
        mSeconds_ = 0;
        mSeconds = 0;
        max_secs = 0.0f;
        position01 = 0;

        breath01 = 0;

        cpr_timer.setText("00:00");

        cpr_arrow01.setVisibility(View.INVISIBLE);

        cpr_arrow01_.setVisibility(View.INVISIBLE);

        remote_depth_text.setText("0");
        remote_arrow_down_text.setVisibility(View.INVISIBLE);
        remote_arrow_up_text.setVisibility(View.INVISIBLE);

        final Animation animation = new TranslateAnimation(0, 0, 0, 0);
        depth_btn01.startAnimation(animation);
        press_ave_btn01.startAnimation(animation);

        Thread.interrupted();
        start_check = true;

        breathtime_01.clear();
        breathtime_01.add(0.0f);
        breathval_01.clear();
        breathval_01.add(200.0f);
        presstime_list01.clear();
        peakTimes.clear();
        bluetoothtime_list01.clear();
        total_count = 0;
        position_bpm = 0f;
        ventil_volume_01 = 0;

        if (set == 1) {
            Intent intent;
            mConnected = false;
            prefs.edit().putString("room", null).apply();

            databaseReference.child("Room").child(room).child("user").orderByChild("name").equalTo(UserName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot datas : snapshot.getChildren()) {
                        datas.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            databaseReference.child("Room").child(room).child("트레이너").removeEventListener(childEventListener);
            databaseReference.child("Room").child(room).child("conference").removeEventListener(conferenceEventListener);

            isout = true;

            intent = new Intent(CPRActivity.this, LobbyActivity.class);
            intent.putExtra("Name", UserName);
            Log.e("Name", UserName);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
        }

        Seconds_ = 0;
        score_01 = 0;
        cycle_01 = 0;

        cprItem_01.clear();

        depth_num = 0;
        depth_correct = 0;
        position_num01 = 0;
        position_correct01 = 0;
        lung_num01 = 0;
        lung_correct01 = 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (permissionCheck()) {
                startCamera();
            } else {
                //  Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                //this.finish();
            }
        }
        if (LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults, rxBleClient)) {
            scanBleDevices();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void startCamera() {
        //cameraView.setSurfaceTextureListener(textureListener);
    }

    /*private void openCamera() throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        cameraId = manager.getCameraIdList()[1];
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            manager.openCamera(cameraId, stateCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1001);
        }
    }*/
    /*private void createCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = cameraView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
        Surface surface = new Surface(texture);

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }

                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Toast.makeText(getApplicationContext(), "Configuration Changed", Toast.LENGTH_LONG).show();
            }
        }, null);
    }*/

    private void updatePreview() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
    }

    protected void stopBackgroundThread() throws InterruptedException {
        mBackgroundThread.quitSafely();
        mBackgroundThread.join();
        mBackgroundThread = null;
        mBackgroundHandler = null;
    }

    // 리스너 콜백 함수
    /*private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };*/

    /*private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                //createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };*/
    private void locationPermissionCheck() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1);
        }
    }

    private boolean permissionCheck() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void ReportSave(ArrayList<ReportItem> reportItems) {
        if (reportItems.size() != 0 && reportItems != null) {
            AppDatabase database = AppDatabase.getDbInstance(CPRActivity.this);
            for (ReportItem reportItem : reportItems) {
                Report report = new Report();
                report.report_name = reportItem.getReport_name();
                report.report_end_time = reportItem.getReport_end_time();
                report.report_interval_sec = reportItem.getReport_interval_sec();
                report.report_cycle = reportItem.getReport_cycle();
                report.report_depth_correct = reportItem.getReport_depth_correct();
                report.report_up_depth = reportItem.getReport_up_depth();
                report.report_down_depth = reportItem.getReport_down_depth();
                report.report_bpm = reportItem.getReport_bpm();
                report.report_angle = reportItem.getReport_angle();

                Converters converters = new Converters();

                report.report_depth_list = converters.writingStringFromList(reportItem.getReport_depth_list());
                report.report_presstimeList = converters.writingStringFromList(reportItem.getReport_presstime_list());
                report.report_breathval = converters.writingStringFromList(reportItem.getReport_breathval());
                report.report_breathtime = converters.writingStringFromList(reportItem.getReport_breathtime());
                report.report_ventil_volume = reportItem.getReport_ventil_volume();

                Log.d("toDay-", toDay);
                report.to_day = toDay;
                report.min = reportItem.getReport_Min();
                report.max = reportItem.getReport_Max();
                report.depth_num = reportItem.getDepth_num();
                report.depth_correct = reportItem.getDepth_correct();
                report.position_num = reportItem.getReport_position_num();
                report.position_correct = reportItem.getReport_position_correct();
                report.lung_num = reportItem.getReport_lung_num();
                report.lung_correct = reportItem.getReport_lung_correct();
                report.stop_time_list = converters.writingStringFromList(reportItem.getStop_time_list());
                report.report_bluetoothtime = converters.writingStringFromList(reportItem.getReport_bletime_list());

                database.reportDao().insert(report);
            }
        }
    }

    private ReportItem report_setting(ArrayList<UserItem> Useritem, String name, ArrayList<Float> presstimeList,
                                      ArrayList<Float> breathval, ArrayList<Float> breathtime, String ventil_volume,
                                      String cycle, String score,
                                      String min, String max, String depth_num, String depth_correct,
                                      String position_num, String position_correct, String lung_num, String lung_correct, ArrayList<Float> gBpm, ArrayList<Float> bluetoothtime_list) {
        ReportItem reportItem = null;
        ArrayList<Float> arrayList = new ArrayList<Float>();
        ArrayList<Float> stopList = new ArrayList<>();

        int Depth_size = 0;
        int position_six = 0;
        int angleSum = 0;
        int hand_off = 0;
        int position = 0;

        if (!Useritem.isEmpty() && Useritem != null)
            for (UserItem userItem : Useritem) {
                if (userItem.getAngle() != 0)
                    angleSum = angleSum + userItem.getAngle();

                if (userItem.getPosition() > 6)
                    position_six = position_six + 1;

                if (userItem.getPosition() == 5 || userItem.getPosition() == 11)
                    position = position + 1;

                if (userItem.getDepth_correct() != 0 || userItem.getDepth() != 0) {
                    Depth_size = Depth_size + 1;
                    if (userItem.getDepth_correct() != 0) {
                        if (userItem.getPosition() > 6)
                            arrayList.add((userItem.getDepth_correct() + 100f));
                        else
                            arrayList.add((float) userItem.getDepth_correct());
                    } else {
                        if (userItem.getPosition() > 6)
                            arrayList.add((userItem.getDepth() + 100f));
                        else
                            arrayList.add((float) userItem.getDepth());
                    }
                }

                if (userItem.getHand_off_start() != 0) {
                    arrayList.add((float) 0);
                    stopList.add(userItem.getHand_off_start());
                    hand_off += (int) userItem.getHand_off_start();
                }

                if (userItem.getBreath() != 0) {
                    arrayList.add((float) userItem.getBreath());
                }
            }

        int add_bpm = 0;

        if (gBpm != null && !gBpm.isEmpty()) {
            for (Float item_bpm : gBpm) {
                add_bpm += item_bpm;
            }
        }

        int up_depth = (int) (100 - ((double) position_six / (double) Depth_size) * 100);
        int position_;
        if (device2_connect) {
            position_ = (int) (((double) position / (double) Depth_size) * 100);
        } else {
            position_ = Integer.parseInt(score);
        }
        //  int bpm = (int) (((double) (Depth_size / (double) Seconds_) * 60));
        int bpm = 0;
        if (gBpm.size() > 0) {
            bpm = add_bpm / gBpm.size();
        }
        int angle = (int) (((double) angleSum / (double) Depth_size));
        if (bluetoothtime_list.isEmpty()) {
            bluetoothtime_list.add(0f);
        }

        reportItem = new ReportItem(name
                , String.valueOf(Seconds_)
                , String.valueOf(hand_off)
                , cycle
                , String.valueOf(position_)
                , String.valueOf(up_depth)
                , score
                , String.valueOf(bpm)
                , String.valueOf(angle)
                , arrayList
                , presstimeList
                , breathtime
                , breathval
                , ventil_volume
                , min
                , max
                , depth_num
                , depth_correct
                , position_num
                , position_correct
                , lung_num
                , lung_correct
                , stopList
                , bluetoothtime_list
        );

        return reportItem;
    }

    private void initialize() {
        String address;
        address = sharedPreferences.getString("address", null);
        if (address != null) {
            HashMap<String, String> addresstohashmap = new Gson().fromJson(address, (Type) HashMap.class);
            if (addresstohashmap.containsKey(bleDevice2.getMacAddress())) {
                address_array01 = addresstohashmap.get(bleDevice2.getMacAddress()).split("/");
                min_lung01 = (int) (Float.parseFloat(address_array01[0]) * 1.1);
                max_lung01 = (int)(Float.parseFloat(address_array01[1]));
                bre_threshold01 = min_lung01 + 5;
                gap_lung01 = max_lung01 - bre_threshold01;
                bre_level01 = (int) (min_lung01 + ((float) gap_lung01 / 3));
                bre_level02 = (int) (min_lung01 + ((float) gap_lung01 / 3 * 2));
                isCali01 = true;
            } else {
                bre_threshold01 = min_lung01 + 5;
                gap_lung01 = max_lung01 - bre_threshold01;
                bre_level01 = (int) (min_lung01 + ((float) gap_lung01 / 3));
                bre_level02 = (int) (min_lung01 + ((float) gap_lung01 / 3 * 2));
            }
        } else {
            bre_threshold01 = min_lung01 + 5;
            gap_lung01 = max_lung01 - bre_threshold01;
            bre_level01 = (int) (min_lung01 + ((float) gap_lung01 / 3));
            bre_level02 = (int) (min_lung01 + ((float) gap_lung01 / 3 * 2));
        }
        Print.e("Test", "min_lung = " + min_lung01 + ", max_lung = " + max_lung01);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String getTime = sdf.format(date);
        String message = min_lung01 + "/" + max_lung01;
        ChatData chatData = new ChatData("cali/" + message, getTime, UserName);
        databaseReference.child("Room").child(room).child("message").child(UserName).push().setValue(chatData);
    }

}

