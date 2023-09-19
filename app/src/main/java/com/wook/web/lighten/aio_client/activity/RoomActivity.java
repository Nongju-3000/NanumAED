package com.wook.web.lighten.aio_client.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.wook.web.lighten.aio_client.R;
import com.wook.web.lighten.aio_client.adapter.ReportAdapter;
import com.wook.web.lighten.aio_client.ble.BluetoothLeServiceCPR;
import com.wook.web.lighten.aio_client.db.AppDatabase;
import com.wook.web.lighten.aio_client.db.Report;
import com.wook.web.lighten.aio_client.utils.LocationPermission;
import com.wook.web.lighten.aio_client.utils.Print;
import com.wook.web.lighten.aio_client.utils.ScanExceptionHandler;

import org.jitsi.meet.sdk.BroadcastIntentHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import soup.neumorphism.NeumorphButton;
import soup.neumorphism.NeumorphCardView;
import soup.neumorphism.NeumorphImageButton;


@SuppressLint("MissingPermission")
public class RoomActivity extends AppCompatActivity {

    private BackPressCloseHandler backPressCloseHandler;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    EditText room;
    EditText name_first, name_second;
    Button into, cali_btn;
    LabeledSwitch mode_switch_one, mode_switch_two;
    //LinearLayout linear;
    InputMethodManager imm;
    String roomName;
    String userName;
    public static SharedPreferences prefs;
    private SharedPreferences sharedPreferences;
    Bitmap bitmap = null;
    static boolean isFirstRun;
    private boolean roomCheck = false;
    NeumorphButton report_list_db;

    NeumorphCardView name_second_cardView;
    private BluetoothLeServiceCPR bluetoothLeServiceCPR;
    private TextView license_tv;
    private String token;
    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private TextView manual_tv;
    private boolean trainerCheck = false;
    private boolean twoMode = false;
    private boolean mConnected = false;
    private final HashMap<String, String> cali_map = new HashMap<>();

    private String Device01 = "-";
    private String Device02 = "-";

