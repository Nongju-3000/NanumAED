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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import java.util.List;

public class ReportActivity extends Activity {

    private BackPressCloseHandler backPressCloseHandler;

    private LineChart chart;
    private TextView chart02;
    private RecyclerView recyclerView;

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
    private TextView report_total;

    private TextView equip;

    private ArrayList<Float> chart_item;
    private ArrayList<ReportItem> reportItems;
    private ArrayList<String> names;

    private TextView all_accuracy;
    private TextView breath_accuracy;
    private TextView depth_accuracy;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    private int depth_accuracy_ = 0;
    private String depth_score;
    private int div_time;
    private String min;
    private String max;

    //private TextView showCount;

    private TextView breath_break, chart_break, depth_break;
    private float prev_val = 0f;

    private TextView totalCount, correctCount, wrongCount;

    private ImageView breath_accuracy_img, depth_accuracy_img, report_end_time_img, report_interval_img, cycle_img, depth_correct_img;
    private BluetoothLeServiceCPR bluetoothLeServiceCPR;
    ArrayList<Integer> stop_list = new ArrayList<>();

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size); // 1600, 2458

        Intent gattServiceIntent = new Intent(this, BluetoothLeServiceCPR.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        if(size.y >= 2400){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        backPressCloseHandler = new BackPressCloseHandler(this);

        chart = findViewById(R.id.chart_img);
        chart02 = findViewById(R.id.chart_img_02);
        recyclerView = findViewById(R.id.report_list);

        report_end_time = (TextView) findViewById(R.id.report_end_time);
        report_interval_sec = (TextView) findViewById(R.id.report_interval_sec);
        report_cycle = (TextView) findViewById(R.id.report_cycle);
        report_depth_correct = (TextView) findViewById(R.id.report_depth_correct);

        report_up_depth = (Button) findViewById(R.id.report_up_depth);
        report_down_depth = (Button) findViewById(R.id.report_down_depth);
        report_bpm = (TextView) findViewById(R.id.report_bpm);
        report_angle = (TextView) findViewById(R.id.report_angle);
        report_total = (TextView) findViewById(R.id.report_total);

        all_accuracy = findViewById(R.id.all_accuracy);
        breath_accuracy = findViewById(R.id.breath_accuracy);
        depth_accuracy = findViewById(R.id.depth_accuracy);

        equip = findViewById(R.id.equip);

        totalCount = findViewById(R.id.totalCount);
        correctCount = findViewById(R.id.correctCount);
        wrongCount = findViewById(R.id.wrongCount);

        breath_break = findViewById(R.id.breath_break);
        breath_break.setOnClickListener(v -> Toast.makeText(getApplication(), "AIO를 사용하셔야 활성화됩니다.", Toast.LENGTH_SHORT).show());
        chart_break = findViewById(R.id.chart_break);
        chart_break.setOnClickListener(v -> Toast.makeText(getApplication(), "AIO를 사용하셔야 활성화됩니다.", Toast.LENGTH_SHORT).show());
        depth_break = findViewById(R.id.depth_break);
        depth_break.setOnClickListener(v -> Toast.makeText(getApplication(), "AIO를 사용하셔야 활성화됩니다.", Toast.LENGTH_SHORT).show());

        breath_accuracy_img = findViewById(R.id.breath_accuracy_img);
        breath_accuracy_img.setOnClickListener(v -> {
            int[] location = new int[2];
            breath_accuracy_img.getLocationOnScreen(location);
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView info_text = dialog.findViewById(R.id.info_text);
            info_text.setText("인공호흡 정확도입니다.");
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = location[0] + 150;
            wmlp.y = location[1] - 50;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
        depth_accuracy_img = findViewById(R.id.depth_accuracy_img);
        depth_accuracy_img.setOnClickListener(v -> {
            int[] location = new int[2];
            depth_accuracy_img.getLocationOnScreen(location);
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView info_text = dialog.findViewById(R.id.info_text);
            info_text.setText("압박 정확도입니다.");
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = location[0] + 150;
            wmlp.y = location[1] - 50;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
        report_end_time_img = findViewById(R.id.report_end_time_img);
        report_end_time_img.setOnClickListener(v -> {
            int[] location = new int[2];
            report_end_time_img.getLocationOnScreen(location);
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView info_text = dialog.findViewById(R.id.info_text);
            info_text.setText("CPR 총 소요 시간입니다.");
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = location[0] + 150;
            wmlp.y = location[1] - 50;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
        report_interval_img = findViewById(R.id.report_interval_img);
        report_interval_img.setOnClickListener(v -> {
            int[] location = new int[2];
            report_interval_img.getLocationOnScreen(location);
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView info_text = dialog.findViewById(R.id.info_text);
            info_text.setText("압박 중단 시간입니다.");
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = location[0] + 150;
            wmlp.y = location[1] - 50;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
        cycle_img = findViewById(R.id.cycle_img);
        cycle_img.setOnClickListener(v -> {
            int[] location = new int[2];
            cycle_img.getLocationOnScreen(location);
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView info_text = dialog.findViewById(R.id.info_text);
            info_text.setText("Cycle 횟수 입니다.");
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = location[0] + 150;
            wmlp.y = location[1] - 50;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
        depth_correct_img = findViewById(R.id.depth_correct_img);
        depth_correct_img.setOnClickListener(v -> {
            int[] location = new int[2];
            depth_correct_img.getLocationOnScreen(location);
            Dialog dialog = new Dialog(ReportActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView info_text = dialog.findViewById(R.id.info_text);
            info_text.setText("위치 정확도 입니다.");
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = location[0] + 150;
            wmlp.y = location[1] - 50;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });

        Intent intent = getIntent();

        Boolean ReportDB = intent.getBooleanExtra("ReportDB", false);
        String ReportItem = intent.getStringExtra("ReportItem");
        String ReportDay = intent.getStringExtra("ReportDay");

        String UserName = intent.getStringExtra("UserName");
        String room = intent.getStringExtra("room");

        names = new ArrayList<>();

        reportItems = (ArrayList<ReportItem>) intent.getSerializableExtra("reportItems");

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
                                    , report.min
                                    , report.max
                                    , report.depth_num
                                    , report.depth_correct
                                    , report.position_num
                                    , report.position_correct
                                    , report.lung_num
                                    , report.lung_correct
                                    , (ArrayList<Integer>) converters.gettingIntegerListFromString(report.stop_time_list)
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
                            , report.min
                            , report.max
                            , report.depth_num
                            , report.depth_correct
                            , report.position_num
                            , report.position_correct
                            , report.lung_num
                            , report.lung_correct
                            , (ArrayList<Integer>) converters.gettingIntegerListFromString(report.stop_time_list)
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
            }
            report_down_depth.setText(reportItems.get(0).getReport_down_depth() + " %");
            report_bpm.setText(reportItems.get(0).getReport_bpm() + " BPM");
            int angle = 90 - Integer.parseInt(reportItems.get(0).getReport_angle());
            report_angle.setText(angle + " °");

            if(Integer.parseInt(reportItems.get(0).getReport_angle()) == 0 && (Integer.parseInt(reportItems.get(0).getReport_position_num()) == 0 && Integer.parseInt(reportItems.get(0).getReport_lung_num()) == 0)){
                equip.setText("");
            }else if(Integer.parseInt(reportItems.get(0).getReport_angle()) != 0 && Integer.parseInt(reportItems.get(0).getReport_position_num()) == 0 && Integer.parseInt(reportItems.get(0).getReport_lung_num()) == 0){
                equip.setText("BAND");
            }else if(Integer.parseInt(reportItems.get(0).getReport_angle()) == 0 && (Integer.parseInt(reportItems.get(0).getReport_position_num()) != 0 || Integer.parseInt(reportItems.get(0).getReport_lung_num()) != 0)){
                equip.setText("AIO");
                report_angle.setText("BLOCK");
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

            div_time = Integer.parseInt(reportItems.get(0).getReport_end_time());

            //showCount.setText(reportItems.get(0).getDepth_correct()+"/"+reportItems.get(0).getDepth_num());

            int depth_num = Integer.parseInt(reportItems.get(0).getDepth_num());
            int depth_correct = Integer.parseInt(reportItems.get(0).getDepth_correct());
            int depth_wrong = depth_num - depth_correct;

            totalCount.setText(reportItems.get(0).getDepth_num());
            correctCount.setText(reportItems.get(0).getDepth_correct());
            wrongCount.setText(String.valueOf(depth_wrong));

            min = reportItems.get(0).getReport_Min();
            max = reportItems.get(0).getReport_Max();

            stop_list = reportItems.get(0).getStop_time_list();

            for (ReportItem reportItem : reportItems)
                names.add("   " + reportItem.getReport_name() + "   ");

            setChart(0);

            Converters converters = new Converters();

            if(!ReportDB) {
                long now_ = System.currentTimeMillis();
                Date date_ = new Date(now_);
                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                String getTime_ = sdf_.format(date_);

                ChatData chatData_ = new ChatData("report/" + reportItems.get(0).getReport_end_time() + "/"
                        + reportItems.get(0).getReport_interval_sec() + "/"
                        + reportItems.get(0).getReport_cycle() + "/"
                        + reportItems.get(0).getReport_depth_correct() + "/"
                        + reportItems.get(0).getReport_up_depth() + "/"
                        + reportItems.get(0).getReport_down_depth() + "/"
                        + reportItems.get(0).getReport_bpm() + "/"
                        + reportItems.get(0).getReport_angle() + "/"
                        + converters.writingStringFromList(reportItems.get(0).getReport_depth_list()) + "/"
                        + reportItems.get(0).getDepth_num() + "/"
                        + reportItems.get(0).getDepth_correct() + "/"
                        + reportItems.get(0).getReport_position_num() + "/"
                        + reportItems.get(0).getReport_position_correct() + "/"
                        + reportItems.get(0).getReport_lung_num() + "/"
                        + reportItems.get(0).getReport_lung_correct() + "/"
                        + reportItems.get(0).getStop_time_list()
                        , getTime_
                        , UserName);

                databaseReference.child("Room").child(room).child("message").push().setValue(chatData_);
            }

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
                report.stop_time_list = converters.writingIntegerStringFromList(reportItem.getStop_time_list());

                database.reportDao().insert(report);
            }
        }
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
        chart.getXAxis().setDrawGridLines(false);
        BarLineChartTouchListener barLineChartTouchListener = (BarLineChartTouchListener) chart.getOnTouchListener();
        barLineChartTouchListener.stopDeceleration();
        float scale = div_time / 30;
        chart.zoomToCenter(scale, 0f);

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
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
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
        int index = 0;
        int chart_size = chart_item.size();

        for (float item : chart_item) {
            if (item >= Integer.parseInt(min) && item <= Integer.parseInt(max)) {
                addEntry_chart(0, 150);
                addEntry_chart(item, 150);
                depth++;
            } else if ((5 < item && item < Integer.parseInt(min))) {
                addEntry_chart(0, 150);
                addEntry_chart(item, 150);
                depth++;
            } else if ((Integer.parseInt(max) < item && item <= 100)) {
                addEntry_chart(0, 150);
                addEntry_chart(70, 150);
                depth++;
            } else if (100 < item) {
                addEntry_chart(10, 150);
                addEntry_chart((item - 100), 150);
                depth++;
            } else if (0 == item) {
                addEntry_chart(0, 200);
                addEntry_chart(0, 200);
                depth++;
            } else if (item < 5) {
                switch ((int) item) {
                    case 1:
                        breath_++;
                        depth++;
                        addEntry_chart(0, 200);
                        addEntry_chart(0, 60);
                        break;
                    case 2:
                        brath++;
                        breath_++;
                        depth++;
                        addEntry_chart(0, 200);
                        addEntry_chart(0, 40);
                        break;
                    case 3:
                        brath++;
                        breath_++;
                        depth++;
                        addEntry_chart(0, 200);
                        addEntry_chart(0, 30);
                        break;
                    case 4:
                        breath_++;
                        depth++;
                        addEntry_chart(0, 200);
                        addEntry_chart(0, 10);
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
        }

        report_total.setText(String.valueOf(depth));

        int breathScore = (int) (((double) brath / (double) breath_) * 100);
        if(Integer.parseInt(reportItems.get(position).getReport_position_num()) == 0){
            chart_break.setVisibility(View.VISIBLE);
            breath_break.setVisibility(View.VISIBLE);
            breath_accuracy.setVisibility(View.GONE);
            chart02.setVisibility(View.GONE);
            chart_break.invalidate();
            breath_accuracy.invalidate();
            chart02.invalidate();
            breath_break.invalidate();
        }
        else{
            chart02.setText(breathScore + " %");
            breath_accuracy.setText(breathScore + " %");

            chart_break.setVisibility(View.GONE);
            breath_break.setVisibility(View.GONE);
            breath_accuracy.setVisibility(View.VISIBLE);
            chart02.setVisibility(View.VISIBLE);
            chart_break.invalidate();
            breath_accuracy.invalidate();
            chart02.invalidate();
            breath_break.invalidate();
        }

        if (breath_ != 0) {
            int all_score = (int) ((breathScore * 0.2) + (depth_accuracy_ * 0.8));
            all_accuracy.setText(all_score + "%");
        } else { 
            all_accuracy.setText(reportItems.get(position).getReport_down_depth() + " %");
        }

        /*ArrayList<String> xValues = new ArrayList<String>();
        for(int i=0; i<100; i+=10){
            xValues.add("" +i);
        }*/
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(depth*2);
        xAxis.setAxisMinimum(0f);
        xAxis.removeAllLimitLines();
        xAxis.setDrawAxisLine(true);
        //     xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setGridColor(Color.TRANSPARENT);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setShowSpecificLabelPositions(true);

        float[] label_position = new float[(int)scale + 1];
        for(int i=0; i<=scale; i++)
            label_position[i] = ((depth/scale)*i)*2;

        xAxis.setSpecificLabelPositions(label_position);

        xAxis.setValueFormatter(new LabelFormatter(depth, (int)scale));
        chart.getXAxis().setTextColor(Color.WHITE);

        for(int i=0; i<stop_array.size(); i++) {
            LimitLine ll = null;
            if(i%2 == 0)
                ll = new LimitLine(stop_array.get(i), " ");
            else{
                try{
                    ll = new LimitLine(stop_array.get(i), (stop_list.get(i/2)+" Secs  "));
                }catch(IndexOutOfBoundsException e){
                    ll = new LimitLine(stop_array.get(i), "2 Secs  ");
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

        set.setColor(Color.rgb(255, 204, 0));
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

    ArrayList<Integer> stop_array = new ArrayList<>();
    boolean isStop = false;

    private void addEntry_chart(float val, float val1) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet depth = data.getDataSetByIndex(0);
            ILineDataSet breath = data.getDataSetByIndex(1);
        //    ILineDataSet depth_red = data.getDataSetByIndex(2);
            if (depth == null) {
                depth = createSet();
                breath = createSet1();
            //    depth_red = createSet2();
                data.addDataSet(depth);
                data.addDataSet(breath);
           //     data.addDataSet(depth_red);
            }
            data.addEntry(new Entry(depth.getEntryCount(), val, "Label1"), 0);
            data.addEntry(new Entry(breath.getEntryCount(), val1), 1);

            if(val1 == 200) {
                if(!isStop) {
                    stop_array.add(breath.getEntryCount() - 1);
                    isStop = true;
                }
            }else if(val1 != 200 && val1 != 60 && val1 != 40 && val1 != 30 && val1 != 10){
                if(isStop) {
                    stop_array.add(breath.getEntryCount() - 1);
                    isStop = false;
                }
            }

            if(val > 0f && val < Integer.parseInt(min) -1 || val > Integer.parseInt(max) + 1){
                ILineDataSet set = createSet2();
                data.addDataSet(set);
                set.addEntry(new Entry(depth.getEntryCount()-2, prev_val));
                set.addEntry(new Entry(depth.getEntryCount()-1, val));
            }
            prev_val = val;

            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            if (true) {
                chart.moveViewToX(data.getEntryCount());
            }
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
}
