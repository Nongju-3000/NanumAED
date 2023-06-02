package com.wook.web.lighten.aio_client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wook.web.lighten.aio_client.R;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.wook.web.lighten.aio_client.adapter.ReportAdapter;
import com.wook.web.lighten.aio_client.ble.BluetoothLeServiceCPR;
import com.wook.web.lighten.aio_client.data.LabelFormatter;
import com.wook.web.lighten.aio_client.db.AppDatabase;
import com.wook.web.lighten.aio_client.db.Converters;
import com.wook.web.lighten.aio_client.db.Report;
import com.wook.web.lighten.aio_client.utils.Print;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import soup.neumorphism.NeumorphCardView;

public class ReportActivity extends Activity {

    private BackPressCloseHandler backPressCloseHandler;

    private LineChart chart;
    private TextView ventil_score;
    private RecyclerView recyclerView;
    private TextView date;
    private TextView time;

    //Sesstion

    private TextView report_end_time;
    private TextView report_interval_sec;
    private TextView report_cycle;
    private TextView report_depth_correct;

    //Compression

    private Button report_up_depth;
    private Button report_down_depth;
    private TextView report_bpm;
    private TextView report_angle;
    private TextView report_total, slash;
    private TextView ave_depth;

    private TextView equip;

    private ArrayList<Float> chart_item, presstime_list, breathval, breathtime;
    private ArrayList<ReportItem> reportItems;
    private ArrayList<String> names;

    private TextView all_accuracy;
    private TextView breath_accuracy;
    private TextView depth_accuracy;

    private ImageView report_angleguage;
    private LinearLayout up_frame;
    private NeumorphCardView cprCardView, sessionCardView, ventilationCardView, switchCardView, timeCardView;
    private Dialog ventilDialog;
    private Dialog cprDialog;
    private Dialog sessionDialog;
    private boolean isOpen = false;
    private boolean isVentil = false;

    private ImageView lung;
    private ClipDrawable lung_clip;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    private int depth_accuracy_ = 0;
    private String depth_score;
    private int div_time;
    private int total_score;
    private String min;
    private String max;

    ArrayList<Integer> stop_array = new ArrayList<>();
    ArrayList<Float> stop_xval = new ArrayList<Float>();
    boolean isover = false;

    //private TextView showCount;

    private TextView breath_break, chart_break, depth_break;
    private float prev_val = 0f;
    private float prev_xval = 0f;
    private float prev_bval = 0f;
    private float prev_bxval = 0f;

    private TextView totalCount, correctCount, wrongCount;

    private ImageButton mg_glass;
    private ImageView breath_accuracy_img, depth_accuracy_img, report_end_time_img, report_interval_img, cycle_img, depth_correct_img;
    private BluetoothLeServiceCPR bluetoothLeServiceCPR;
    ArrayList<Float> stop_list = new ArrayList<>();
    ArrayList<Float> ble_list = new ArrayList<>();

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

    private void setDateTextView(TextView date, String rawdate){
        String year, month, day;

        year = String.valueOf(rawdate.charAt(0))+ rawdate.charAt(1) + rawdate.charAt(2) + rawdate.charAt(3);
        month = String.valueOf(rawdate.charAt(4))+ rawdate.charAt(5);
        day = String.valueOf(rawdate.charAt(6))+ rawdate.charAt(7);

        date.setText(year+"."+month+"."+day);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size); // 1600, 2458