    public class BackPressCloseHandler {
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            moveTaskToBack(true); // 태스크를 백그라운드로 이동
            finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
        }
    }

    public void onBackPressed() {
        this.backPressCloseHandler.onBackPressed();
    }

    //TODO BLE SERVICE CONNECTION
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            bluetoothLeServiceCPR = ((BluetoothLeServiceCPR.LocalBinder) service).getService();

            if (!bluetoothLeServiceCPR.initialize()) {
                finish();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeServiceCPR = null;
        }
    };

    private void initialize() {
        String address;
        address = sharedPreferences.getString("address01", "-");
        address_array01 = address.split("/");
        if (address_array01[0].equals(address01)) {
            min_lung01 = Integer.parseInt(address_array01[1]);
            max_lung01 = Integer.parseInt(address_array01[2]);
            isCali01 = true;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);    // 타이틀바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_room);

        Intent gattServiceIntent = new Intent(this, BluetoothLeServiceCPR.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        locationPermissionCheck();

        this.backPressCloseHandler = new BackPressCloseHandler(this);

        rxBleClient = RxBleClient.create(this);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );
        /*RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException && throwable.getCause() instanceof BleException) {
                Log.v("RxBleApplication", "Suppressed UndeliverableException: " + throwable.toString());
                return; // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw new RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable);
        });*/

        // Intent gattServiceIntent = new Intent(this, BluetoothLeServiceCPR.class);
        // bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                // 23 버전 이상일 때 상태바 하얀 색상에 회색 아이콘 색상을 설정
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#6178e3"));
            }
        } else if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            getWindow().setStatusBarColor(Color.BLACK);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
            // Get new FCM registration token
            token = task.getResult();
        });

        license_tv = findViewById(R.id.license_tv);
        license_tv.setOnClickListener(v -> {
            Intent intent = new Intent(RoomActivity.this, OpenSourceActivity.class);
            startActivity(intent);
            finish();
        });

        manual_tv = findViewById(R.id.manual_tv);
        manual_tv.setOnClickListener(v -> {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference pdfRef = storageReference.child("manual/CREDO-MAN-003-AIO(rev.1.0)_Kor+Eng.pdf");

            File destinationPath = new File(getExternalFilesDir(null), "/manual");
            if (!destinationPath.exists()) {
                destinationPath.mkdirs();
            }
            //   File destinationPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
            File checkPath = new File(destinationPath, "CREDO-MAN-003-AIO(rev.1.0)_Kor+Eng.pdf");
            getPermission();
            if (checkPath.exists()) {
                showPdf();
            } else {
                Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.loading_screen);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false);

                TextView proPercent = dialog.findViewById(R.id.proPercent);

                dialog.show();
                try {
                    checkPath.createNewFile();
                    pdfRef.getFile(checkPath).addOnCompleteListener(complete -> {
                        dialog.dismiss();
                        showPdf();
                    }).addOnProgressListener(data -> {
                        int percent = (int) ((100 * data.getBytesTransferred()) / data.getTotalByteCount());
                        proPercent.setText(String.valueOf(percent));
                    }).addOnFailureListener(fail -> {
                        Log.e("Test", "download fail " + fail.getMessage());
                    })
                    ;
                } catch (IOException e) {
                    Log.e("Test", "IO Exception " + e.getMessage());
                }
            }
        });

        //room = (EditText) findViewById(R.id.room);
        name_second_cardView = (NeumorphCardView) findViewById(R.id.name_second_cardView);
        name_first = (EditText) findViewById(R.id.first_name);
        name_second = (EditText) findViewById(R.id.second_name);
        into = (Button) findViewById(R.id.into);
        //linear = (LinearLayout) findViewById(R.id.linear);
        report_list_db = (NeumorphButton) findViewById(R.id.report_list_db);
        mode_switch_one = (LabeledSwitch) findViewById(R.id.mode_switch_one);
        mode_switch_two = (LabeledSwitch) findViewById(R.id.mode_switch_two);

        report_list_db.setOnClickListener(v -> showReportList(RoomActivity.this));

        prefs = getSharedPreferences("Pref", MODE_PRIVATE);
        sharedPreferences = getApplication().getSharedPreferences("DeviceCPR", MODE_PRIVATE);

        String image = prefs.getString("image", "");
        bitmap = StringToBitMap(image);

        Intent intent = getIntent();
        //roomName = intent.getStringExtra("roomName");
        userName = prefs.getString("userName", "");

        //room.setText(roomName);
        if (userName.contains("&")) {
            String[] name = userName.split("&");
            name_first.setText(name[0]);
            name_second.setText(name[1]);
            twoMode = true;
            mode_switch_one.setColorOff(Color.parseColor("#232323"));
            mode_switch_one.setColorOn(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
            mode_switch_two.setColorOn(Color.parseColor("#232323"));
            mode_switch_two.setColorOff(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
            name_second_cardView.setVisibility(View.VISIBLE);
        } else {
            name_first.setText(userName);
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        cali_btn = findViewById(R.id.cali_btn);

        String Device01 = sharedPreferences.getString("DeviceCPR_01", "-");
        String Device02 = sharedPreferences.getString("DeviceCPR_02", "-");

        if (!Device01.equals("-"))
            Devices.put("Device_01", Device01);
        if (!Device02.equals("-"))
            Devices.put("Device_02", Device02);

        mode_switch_one.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (twoMode) {
                    mode_switch_one.setColorOff(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    mode_switch_one.setColorOn(Color.parseColor("#232323"));
                    mode_switch_two.setColorOn(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    mode_switch_two.setColorOff(Color.parseColor("#232323"));
                    twoMode = false;
                    name_second_cardView.setVisibility(View.GONE);
                } else {
                    mode_switch_one.setColorOff(Color.parseColor("#232323"));
                    mode_switch_one.setColorOn(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    mode_switch_two.setColorOn(Color.parseColor("#232323"));
                    mode_switch_two.setColorOff(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    twoMode = true;
                    name_second_cardView.setVisibility(View.VISIBLE);
                }
            }
            return true;
        });

        mode_switch_two.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (twoMode) {
                    mode_switch_one.setColorOff(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    mode_switch_one.setColorOn(Color.parseColor("#232323"));
                    mode_switch_two.setColorOn(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    mode_switch_two.setColorOff(Color.parseColor("#232323"));
                    name_second_cardView.setVisibility(View.GONE);
                    twoMode = false;
                } else {
                    mode_switch_one.setColorOff(Color.parseColor("#232323"));
                    mode_switch_one.setColorOn(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    mode_switch_two.setColorOn(Color.parseColor("#232323"));
                    mode_switch_two.setColorOff(ContextCompat.getColor(RoomActivity.this, R.color.text_orange));
                    name_second_cardView.setVisibility(View.VISIBLE);
                    twoMode = true;
                }
            }
            return true;
        });

        /*mode_switch_one.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn) {
                    mode_switch_two.setOn(false);
                    name_second_cardView.setVisibility(View.GONE);
                } else {
                    mode_switch_two.setOn(true);
                }
            }
        });

        mode_switch_two.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn) {
                    mode_switch_one.setActivated(false);
                    name_second_cardView.setVisibility(View.VISIBLE);
                } else {
                    mode_switch_one.setOn(true);
                }
            }
        });*/

        ////
        if (!Devices.isEmpty()) {
            List<BluetoothDevice> devices = bluetoothManager.getConnectedDevices(BluetoothGatt.GATT);
            int status = -1;
            for (BluetoothDevice device : devices) {
                status = bluetoothManager.getConnectionState(device, BluetoothGatt.GATT);
                if (status == BluetoothProfile.STATE_CONNECTED) {
                    if (Devices.get("Device_02").equals(device.getAddress())) {
                        mConnected = true;
                    }
                }
            }
        }
        ////

        cali_btn.setOnClickListener(v -> {
            if (mConnected) {
                showSettingDialog();
            } else {
                if (Device02.equals("-")) {
                    showAlertDialog();
                } else {
                    showScanDialog();
                }
            }
        });

        /*room.addTextChangedListener(new TextValidator(room) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length() == 0){
                    int color = Color.parseColor("#cccccc");
                    into.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
                }else{
                    int color = Color.parseColor("#6178e3");
                    into.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
                }

            }
        });*/

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        if (prefs.getString("room", null) != null) {
            databaseReference.child("Room").child(prefs.getString("room", null)).child("into").orderByKey().equalTo(prefs.getString("userName", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Intent intent1 = new Intent(RoomActivity.this, CPRActivity.class);
                        intent1.putExtra("room", prefs.getString("room", null));
                        intent1.putExtra("name", prefs.getString("userName", null));
                        startActivity(intent1);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        /*linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(room.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(name_first.getWindowToken(), 0);
            }
        });*/

        into.setOnClickListener(v -> {
            isFirstRun = prefs.getBoolean("isFirstRun", true);
            if (isFirstRun) {
                Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon);
                String intoname = name_first.getText().toString();
                if (twoMode) {
                    intoname = intoname + "&" + name_second.getText().toString();
                }
                User user = new User("android", token, intoname, BitMapToString(icon));
                databaseReference.child("TOKEN").child(intoname).setValue(user);
                prefs.edit().putBoolean("isFirstRun", false).apply();
            }

            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            final String getTime = sdf.format(date);

            SharedPreferences.Editor editor = prefs.edit();
            if (twoMode) {
                editor.putString("userName", name_first.getText().toString() + "&" + name_second.getText().toString());
            } else {
                editor.putString("userName", name_first.getText().toString());
            }

            editor.apply();

            final Intent intent1 = new Intent(RoomActivity.this, LobbyActivity.class);

            if (name_first.getText().toString().equals("") || (name_second.getText().toString().equals("") && twoMode)) {
                Toast.makeText(RoomActivity.this, R.string.inputname, Toast.LENGTH_SHORT).show();
            } else {
                String intoName = name_first.getText().toString();
                if (twoMode) {
                    intoName = intoName + "&" + name_second.getText().toString();
                }
                intent1.putExtra("Name", intoName);
                intent1.putExtra("Token", token);

                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                finish();
            }

            /*final Intent intent1 = new Intent(RoomActivity.this, CPRActivity.class);

            Bundle bundle = new Bundle();

            bundle.putString("room", room.getText().toString());
            if (!room.equals("") && room != null) {
                intent1.putExtras(bundle);
            }

            bundle.putString("name", name.getText().toString());
            if (!name.equals("") && name != null) {
                intent1.putExtras(bundle);
            }


            if(room.getText().toString().length() == 0){
                Toast.makeText(getApplication(), getString(R.string.enter_room), Toast.LENGTH_SHORT).show();
            }else{
                if(name.getText().toString().length() == 0){
                    Toast.makeText(getApplication(), getString(R.string.enter_nickname), Toast.LENGTH_SHORT).show();
                }
                else{
                    databaseReference.child("Room").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot datas: snapshot.getChildren()){
                                if(datas.getKey().equals(room.getText().toString()))
                                    roomCheck = true;
                            }if(roomCheck){

                                databaseReference.child("Room").child(room.getText().toString()).child("trainer").orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot datas: dataSnapshot.getChildren()){
                                            if(name.getText().toString().equals(datas.child("name").getValue().toString())){
                                                trainercheck = false;
                                            } else {
                                                trainercheck = true;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                databaseReference.child("Room").child(room.getText().toString()).child("user").orderByChild("name").equalTo(name.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int count = 0;
                                        int count2 = 0;
                                        for(DataSnapshot datas: snapshot.getChildren()){
                                            if(name.getText().toString().equals(datas.child("name").getValue().toString())) {
                                                count++;
                                            }
                                             if(token.equals(datas.child("token").getValue().toString())) {
                                                count2++;
                                            }
                                          }
                                        if(count == 0 && trainercheck){
                                            if(roomCheck) {
                                                NameData nameData = new NameData(name.getText().toString(), token);
                                                databaseReference.child("Room").child(room.getText().toString()).child("user").push().setValue(nameData);
                                                ChatData chatData = new ChatData("입장했습니다.", getTime, name.getText().toString());  // 유저 이름과 메세지로 chatData 만들기
                                                databaseReference.child("Room").child(room.getText().toString()).child("message").push().setValue(chatData);
                                                databaseReference.child("Room").child(room.getText().toString()).child("into").push().setValue(chatData.getUserName());
                                                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent1);
                                                finish();
                                            }
                                        }else{
                                            if(count2 == 0 || databaseReference.child("Room").child(room.getText().toString()).child("trainer").orderByChild("name").equals(name.getText().toString())){
                                                Toast.makeText(getApplication(), getString(R.string.exist_name), Toast.LENGTH_SHORT).show();
                                                Log.e("Test", "token = "+FirebaseMessaging.getInstance().getToken());
                                            }
                                            else if(trainercheck){
                                                NameData nameData = new NameData(name.getText().toString(), token);
                                         //       databaseReference.child("Room").child(room.getText().toString()).child("user").push().setValue(nameData);
                                                ChatData chatData = new ChatData("입장했습니다.", getTime, name.getText().toString());  // 유저 이름과 메세지로 chatData 만들기
                                                databaseReference.child("Room").child(room.getText().toString()).child("message").push().setValue(chatData);
                                                databaseReference.child("Room").child(room.getText().toString()).child("into").push().setValue(chatData.getUserName());
                                                Print.e("Test", "Enter the room index1");
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent1);
                                                finish();
                                            }
                                        }
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else
                                Toast.makeText(getApplication(), getString(R.string.no_room), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }*/
        });

    } // onCreate end

    private void showPdf() {
        File destinationPath = new File(getExternalFilesDir(null), "/manual");
        File file = new File(destinationPath, "CREDO-MAN-003-AIO(rev.1.0)_Kor+Eng.pdf");
        // Get the URI Path of file.
        Uri uriPdfPath = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        // Start Intent to View PDF from the Installed Applications.
        Intent pdfOpenIntent = new Intent(Intent.ACTION_VIEW);
        pdfOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenIntent.setClipData(ClipData.newRawUri("", uriPdfPath));
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf");
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try {
            startActivity(pdfOpenIntent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(this, "There is no app to load corresponding PDF", Toast.LENGTH_LONG).show();

        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeServiceCPR.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BluetoothLeServiceCPR.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        bluetoothLeServiceCPR.disconnect();
        bluetoothLeServiceCPR = null;
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }


    private void getPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    1);
        }
    }

    //TODO Broadcast_Receiver
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (bluetoothLeServiceCPR.ACTION_BLE_CONNECTED.equals(action)) {
                connection_on(intent.getStringExtra(bluetoothLeServiceCPR.EXTRA_BLE_DEVICE_ADDRESS));
            } else if (bluetoothLeServiceCPR.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(bluetoothLeServiceCPR.EXTRA_DATA));
            }
        }
    };

    private void connection_on(String address) {
        if (Objects.equals(Devices.get("Device_01"), address)) {
            if (device_btn01 != null)
                device_btn01.setImageResource(R.drawable.band_on);
        } else if (Objects.equals(Devices.get("Device_02"), address)) {
            if (device_btn02 != null) {
                mConnected = true;
                Log.e("mConnected", "mConnected = " + mConnected);
                device_btn02.setImageResource(R.drawable.cpr_on);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private void locationPermissionCheck() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @io.reactivex.annotations.NonNull final String[] permissions,
                                           @io.reactivex.annotations.NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults, rxBleClient)) {
            scanBleDevices();
        }
    }

    TextView calibration_text;
    ImageView calibration_img;
    ViewFlipper viewFlipper;
    ImageView arrow_img;
    TextView magnet_text;
    ImageView magnet_arrow_img;

    private void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.setting_dialog, null);
        builder.setView(view);

        NeumorphButton init_btn = view.findViewById(R.id.init_btn);
        NeumorphButton magnet_btn = view.findViewById(R.id.magnet_btn);
        NeumorphButton setting_confirm = view.findViewById(R.id.setting_confirm);
        viewFlipper = view.findViewById(R.id.setting_viewFlipper);
        calibration_img = view.findViewById(R.id.calibration_img);
        calibration_text = view.findViewById(R.id.calibration_text);
        magnet_text = view.findViewById(R.id.magnet_text);
        arrow_img = view.findViewById(R.id.arrow_img);
        magnet_arrow_img = view.findViewById(R.id.magnet_arrow_img);
        LinearLayout setting_layout = view.findViewById(R.id.setting_layout);
        LinearLayout calibration_layout = view.findViewById(R.id.calibration_layout);
        LinearLayout magnet_layout = view.findViewById(R.id.magnet_layout);


        viewFlipper.removeAllViews();
        viewFlipper.addView(setting_layout, 0);
        viewFlipper.addView(magnet_layout, 1);
        viewFlipper.addView(calibration_layout, 2);

        arrow_img.setVisibility(View.INVISIBLE);

        final AlertDialog dialog = builder.create();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        /*arrow_img.setOnClickListener(v -> {
            arrow_img.setVisibility(View.INVISIBLE);
            Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
            sender.setAction(BluetoothLeServiceCPR.ACTION_CALIBRATION);
            startService(sender);
        });*/

        init_btn.setOnClickListener(v -> { // 186(BA)
            viewFlipper.showNext();
            magnet_text.setText(getString(R.string.magnet_put));
        });

        magnet_arrow_img.setOnClickListener(v -> {
            Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
            sender.setAction(BluetoothLeServiceCPR.ACTION_CALIBRATION_MAGNET);
            startService(sender);
        });

        magnet_btn.setOnClickListener(v -> {
            Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
            sender.setAction(BluetoothLeServiceCPR.ACTION_MAGNET);
            startService(sender);
        });
        setting_confirm.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    boolean isConnect = false;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    ;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 20000; //scan time 4000
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter02;
    private HashMap<String, String> Devices = new HashMap<>();
    private int scan_count = 0;
    private BluetoothGattCharacteristic mNotifyCharacteristic = null;
    private final ArrayList<String> mac_list = new ArrayList<>();
    NeumorphImageButton device_btn01;
    NeumorphImageButton device_btn02;

    private void showScanDialog() {
        isConnect = true;
        mConnected = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cpr_scan_layout, null);
        builder.setView(view);

        //블루투스 활성화 체크
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // 어텝터 초기화

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mLeDeviceListAdapter02 = new LeDeviceListAdapter();
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter02.clear();
        scanLeDevice(true);

        Device01 = sharedPreferences.getString("DeviceCPR_01", "-");
        Device02 = sharedPreferences.getString("DeviceCPR_02", "-");


        Devices.put("Device_01", Device01);
        Devices.put("Device_02", Device02);

        if (!Devices.isEmpty()) {
            for (RxBleDevice bluetoothDevice : mLeDeviceListAdapter.mLeDevices) {
                String address = bluetoothDevice.getMacAddress();
                if (Objects.equals(Devices.get("Device_01"), address)) {
                    bluetoothLeServiceCPR.connect(address, 0);
                }
            }
            for (RxBleDevice bluetoothDevice : mLeDeviceListAdapter02.mLeDevices) {
                String address = bluetoothDevice.getMacAddress();
                if (Objects.equals(Devices.get("Device_02"), address)) {
                    Log.e("Device02", address);
                    bluetoothLeServiceCPR.connect(address, 1);
                }
            }
        }

        final NeumorphButton band_dialog_reset = (NeumorphButton) view.findViewById(R.id.cpr_dialog_reset);
        final NeumorphButton band_dialog_layout = (NeumorphButton) view.findViewById(R.id.cpr_dialog_layout);
        device_btn01 = (NeumorphImageButton) view.findViewById(R.id.device_btn_cpr01);
        device_btn02 = (NeumorphImageButton) view.findViewById(R.id.device_btn_aio_01);

        final LinearLayout cpr_scan_reset = (LinearLayout) view.findViewById(R.id.cpr_scan_reset);

        band_dialog_reset.setVisibility(View.VISIBLE);
        band_dialog_layout.setVisibility(View.VISIBLE);

        if (!Devices.isEmpty()) {
            List<BluetoothDevice> devices = bluetoothManager.getConnectedDevices(BluetoothGatt.GATT);
            int status = -1;
            for (BluetoothDevice device : devices) {
                status = bluetoothManager.getConnectionState(device, BluetoothGatt.GATT);
                if (status == BluetoothProfile.STATE_CONNECTED) {
                    if (Devices.get("Device_01").equals(device.getAddress())) {
                        //mConnected = true;
                        device_btn01.setImageResource(R.drawable.band_on);
                    } else if (Devices.get("Device_02").equals(device.getAddress())) {
                        mConnected = true;
                        Log.e("mConnected", String.valueOf(mConnected));
                        device_btn02.setImageResource(R.drawable.cpr_on);
                        //initialize();
                    }
                }
            }
        }

        final AlertDialog dialog = builder.create();

        new Thread(() -> {
            while (isConnect) {
                try {
                    Thread.sleep(500);
                    scan_count++;
                    if (scan_count == 40) {
                        scanLeDevice(true);
                        scan_count = 0;
                    }
                    if (!Devices.isEmpty()) {
                        if (bluetoothLeServiceCPR != null) {
                            for (RxBleDevice bluetoothDevice : mLeDeviceListAdapter.mLeDevices) {
                                if ((Devices.get("Device_01") != null))
                                    if (Devices.get("Device_01").equals(bluetoothDevice.getMacAddress())) {
                                        if (bluetoothLeServiceCPR != null) {
                                            if (!bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                                                bluetoothLeServiceCPR.connect(bluetoothDevice.getMacAddress(), 0);
                                            }
                                        }
                                    }
                            }
                            if (!Devices.isEmpty())
                                for (RxBleDevice bluetoothDevice : mLeDeviceListAdapter02.mLeDevices) {
                                    if ((Devices.get("Device_02") != null))
                                        if (Devices.get("Device_02").equals(bluetoothDevice.getMacAddress())) {
                                            if (bluetoothLeServiceCPR != null) {
                                                if (!bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                                                    bluetoothLeServiceCPR.connect(bluetoothDevice.getMacAddress(), 1);
                                                }
                                            }
                                        }
                                }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        device_btn01.setOnClickListener(v -> {
            if ((Devices.get("Device_01") != null))
                if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                    Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
                    sender.setAction(BluetoothLeServiceCPR.ACTION_CALL);
                    sender.putExtra(BluetoothLeServiceCPR.DATA1_NOT_KEY, 0);
                    startService(sender);
                }
        });

        device_btn02.setOnClickListener(v -> {
            if ((Devices.get("Device_02") != null))
                if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                    Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
                    sender.setAction(BluetoothLeServiceCPR.ACTION_CALL);
                    sender.putExtra(BluetoothLeServiceCPR.DATA1_NOT_KEY, 1);
                    startService(sender);
                }
        });

        band_dialog_layout.setOnClickListener(v -> {
            isConnect = false;
            try {
                if (!Devices.isEmpty()) { //TODO BAND SET
                    if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_01"))) {
                        bluetoothLeServiceCPR.writeCharacteristic(0, "f3");
                    }
                    if (bluetoothLeServiceCPR.isConnected(Devices.get("Device_02"))) {
                        //bluetoothLeServiceCPR.writeCharacteristic(1, "f3");
                        showSettingDialog();
                    }
                }
            } catch (Exception e) {
            }
            dialog.dismiss();

        });

        cpr_scan_reset.setOnClickListener(v -> {
            isConnect = false;
            bluetoothLeServiceCPR.disconnect();
            Thread.interrupted();
            showScanDialog();
            dialog.dismiss();
        });

        band_dialog_reset.setOnClickListener(v -> {
            isConnect = false;
            bluetoothLeServiceCPR.disconnect();
            showAlertDialog();
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    private void showAlertDialog() {
        mConnected = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
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

            if (!mac_address_02.getText().toString().equals("")) {
                sharedPreferences.edit().putString("DeviceCPR_02", mac_address_02.getText().toString()).apply();
                Device02 = mac_address_02.getText().toString();
            } else {
                sharedPreferences.edit().putString("DeviceCPR_02", "-").apply();
            }

            dialog.dismiss();
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter02.clear();
            mac_list.clear();
            showScanDialog();

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
            showScanDialog();
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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

    private static ToBinary ToBinary;

    public static String getHexToDec(String hex) {
        ToBinary = new ToBinary(hex);
        return ToBinary.hexTobin();
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<RxBleDevice> mLeDevices;
        private LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<RxBleDevice>();
            mInflater = getLayoutInflater();
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

        @Override
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
                view = mInflater.inflate(R.layout.listitem_device, null);
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

    private void showReportList(final Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.report_layout, null);
        builder.setView(view);

        final ExpandableListView report_list_data = (ExpandableListView) view.findViewById(R.id.report_list_data);
        final Button report_print = (Button) view.findViewById(R.id.report_print);
        final Button report_cancel = (Button) view.findViewById(R.id.report_cancel);

        final AlertDialog dialog = builder.create();
        final List<Report> reports;

        AppDatabase database = AppDatabase.getDbInstance(getApplicationContext());
        reports = database.reportDao().getAllDevice();

        HashMap<String, String> keyItem = new HashMap<>();

        final HashMap<String, ArrayList<String>> reportList = new HashMap<>();

        for (Report report : reports) {
            String[] split = report.to_day.split("/");
            keyItem.put(split[0], "");
        }

        final ArrayList<String> date = new ArrayList<>(keyItem.keySet());

        Collections.sort(date);

        for (String date_ : date) {
            ArrayList<String> item = new ArrayList<>();
            for (Report report : reports) {
                String[] split = report.to_day.split("/");
                //if (date_.equals(report.to_day)) {
                if (date_.equals(split[0])) {
                    int all_score;
                    int breathScore = (int) ((Double.parseDouble(report.lung_correct) / Double.parseDouble(report.lung_num)) * 100);
                    int sum_depth = Integer.parseInt(report.report_depth_correct)
                            + Integer.parseInt(report.report_up_depth)
                            + Integer.parseInt(report.report_down_depth);

                    int depth_accuracy_ = (int) ((double) sum_depth / (double) 3);
                    if (Integer.parseInt(report.lung_num) != 0) {
                        all_score = (int) ((breathScore * 0.2) + (depth_accuracy_ * 0.8));
                    } else {
                        if (Integer.parseInt(report.position_num) != 0) {
                            all_score = depth_accuracy_;
                        } else {
                            all_score = Integer.parseInt(report.report_down_depth);
                        }
                    }
                    Log.e("Test", "name = " + report.report_name);
                    item.add(report.report_name + "," + report.report_depth_correct + "," + report.to_day + "," + all_score);
                }
            }
            Collections.sort(item);
            reportList.put(date_, item);
        }

        ReportAdapter arrayAdapter = new ReportAdapter(RoomActivity.this, date, reportList);
        report_list_data.setAdapter(arrayAdapter);

        report_list_data.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            //String day = date.get(groupPosition);
            String item = reportList.get(date.get(groupPosition)).get(childPosition);
            String[] split = item.split(",");
            String day = split[2];
            Intent main = new Intent(RoomActivity.this, ReportActivity.class);
            main.putExtra("ReportDB", true);
            main.putExtra("ReportDay", day);
            main.putExtra("ReportItem", item);
            startActivity(main);
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
            dialog.dismiss();
            activity.finish();

            return true;
        });

        report_print.setOnClickListener(v -> {
            Intent main = new Intent(RoomActivity.this, ReportActivity.class);
            main.putExtra("ReportDB", true);
            startActivity(main);
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
            dialog.dismiss();
            activity.finish();
        });

        report_cancel.setOnClickListener(v -> {
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private int position01;
    boolean isMax = false;
    boolean isMin = false;
    private String address01;
    private boolean isCali01 = false;
    String[] address_array01;
    double max_lung01 = 100;
    double min_lung01 = 64;

    private void displayData(String data) { //TODO DATA
        if (data != null) {
            String[] spil = data.split(",");
            if (Devices.get("Device_02").equals(spil[2])) {
                Print.e("Test", "uuid = " + spil[1] + ", spil[0] = " + Integer.parseInt(getHexToDec(spil[0])));
                switch (spil[1]) {
                    case "0000fff1-0000-1000-8000-00805f9b34fb":
                        position01 = Integer.parseInt(getHexToDec(spil[0]));
                        if (position01 == 193) {
                            Toast.makeText(RoomActivity.this, getString(R.string.magnet_normal), Toast.LENGTH_LONG).show();
                        } else if (position01 == 194) {
                            Toast.makeText(RoomActivity.this, getString(R.string.check_magnet), Toast.LENGTH_LONG).show();
                        } else if (position01 == 177) {
                            Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
                            sender.setAction(BluetoothLeServiceCPR.ACTION_CALIBRATION);
                            startService(sender);

                            calibration_text.setText(getString(R.string.second));
                            calibration_img.setImageResource(R.drawable.lung1);
                        } else if (position01 == 178) {
                            Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
                            sender.setAction(BluetoothLeServiceCPR.ACTION_CALIBRATION);
                            startService(sender);

                            calibration_text.setText(getString(R.string.third));
                            calibration_img.setImageResource(R.drawable.lung2);
                        } else if (position01 == 179) {
                            isMax = true;
                            calibration_text.setText(getString(R.string.complete));
                            calibration_img.setImageResource(R.drawable.lung3);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                viewFlipper.setDisplayedChild(0);
                                calibration_img.setImageResource(R.drawable.lung0);
                            }, 2000);
                            break;
                        } else if (position01 == 186) {
                            Toast.makeText(this, getString(R.string.complete_check_magnet), Toast.LENGTH_LONG).show();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                viewFlipper.showNext();
                                Intent sender = new Intent(RoomActivity.this, BluetoothLeServiceCPR.class);
                                sender.setAction(BluetoothLeServiceCPR.ACTION_CALIBRATION);
                                startService(sender);

                                calibration_text.setText(getString(R.string.first));
                            }, 500);
                            isMin = true;
                        } else if (position01 == 187) {
                            magnet_text.setText(getString(R.string.magnet_again));
                        }
                        if (isMax) {
                            if (position01 != 179) {
                                isMax = false;
                                cali_map.put(spil[2], min_lung01 + "/" + position01);
                                String addressjson = new Gson().toJson(cali_map);
                                sharedPreferences.edit().putString("address", addressjson).apply();
                                max_lung01 = position01;
                            }
                        }
                        if (isMin) {
                            if (position01 != 186) {
                                isMin = false;
                                cali_map.put(spil[2], String.valueOf(position01));
                                min_lung01 = position01;
                            }
                        }
                        break;
                    case "0000fff2-0000-1000-8000-00805f9b34fb":
                    case "0000fff3-0000-1000-8000-00805f9b34fb":
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + spil[1]);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    // 이미지 표시

                    Bitmap resized = Bitmap.createScaledBitmap(img, 500, 500, true);

                    String image = BitMapToString(resized);

                    SharedPreferences pref = getSharedPreferences("Pref", MODE_PRIVATE);

                    SharedPreferences.Editor editor = pref.edit();

                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    return;
                                }
                                // Get new FCM registration token
                                token = task.getResult();
                            });

                    String intoName = name_first.getText().toString();
                    if (twoMode) {
                        intoName = intoName + "&" + name_second.getText().toString();
                    }
                    String key = databaseReference.child("TOKEN").child(intoName).getKey();
                    User user = new User("android", token, intoName, image);
                    Map<String, Object> postValues = user.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/TOKEN/" + key, postValues);

                    databaseReference.updateChildren(childUpdates);

                    editor.putString("image", image);

                    editor.apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String BitMapToString(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);

    }

    public Bitmap StringToBitMap(String encodedString) {

        try {

            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);

            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

        } catch (Exception e) {

            e.getMessage();

            return null;
        }
    }

}