        Intent gattServiceIntent = new Intent(this, BluetoothLeServiceCPR.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        backPressCloseHandler = new BackPressCloseHandler(this);

        chart = findViewById(R.id.chart_img);
        recyclerView = findViewById(R.id.report_list);
        ventil_score = findViewById(R.id.chart_img_02);

        report_end_time = (TextView) findViewById(R.id.report_end_time);
        report_interval_sec = (TextView) findViewById(R.id.report_interval_sec);
        report_cycle = (TextView) findViewById(R.id.report_cycle);
        report_depth_correct = (TextView) findViewById(R.id.report_depth_correct);

        report_up_depth = (Button) findViewById(R.id.report_up_depth);
        report_down_depth = (Button) findViewById(R.id.report_down_depth);
        report_bpm = (TextView) findViewById(R.id.report_bpm);
        report_angle = (TextView) findViewById(R.id.report_angle);
        report_total = (TextView) findViewById(R.id.report_total);
        report_angleguage = findViewById(R.id.report_angleguage);
        mg_glass = findViewById(R.id.mg_glass);

        all_accuracy = findViewById(R.id.all_accuracy);
        depth_accuracy = findViewById(R.id.depth_accuracy);
        ave_depth = findViewById(R.id.ave_depth);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        lung = findViewById(R.id.lung_score_image);
        lung_clip = (ClipDrawable) lung.getDrawable();
        lung_clip.setLevel(0);
        slash = findViewById(R.id.slash);
        equip = findViewById(R.id.equip);

        //totalCount = findViewById(R.id.totalCount);
        correctCount = findViewById(R.id.correctCount);
        //wrongCount = findViewById(R.id.wrongCount);

        cprCardView = findViewById(R.id.cprCardView);
        sessionCardView = findViewById(R.id.sessionCardView);
        ventilationCardView = findViewById(R.id.ventilationCardView);
        timeCardView = findViewById(R.id.timeCardView);

        up_frame = findViewById(R.id.up_frame);

        //breath_break = findViewById(R.id.breath_break);
        //breath_break.setOnClickListener(v -> Toast.makeText(getApplication(), "AIO를 사용하셔야 활성화됩니다.", Toast.LENGTH_SHORT).show());
        chart_break = findViewById(R.id.chart_break);
        //chart_break.setOnClickListener(v -> Toast.makeText(getApplication(), "AIO를 사용하셔야 활성화됩니다.", Toast.LENGTH_SHORT).show());
        depth_break = findViewById(R.id.depth_break);
        //depth_break.setOnClickListener(v -> Toast.makeText(getApplication(), "AIO를 사용하셔야 활성화됩니다.", Toast.LENGTH_SHORT).show());

        cprCardView.post(()->{
            createDialog();
        });

        sessionCardView.setOnClickListener(v -> {
            createDialog();
            if(!isOpen){
                if(isVentil) {
                    ventilDialog.show();
                }
                cprDialog.show();
                sessionDialog.show();
                isOpen = true;
            }
        });

        cprCardView.setOnClickListener(v -> {
            createDialog();
            if(!isOpen){
                sessionDialog.show();
                if(isVentil) {
                    ventilDialog.show();
                }
                cprDialog.show();
                isOpen = true;
            }
        });
        ventilationCardView.setOnClickListener(v -> {
            createDialog();
            if(!isOpen){
                sessionDialog.show();
                ventilDialog.show();
                cprDialog.show();
                isOpen = true;
            }
        });

        /*sessionCardView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_session);
            NeumorphCardView info_sessionCardView = dialog.findViewById(R.id.info_sessionCardView);
            info_sessionCardView.setLayoutParams(new FrameLayout.LayoutParams(sessionCardView.getWidth(), sessionCardView.getHeight()));
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = cprCardView.getWidth();
            wmlp.y = up_frame.getHeight();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            info_sessionCardView.setOnClickListener(ev -> {
                dialog.dismiss();
            });
        });

        cprCardView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Log.d("getwidth", String.valueOf(cprCardView.getWidth()));
            dialog.setContentView(R.layout.info_accuracy);
            TextView all_accuracy = dialog.findViewById(R.id.all_accuracy);
            all_accuracy.setText(total_score + "%");
            NeumorphCardView info_cprCardView = dialog.findViewById(R.id.info_cprCardView);
            info_cprCardView.setLayoutParams(new FrameLayout.LayoutParams(cprCardView.getWidth(), cprCardView.getHeight()));
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = 0;
            wmlp.y = up_frame.getHeight();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            info_cprCardView.setOnClickListener(ev -> {
                dialog.dismiss();
            });
        });

        ventilationCardView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_ventil);
            NeumorphCardView info_ventilationCardView = dialog.findViewById(R.id.info_ventilationCardView);
            TextView ventil_count = dialog.findViewById(R.id.ventil_count);
            TextView ventil_volume = dialog.findViewById(R.id.ventil_volume);
            TextView ave_volume = dialog.findViewById(R.id.ave_volume);
            info_ventilationCardView.setLayoutParams(new FrameLayout.LayoutParams(ventilationCardView.getWidth(), ventilationCardView.getHeight()));
            ventil_count.setText(reportItems.get(0).getReport_lung_num());
            ventil_volume.setText(reportItems.get(0).getReport_ventil_volume() + "ml");
            int num = Integer.parseInt(reportItems.get(0).getReport_lung_num());
            int volume = Integer.parseInt(reportItems.get(0).getReport_ventil_volume());
            if(num != 0)
                ave_volume.setText((volume / num)+ "ml");
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = 10000;
            wmlp.y = up_frame.getHeight();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            info_ventilationCardView.setOnClickListener(ev -> {
                dialog.dismiss();
            });
        });*/

        Intent intent = getIntent();

        Boolean ReportDB = intent.getBooleanExtra("ReportDB", false);
        String ReportItem = intent.getStringExtra("ReportItem");
        String ReportDay = intent.getStringExtra("ReportDay");

        String UserName = intent.getStringExtra("UserName");
        String room = intent.getStringExtra("room");

        names = new ArrayList<>();

        reportItems = (ArrayList<ReportItem>) intent.getSerializableExtra("reportItems");

        mg_glass.setOnClickListener(v -> showReportList(ReportActivity.this));

        if (ReportDB) {
            List<Report> reports;

            AppDatabase database = AppDatabase.getDbInstance(getApplicationContext());
            reports = database.reportDao().getAllDevice();

            Converters converters = new Converters();

            Collections.reverse(reports);

            reportItems = new ArrayList<>();

            if (ReportItem != null && ReportDay != null) {
                for (Report report : reports)
                    if (ReportDay.equals(report.to_day)) {
                        String[] split = report.to_day.split("/");
                        setDateTextView(date,split[0]);
                        String[] timesplit = split[1].split(":");
                        int time_sec = Integer.parseInt(timesplit[0]) * 3600 + Integer.parseInt(timesplit[1]) * 60 + Integer.parseInt(timesplit[2]);
                        time_sec = time_sec + Integer.parseInt( report.report_end_time);
                        String hour = String.format("%02d",time_sec / 3600);
                        String min = String.format("%02d",(time_sec % 3600) / 60);
                        String sec = String.format("%02d",time_sec % 60);
                        time.setText(split[1] + " - " + hour +":"+ min +":"+ sec);
                        String item[] = ReportItem.split(",");
                        if (item[0].equals(report.report_name) && item[1].equals(report.report_depth_correct)) {
                            reportItems.add(new ReportItem(report.report_name
                                    , report.report_end_time
                                    , report.report_interval_sec
                                    , report.report_cycle
                                    , report.report_depth_correct
                                    , report.report_up_depth
                                    , report.report_down_depth
                                    , report.report_bpm
                                    , report.report_angle
                                    , (ArrayList<Float>) converters.gettingListFromString(report.report_depth_list)
                                    , (ArrayList<Float>) converters.gettingListFromString(report.report_presstimeList)
                                    , (ArrayList<Float>) converters.gettingListFromString(report.report_breathtime)
                                    , (ArrayList<Float>) converters.gettingListFromString(report.report_breathval)
                                    , report.report_ventil_volume
                                    , report.min
                                    , report.max
                                    , report.depth_num
                                    , report.depth_correct
                                    , report.position_num
                                    , report.position_correct
                                    , report.lung_num
                                    , report.lung_correct
                                    , (ArrayList<Float>) converters.gettingListFromString(report.stop_time_list)
                                    , (ArrayList<Float>) converters.gettingListFromString(report.report_bluetoothtime)
                            ));
                        }
                    }
            } else {
                for (Report report : reports) {
                    reportItems.add(new ReportItem(report.report_name
                            , report.report_end_time
                            , report.report_interval_sec
                            , report.report_cycle
                            , report.report_depth_correct
                            , report.report_up_depth
                            , report.report_down_depth
                            , report.report_bpm
                            , report.report_angle
                            , (ArrayList<Float>) converters.gettingListFromString(report.report_depth_list)
                            , (ArrayList<Float>) converters.gettingListFromString(report.report_presstimeList)
                            , (ArrayList<Float>) converters.gettingListFromString(report.report_breathtime)
                            , (ArrayList<Float>) converters.gettingListFromString(report.report_breathval)
                            , report.report_ventil_volume
                            , report.min
                            , report.max
                            , report.depth_num
                            , report.depth_correct
                            , report.position_num
                            , report.position_correct
                            , report.lung_num
                            , report.lung_correct
                            , (ArrayList<Float>) converters.gettingListFromString(report.stop_time_list)
                            , (ArrayList<Float>) converters.gettingListFromString(report.report_bluetoothtime)
                    ));
                }
            }
        }else{
            reportItems = (ArrayList<ReportItem>) intent.getSerializableExtra("reportItems");
            ReportSave(reportItems);
        }

        if (reportItems != null && reportItems.size() > 0) {

            report_end_time.setText(reportItems.get(0).getReport_end_time() + " sec");
            report_interval_sec.setText(reportItems.get(0).getReport_interval_sec() + " sec");
            report_cycle.setText(reportItems.get(0).getReport_cycle());
            if(Integer.parseInt(reportItems.get(0).getReport_position_num()) == 0) {
                report_depth_correct.setVisibility(View.GONE);
                depth_break.setVisibility(View.VISIBLE);
                report_up_depth.setText("-");
            }
            else {
                depth_break.setVisibility(View.GONE);
                report_depth_correct.setVisibility(View.VISIBLE);
                report_depth_correct.setText(reportItems.get(0).getReport_depth_correct() + " %");
                report_up_depth.setText(reportItems.get(0).getReport_up_depth() + " %");
                if(Integer.parseInt( reportItems.get(0).getReport_up_depth()) >= 70){
                    report_up_depth.setTextColor(Color.GREEN);
                }
                else if(Integer.parseInt( reportItems.get(0).getReport_up_depth()) >= 30){
                    report_up_depth.setTextColor(Color.rgb(255,100,0));
                }
                else if(Integer.parseInt( reportItems.get(0).getReport_up_depth()) >= 0){
                    report_up_depth.setTextColor(Color.RED);
                }
            }
            report_down_depth.setText(reportItems.get(0).getReport_down_depth() + " %");
            if(Integer.parseInt( reportItems.get(0).getReport_down_depth()) >= 70){
                report_down_depth.setTextColor(Color.GREEN);
            }
            else if(Integer.parseInt( reportItems.get(0).getReport_down_depth()) >= 30){
                report_down_depth.setTextColor(Color.rgb(255,100,0));
            }
            else if(Integer.parseInt( reportItems.get(0).getReport_down_depth()) >= 0){
                report_down_depth.setTextColor(Color.RED);
            }
            report_bpm.setText(reportItems.get(0).getReport_bpm() + " BPM");
            int angle = 90 - Integer.parseInt(reportItems.get(0).getReport_angle());
            if( angle > 60 && angle < 120) {
                report_angle.setText(getString(R.string.correct));
                report_angleguage.setBackground(getDrawable(R.drawable.angle_green));
            }
            else if( angle > 30 && angle < 150){
                report_angle.setText(getString(R.string.incorrect));
                report_angleguage.setBackground(getDrawable(R.drawable.angle_orange));

            }
            else{
                report_angle.setText(getString(R.string.jiggle));
                report_angleguage.setBackground(getDrawable(R.drawable.angle_red));
            }
            report_total.setText(reportItems.get(0).getDepth_num());

            if(Integer.parseInt(reportItems.get(0).getReport_angle()) == 0 && (Integer.parseInt(reportItems.get(0).getReport_position_num()) == 0 && Integer.parseInt(reportItems.get(0).getReport_lung_num()) == 0)){
                equip.setText("");
            }else if(Integer.parseInt(reportItems.get(0).getReport_angle()) != 0 && Integer.parseInt(reportItems.get(0).getReport_position_num()) == 0 && Integer.parseInt(reportItems.get(0).getReport_lung_num()) == 0){
                equip.setText("BAND");
            }else if(Integer.parseInt(reportItems.get(0).getReport_angle()) == 0 && (Integer.parseInt(reportItems.get(0).getReport_position_num()) != 0 || Integer.parseInt(reportItems.get(0).getReport_lung_num()) != 0)){
                equip.setText("AIO");
                report_angle.setText("BLOCK");
                report_angleguage.setBackground(getDrawable(R.drawable.angle_gray));
            }else if(Integer.parseInt(reportItems.get(0).getReport_angle()) != 0 && (Integer.parseInt(reportItems.get(0).getReport_position_num()) != 0 || Integer.parseInt(reportItems.get(0).getReport_lung_num()) != 0)){
                equip.setText("BAND, AIO");
            }

            chart_item = reportItems.get(0).getReport_depth_list();

            int sum_depth = Integer.parseInt(reportItems.get(0).getReport_depth_correct())
                    + Integer.parseInt(reportItems.get(0).getReport_up_depth())
                    + Integer.parseInt(reportItems.get(0).getReport_down_depth());

            depth_accuracy_ = (int) ((double) sum_depth / (double) 3);
            if(Integer.parseInt(reportItems.get(0).getReport_position_num()) == 0) {
                depth_accuracy.setText(reportItems.get(0).getReport_down_depth() + " %");
            }
            else {
                depth_accuracy.setText(depth_accuracy_ + " %");
            }

            depth_score = reportItems.get(0).getReport_depth_correct();
            presstime_list = reportItems.get(0).getReport_presstime_list();
            breathval = reportItems.get(0).getReport_breathval();
            breathtime = reportItems.get(0).getReport_breathtime();

            div_time = Integer.parseInt(reportItems.get(0).getReport_end_time());

            //showCount.setText(reportItems.get(0).getDepth_correct()+"/"+reportItems.get(0).getDepth_num());

            int depth_num = Integer.parseInt(reportItems.get(0).getDepth_num());
            int depth_correct = Integer.parseInt(reportItems.get(0).getDepth_correct());
            int depth_wrong = depth_num - depth_correct;

            //totalCount.setText(reportItems.get(0).getDepth_num());
            correctCount.setText(reportItems.get(0).getDepth_correct());
            //wrongCount.setText(String.valueOf(depth_wrong));

            min = reportItems.get(0).getReport_Min();
            max = reportItems.get(0).getReport_Max();

            stop_list = reportItems.get(0).getStop_time_list();
            ble_list = reportItems.get(0).getReport_bletime_list();

            report_total.post(()->{
                float correctCountTextSize = correctCount.getTextSize()*0.8f;
                float report_totalTextSize = report_total.getTextSize()*0.8f;
                Log.d("CountTextSize", String.valueOf(correctCountTextSize) + ", " + report_totalTextSize);
                if (correctCountTextSize > report_totalTextSize){
                    report_total.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
                    correctCount.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
                    slash.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
                    report_total.setTextSize(TypedValue.COMPLEX_UNIT_PX, report_totalTextSize);
                    correctCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, report_totalTextSize);
                    slash.setTextSize(TypedValue.COMPLEX_UNIT_PX, report_totalTextSize);
                } else {
                    correctCount.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
                    report_total.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
                    slash.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
                    correctCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, correctCountTextSize);
                    report_total.setTextSize(TypedValue.COMPLEX_UNIT_PX, correctCountTextSize);
                    slash.setTextSize(TypedValue.COMPLEX_UNIT_PX, correctCountTextSize);
                }
            });

            for (ReportItem reportItem : reportItems)
                names.add("   " + reportItem.getReport_name() + "   ");
            setChart(0);

        } else {
            Intent main = new Intent(ReportActivity.this, RoomActivity.class);
            startActivity(main);
            finish();
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
        }


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListViewAdapterReport adapter = new ListViewAdapterReport(names);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        bluetoothLeServiceCPR = null;
        sessionDialog.dismiss();
        ventilDialog.dismiss();
        cprDialog.dismiss();
    }

    private void ReportSave(ArrayList<ReportItem> reportItems) {
        if (reportItems.size() != 0 && reportItems != null) {
            AppDatabase database = AppDatabase.getDbInstance(ReportActivity.this);
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

                Calendar cal = Calendar.getInstance();
                Date nowDate = cal.getTime();
                SimpleDateFormat dataformat = new SimpleDateFormat("yyyyMMdd");
                String toDay = dataformat.format(nowDate);
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

                database.reportDao().insert(report);

                String[] split = toDay.split("/");
                setDateTextView(date,split[0]);
                String[] timesplit = split[1].split(":");
                int time_sec = Integer.parseInt(timesplit[0]) * 3600 + Integer.parseInt(timesplit[1]) * 60 + Integer.parseInt(timesplit[2]);
                Log.d("interval", report.report_end_time);
                time_sec = time_sec + Integer.parseInt( report.report_end_time);
                String hour = String.format("%02d",time_sec / 3600);
                String min = String.format("%02d",(time_sec % 3600) / 60);
                String sec = String.format("%02d",time_sec % 60);
                time.setText(split[1] + " - " + hour +":"+ min +":"+ sec);
            }
        }
    }

    private TextView ventil_count;
    private TextView ventil_volume;
    private TextView ave_volume;

    private void createDialog(){
        ventilDialog = new Dialog(ReportActivity.this);
        ventilDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ventilDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        ventilDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ventilDialog.setContentView(R.layout.info_ventil);

        ventilDialog.setOnDismissListener(dialog -> {
            if(sessionDialog.isShowing())
                sessionDialog.dismiss();
            if(cprDialog.isShowing())
                cprDialog.dismiss();
            isOpen = false;
        });

        NeumorphCardView info_ventilationCardView = ventilDialog.findViewById(R.id.info_ventilationCardView);
        ventil_count = ventilDialog.findViewById(R.id.ventil_count);
        ventil_volume = ventilDialog.findViewById(R.id.ventil_volume);
        ave_volume = ventilDialog.findViewById(R.id.ave_volume);
        info_ventilationCardView.setLayoutParams(new FrameLayout.LayoutParams(ventilationCardView.getWidth(), ventilationCardView.getHeight()));
        Log.e("ventilDialog", "ventilDialog");

        ventil_count.setText(reportItems.get(0).getReport_lung_num());
        ventil_volume.setText(reportItems.get(0).getReport_ventil_volume() + "ml");
        int num = Integer.parseInt(reportItems.get(0).getReport_lung_num());
        int volume = Integer.parseInt(reportItems.get(0).getReport_ventil_volume());
        if(num != 0)
            ave_volume.setText((volume / num)+ "ml");
        WindowManager.LayoutParams wmlp = ventilDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        wmlp.x = 10000;
        wmlp.y = up_frame.getHeight();

        ventilDialog.setCanceledOnTouchOutside(true);
        //       ventilDialog.show();
        info_ventilationCardView.setOnClickListener(ev -> {
            sessionDialog.hide();
            ventilDialog.hide();
            cprDialog.hide();
            isOpen = false;
        });

        cprDialog = new Dialog(ReportActivity.this);
        cprDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cprDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        cprDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cprDialog.setContentView(R.layout.info_accuracy);

        cprDialog.setOnDismissListener(dialog -> {
            if(sessionDialog.isShowing())
                sessionDialog.dismiss();
            if(ventilDialog.isShowing())
                ventilDialog.dismiss();
            isOpen = false;
        });

        TextView all_accuracy = cprDialog.findViewById(R.id.all_accuracy);
        all_accuracy.setText(total_score + "%");
        NeumorphCardView info_cprCardView = cprDialog.findViewById(R.id.info_cprCardView);
        info_cprCardView.setLayoutParams(new FrameLayout.LayoutParams(cprCardView.getWidth(), cprCardView.getHeight()));
        wmlp = cprDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        wmlp.x = 0;
        wmlp.y = up_frame.getHeight();
        cprDialog.setCanceledOnTouchOutside(true);
        // cprDialog.show();
        info_cprCardView.setOnClickListener(ev -> {
            sessionDialog.hide();
            ventilDialog.hide();
            cprDialog.hide();
            isOpen = false;
        });

        sessionDialog = new Dialog(ReportActivity.this);
        sessionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        sessionDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        sessionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sessionDialog.setContentView(R.layout.info_session);
        NeumorphCardView info_sessionCardView = sessionDialog.findViewById(R.id.info_sessionCardView);

        sessionDialog.setOnDismissListener(dialog -> {
            if(ventilDialog.isShowing())
                ventilDialog.dismiss();
            if(cprDialog.isShowing())
                cprDialog.dismiss();
            isOpen = false;
        });

        info_sessionCardView.setLayoutParams(new FrameLayout.LayoutParams(sessionCardView.getWidth(), sessionCardView.getHeight()));
        wmlp = sessionDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        wmlp.x = cprCardView.getWidth();
        wmlp.y = up_frame.getHeight();

        sessionDialog.setCanceledOnTouchOutside(true);
        //sessionDialog.show();
        info_sessionCardView.setOnClickListener(ev -> {
            sessionDialog.hide();
            ventilDialog.hide();
            cprDialog.hide();
            isOpen = false;
        });
    }

    private void setChart(int position) {
        chart.clear();
        chart.fitScreen();
        chart.setDescription(null);
        chart.setTouchEnabled(true);
        chart.setNoDataText("Loading...");
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setDrawBorders(false);
        chart.setAutoScaleMinMaxEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        BarLineChartTouchListener barLineChartTouchListener = (BarLineChartTouchListener) chart.getOnTouchListener();
        barLineChartTouchListener.stopDeceleration();
        float scale = div_time / 15;
        chart.zoomToCenter(scale/2, 0f);

        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(!(chart.getLowestVisibleX() == chart.getXAxis().getAxisMinimum() || chart.getHighestVisibleX() == chart.getXAxis().getAxisMaximum())){
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP: {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                        }
                        case MotionEvent.ACTION_DOWN: {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;
                        }
                        case  MotionEvent.ACTION_MOVE:{
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;
                        }
                    }
                }
                return false;
            }
        });

        LineData data = new LineData();
        ILineDataSet set = createSet();
        ILineDataSet set1 = createSet1();
        //ILineDataSet set2 = createSet2();

        data.addDataSet(set);
        data.addDataSet(set1);
        //data.addDataSet(set2);

        chart.animateX(500);
        chart.setData(data);

        LimitLine ll0 = new LimitLine(0f, " ");
        ll0.setLineWidth(1f);
        ll0.enableDashedLine(10f, 0f, 0f);
        ll0.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll0.setLineColor(Color.WHITE);
        ll0.setTextSize(10f);

        LimitLine ll1 = new LimitLine(Float.parseFloat(max), " ");
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(10f, 0f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setLineColor(ContextCompat.getColor(ReportActivity.this, R.color.lineColor));
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(Float.parseFloat(min), " ");
        ll2.setLineWidth(2f);
        ll2.enableDashedLine(10f, 0f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        ll2.setLineColor(ContextCompat.getColor(ReportActivity.this, R.color.lineColor));
        ll2.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setEnabled(true);
        // leftAxis.setGridColor(ContextCompat.getColor(ReportActivity.this, R.color.lineColor));
        leftAxis.setGridColor(Color.TRANSPARENT);
        //  leftAxis.setGridLineWidth(1);
        leftAxis.setTextColor(Color.WHITE);
        //leftAxis.setLabelCount();
        leftAxis.setShowSpecificLabelPositions(true);
        leftAxis.setSpecificLabelPositions(new float[]{0f, Float.parseFloat(min), Float.parseFloat(max)});
        leftAxis.addLimitLine(ll0);
        /*leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);*/
        leftAxis.setDrawLimitLinesBehindData(true);
        //leftAxis.setGranularity(30f);
        // leftAxis.setLabelCount(1);
        // leftAxis.setValueFormatter(new YLabelFormatter(min, max));
        leftAxis.setZeroLineWidth(1f);
        leftAxis.setZeroLineColor(Color.WHITE);
        leftAxis.setAxisMaximum(70f);
        leftAxis.setAxisMinimum(-20f);
        leftAxis.setInverted(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);

        chart.invalidate();

        int breath_ = 0;
        int brath = 0;
        int depth = 0;
        int depth_sum = 0;
        int depth_count = 0;
        int index = 0;
        int indax = 0;
        int chart_size = chart_item.size();

        float pre_item = 0f;
        float pre_xval = 0f;

        for(float itam: breathval) {
            if (itam > 0) {
                addEntry_chart(itam, breathtime.get(indax), 1);
            }
            else{
                addEntry_chart(itam * (-1) - 5.0f, breathtime.get(indax), 2);
            }
            indax++;
        }

        Log.e("presstime", String.valueOf(presstime_list));
        int j = 0;
        for (float item : chart_item) {
            if (item >= Integer.parseInt(min) && item <= Integer.parseInt(max)) {
                try {
                    if(pre_item != 0) {
                        addEntry_chart(0, (presstime_list.get(index) - presstime_list.get(index - 1)) / 2 + presstime_list.get(index - 1), 0);
                        addEntry_chart(item, presstime_list.get(index), 0);
                    }
                    else{
                        addEntry_chart(0, pre_xval, 0);
                        addEntry_chart(item, presstime_list.get(index), 0);
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    addEntry_chart(0, 0, 0);
                    addEntry_chart(item, presstime_list.get(index), 0);
                }
                depth++;
                depth_sum += item;
                depth_count++;
            } else if ((5 < item && item < Integer.parseInt(min))) {
                try{
                    if(pre_item!=0) {
                        addEntry_chart(0, (presstime_list.get(index) - presstime_list.get(index - 1)) / 2 + presstime_list.get(index - 1), 0);
                        addEntry_chart(item, presstime_list.get(index), 0);
                    }
                    else{
                        addEntry_chart(0, pre_xval, 0);
                        addEntry_chart(item, presstime_list.get(index), 0);
                    }
                } catch (ArrayIndexOutOfBoundsException e){
                    addEntry_chart(0, 0, 0);
                    addEntry_chart(item, presstime_list.get(index), 0);
                }
                /*addEntry_chart(0, 150);
                addEntry_chart(item, 150);*/
                depth++;
                depth_sum += item;
                depth_count++;
            } else if ((Integer.parseInt(max) < item && item <= 100)) {
                try{
                    if( pre_item !=0 ) {
                        addEntry_chart(0, (presstime_list.get(index) - presstime_list.get(index - 1)) / 2 + presstime_list.get(index - 1), 0);
                        addEntry_chart(70, presstime_list.get(index), 0);
                    }
                    else{
                        addEntry_chart(0, pre_xval, 0);
                        addEntry_chart(70, presstime_list.get(index), 0);
                    }
                } catch (ArrayIndexOutOfBoundsException e){
                    addEntry_chart(0, 0, 0);
                    addEntry_chart(70, presstime_list.get(index), 0);
                }
                depth++;
                depth_sum += item;
                depth_count++;
            } else if (100 < item) {
                try{
                    if(pre_item !=0 ) {
                        addEntry_chart(10, (presstime_list.get(index) - presstime_list.get(index - 1)) / 2 + presstime_list.get(index - 1), 0);
                        addEntry_chart((item - 100), presstime_list.get(index), 0);
                    }
                    else{
                        addEntry_chart(10, pre_xval, 0);
                        addEntry_chart((item - 100), presstime_list.get(index), 0);
                    }
                } catch (ArrayIndexOutOfBoundsException e){
                    addEntry_chart(10, 0, 0);
                    addEntry_chart((item - 100), presstime_list.get(index), 0);
                }
                depth++;
            } else if (0 == item) {
                try {
                    addEntry_chart(0, (presstime_list.get(index - 1) - presstime_list.get(index - 2)) / 2 + presstime_list.get(index - 1), 0);
                    addEntry_chart(0, presstime_list.get(index - 1)  + (float)stop_list.get(j), 0);
                    pre_xval = presstime_list.get(index - 1)  + (float)stop_list.get(j);
                    stop_xval.add((presstime_list.get(index - 1) - presstime_list.get(index - 2)) / 2 + presstime_list.get(index - 1));
                    stop_xval.add(presstime_list.get(index - 1)  + (float)stop_list.get(j));
                } catch (ArrayIndexOutOfBoundsException e){
                    try{
                        addEntry_chart(0, presstime_list.get(index - 1) + presstime_list.get(index - 1), 0);
                        addEntry_chart(0, presstime_list.get(index - 1) + (float)stop_list.get(j), 0);
                        pre_xval = presstime_list.get(index - 1) + (float)stop_list.get(j);
                        stop_xval.add(presstime_list.get(index - 1) + presstime_list.get(index - 1));
                        stop_xval.add(presstime_list.get(index - 1) + (float)stop_list.get(j));
                    } catch (ArrayIndexOutOfBoundsException e_){
                        addEntry_chart(0, 0, 0);
                        addEntry_chart(0, 0f + stop_list.get(j), 0);
                        pre_xval = 0f + stop_list.get(j);
                        stop_xval.add(0f);
                        stop_xval.add(0f + stop_list.get(j));
                    }
                }
                depth++;
                index--;
                j++;
            } else if (item < 5) {
                switch ((int) item) {
                    case 1:
                        breath_++;
                        depth++;
                        index--;
                        break;
                    case 2:
                        depth++;
                        brath++;
                        breath_++;
                        index--;
                        break;
                    case 3:
                        depth++;
                        brath++;
                        breath_++;
                        index--;
                        break;
                    case 4:
                        depth++;
                        breath_++;
                        index--;
                        break;
                }
            }
            index++;
            if(index == chart_size){
                if(item < 5){
                    data = chart.getData();
                    if(data != null){
                        ILineDataSet breath = data.getDataSetByIndex(1);
                        stop_array.add(breath.getEntryCount() - 1);
                    }
                }
            }
            pre_item = item;
        }

        for(int i = 0; i < stop_xval.size(); i++){
            if(i % 2 == 1){
                if(i+1 == stop_xval.size()){
                    addll_chart(stop_xval.get(i),div_time,1);
                }
            }
            else if(i % 2 == 0){
                if(i == 0){
                    addll_chart(0,stop_xval.get(i),1);
                }
                else{
                    addll_chart(stop_xval.get(i-1),stop_xval.get(i),1);
                }
            }
        }

        if(stop_xval.size() == 0){
            addll_chart(0,div_time,1);
        }

        double avg_depth = depth_sum / depth_count;
        String avg_depth_s = String.format("%.1f", avg_depth);
        ave_depth.setText(avg_depth_s+"mm");

        int breathScore = (int) (((double) brath / (double) breath_) * 100);
        if(Integer.parseInt(reportItems.get(position).getReport_position_num()) == 0){
            chart_break.setVisibility(View.VISIBLE);
            //breath_break.setVisibility(View.VISIBLE);
            //breath_accuracy.setVisibility(View.GONE);
            ventil_score.setVisibility(View.GONE);
            ventilationCardView.setVisibility(View.INVISIBLE);
            chart_break.invalidate();
            //breath_accuracy.invalidate();
            ventil_score.invalidate();
            //breath_break.invalidate();
        }
        else{
            ventil_score.setText(breathScore + " %");
            //breath_accuracy.setText(breathScore + " %");
            lung_clip.setLevel(breathScore * 90);
            chart_break.setVisibility(View.GONE);
            chart_break.setVisibility(View.GONE);
            //breath_break.setVisibility(View.GONE);
            //breath_accuracy.setVisibility(View.VISIBLE);
            ventil_score.setVisibility(View.VISIBLE);
            chart_break.invalidate();
            //breath_accuracy.invalidate();
            ventil_score.invalidate();
            //breath_break.invalidate();
            isVentil = true;
        }

        if (breath_ != 0) {
            int all_score = (int) ((breathScore * 0.2) + (depth_accuracy_ * 0.8));
            all_accuracy.setText(all_score + "%");
            total_score = all_score;
        } else {
            if(Integer.parseInt(reportItems.get(position).getReport_up_depth()) != 0) {
                total_score = depth_accuracy_;
                all_accuracy.setText(total_score + "%");
            } else {
                all_accuracy.setText(reportItems.get(position).getReport_down_depth() + "%");
                total_score = Integer.parseInt(reportItems.get(position).getReport_down_depth());
            }
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(depth*2);
        xAxis.setAxisMinimum(0f);
        xAxis.removeAllLimitLines();
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisMaximum((float)div_time);
        xAxis.setAxisMinimum(0.0f);
        //     xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setGridColor(Color.TRANSPARENT);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setShowSpecificLabelPositions(true);

        float[] label_position = new float[(int)scale + 1];
        for(int i=0; i<scale + 1; i++)
            label_position[i] = i * 15.0f;

        xAxis.setSpecificLabelPositions(label_position);

        chart.getXAxis().setTextColor(Color.WHITE);

        for(int i=0; i<stop_xval.size(); i++) {
            LimitLine ll = null;
            if(i%2 == 0)
                ll = new LimitLine(stop_xval.get(i), " ");
            else{
                try{
                    ll = new LimitLine(stop_xval.get(i), (String.format("%.1f" , stop_list.get(i/2)) + " Secs  "));
                }catch(IndexOutOfBoundsException e){
                    ll = new LimitLine(stop_xval.get(i), "2.0 Secs  ");
                }
            }
            ll.setLineWidth(1f);
            ll.enableDashedLine(15f, 2f, 0f);
            ll.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            ll.setLineColor(Color.RED);
            ll.setTextSize(10f);
            ll.setTextColor(Color.WHITE);
            xAxis.addLimitLine(ll);
        }

        for(int i = 0; i<ble_list.size(); i++){
            LimitLine ll= null;
            if(i%2 == 0)
                ll = new LimitLine(ble_list.get(i), "Bluetooth OFF");
            else{
                try{
                    ll = new LimitLine(ble_list.get(i), "");
                }catch(IndexOutOfBoundsException e){
                    ll = new LimitLine(ble_list.get(i), "");
                }
            }
            ll.setLineWidth(1f);
            ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll.setLineColor(Color.BLUE);
            ll.setTextSize(10f);
            ll.setTextColor(Color.WHITE);
            xAxis.addLimitLine(ll);
        }

        chart.setNoDataText(" ");
        chart.moveViewToX(-0.5f);
        chart.invalidate();
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setColor(Color.rgb(255, 255, 255));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawHighlightIndicators(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet1() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setColor(Color.rgb(255, 255, 255));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawHighlightIndicators(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet2(){
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setColor(Color.rgb(255, 0, 0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet3(){
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255,210,0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet4(){
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255,100,0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet5(){
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(72,214,214));
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet6(){
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(73,198,68));
        set.setLineWidth(2.5f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    boolean isStop = false;

    private void addEntry_chart(float val, float xval, int label) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet depth = data.getDataSetByIndex(0);
            ILineDataSet breath = data.getDataSetByIndex(1);
            if (depth == null) {
                depth = createSet();
                breath = createSet1();
                data.addDataSet(depth);
                data.addDataSet(breath);
            }

            if(label == 0)
                data.addEntry(new Entry(xval, val, "Label1"), 0);

            if(label == 1 || label == 2 )
                data.addEntry(new Entry(xval, val), 1);

            if( val > 0f && (val > Integer.parseInt(max) || val < Integer.parseInt(min))){
                if(val > prev_val && label == 0 ) {
                    ILineDataSet set = createSet2();
                    data.addDataSet(set);
                    set.addEntry(new Entry(prev_xval, prev_val));
                    set.addEntry(new Entry(xval, val));
                }
            }

            if( val > 0f && val < Integer.parseInt(min)){
                if(prev_val > val && label == 0 ) {
                    ILineDataSet set = createSet3();
                    data.addDataSet(set);
                    set.addEntry(new Entry(prev_xval, prev_val));
                    set.addEntry(new Entry(xval, val));
                }
            }

            if (isover){
                Log.d("overdrawing", String.valueOf(prev_bxval));
                Log.d("overdrawing", String.valueOf(xval));
                ILineDataSet set = createSet4();
                data.addDataSet(set);
                set.addEntry(new Entry(prev_bxval, prev_bval));
                set.addEntry(new Entry(xval, val));
                isover = false;
            }

            if ( label == 2){
                Log.d("overdrawing", String.valueOf(prev_bxval));
                Log.d("overdrawing", String.valueOf(xval));
                ILineDataSet set = createSet4();
                data.addDataSet(set);
                set.addEntry(new Entry(prev_bxval, prev_bval));
                set.addEntry(new Entry(xval, val));
                isover = true;
            }

            if(label == 3){
                ILineDataSet set = createSet5();
                data.addDataSet(set);
                set.addEntry(new Entry(xval, val));
            }

            if(label == 0) {
                prev_xval = xval;
                prev_val = val;
            }

            else if(label == 1 || label == 2){
                prev_bxval = xval;
                prev_bval = val;
            }

            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.moveViewToX(data.getEntryCount());
        }
    }

    final float breath_limit = 10.0f;
    final float breath_threshold= 50.0f;

    private void addll_chart( float pre_xval, float xval, int label) {
        LineData data = chart.getData();
        if (data != null) {
            if(label == 0){
                ILineDataSet set = createSet5();
                ILineDataSet mset = createSet5();
                data.addDataSet(set);
                data.addDataSet(mset);
                set.addEntry(new Entry(pre_xval, breath_limit));
                set.addEntry(new Entry(xval, breath_limit));
                mset.addEntry(new Entry(pre_xval, breath_threshold));
                mset.addEntry(new Entry(xval, breath_threshold));
            }
            if(label == 1){
                ILineDataSet set = createSet6();
                ILineDataSet mset = createSet6();
                data.addDataSet(set);
                data.addDataSet(mset);
                set.addEntry(new Entry(pre_xval, Float.parseFloat(min)));
                set.addEntry(new Entry(xval, Float.parseFloat(min)));
                mset.addEntry(new Entry(pre_xval, Float.parseFloat(max)));
                mset.addEntry(new Entry(xval, Float.parseFloat(max)));
            }
            //data.addEntry(new Entry(depth_red.getEntryCount(), val), 2);
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private void resetChart() {

        chart.clear();
        chart.invalidate();

//        chart02.clear();
//        chart02.invalidate();


    }


    public void onBackPressed() {
        this.backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {
        private Activity activity;
        private long backKeyPressedTime;
        private Toast toast;

        public BackPressCloseHandler(Activity context) {
            this.backKeyPressedTime = 0;
            this.activity = context;
        }

        public void onBackPressed() {

            showGuide(ReportActivity.this);

        }

        private void showGuide(final Activity activity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View view = inflater.inflate(R.layout.done_layout, null);
            builder.setView(view);

            final Button appbtnCancel = view.findViewById(R.id.appbtnCancel);
            final Button appbtnExit = view.findViewById(R.id.appbtnExit);

            final AlertDialog dialog = builder.create();

            appbtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            appbtnExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent main = new Intent(ReportActivity.this, RoomActivity.class);
                    startActivity(main);
                    finish();
                    overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                }
            });

            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
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

        HashMap<String, String> keyitem = new HashMap<>();

        final HashMap<String, ArrayList<String>> reportList = new HashMap<>();
        final ArrayList<String> date = new ArrayList<>();

        for (Report report : reports){
            String split[] = report.to_day.split("/");
            keyitem.put(split[0], "");
        }

        for (String key : keyitem.keySet()) {date.add(key);}

        Collections.sort(date);

        for (String date_ : date) {
            ArrayList<String> item = new ArrayList<>();
            for (Report report : reports) {
                String split[] = report.to_day.split("/");
                //if (date_.equals(report.to_day)) {
                if(date_.equals(split[0])){
                    int all_score;
                    int breathScore = (int) ((Double.parseDouble(report.lung_correct) / Double.parseDouble(report.lung_num)) * 100);
                    int sum_depth = Integer.parseInt(report.report_depth_correct)
                            + Integer.parseInt(report.report_up_depth)
                            + Integer.parseInt(report.report_down_depth);

                    int depth_accuracy_ = (int) ((double) sum_depth / (double) 3);
                    if(Integer.parseInt(report.lung_num) != 0){
                        all_score = (int) ((breathScore * 0.2) + (depth_accuracy_ * 0.8));
                    }else
                        all_score = depth_accuracy_;
                    item.add(report.report_name + "," + report.report_depth_correct+ ","+report.to_day +","+all_score);
                }
            }
            Collections.sort(item);
            reportList.put(date_, item);
        }

        ReportAdapter arrayAdapter = new ReportAdapter(ReportActivity.this, date, reportList);
        report_list_data.setAdapter(arrayAdapter);

        report_list_data.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            //String day = date.get(groupPosition);
            String item = reportList.get(date.get(groupPosition)).get(childPosition);
            String[] split = item.split(",");
            String day = split[2];
            Intent main = new Intent(ReportActivity.this, ReportActivity.class);
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
            Intent main = new Intent(ReportActivity.this, ReportActivity.class);
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
}
