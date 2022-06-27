package com.wook.web.lighten.aio_client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wook.web.lighten.aio_client.R;


import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import soup.neumorphism.NeumorphButton;

public class CPRScoreActivity extends AppCompatActivity {

    private NeumorphButton create_cpr_btn01;


    private BackPressCloseHandler backPressCloseHandler;

    private ArrayList<UserItem> cprItem_01;

    private int endTime = 0;
    private int score_01 = 0;
    private int cycle_01 = 0;

    private EditText Score_name_cpr01;


    private ImageView score_setting_cpr;

    private ListView score_list_cpr01;

    private TextView score_cpr_re_01;

    private ArrayAdapter adapter;
    private ArrayList<String> list;

    private ArrayList<ReportItem> reportItems;
    private int depth_correct, depth_num;

    String UserName;
    String room;

    private int minDepth, maxDepth;

    int position_num, position_correct, lung_num, lung_correct;

    public void onBackPressed() {

        this.backPressCloseHandler.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);    // 타이틀바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cpr_score);

        backPressCloseHandler = new BackPressCloseHandler(this);

        create_cpr_btn01 = (NeumorphButton) findViewById(R.id.create_cpr_btn01);

        Intent intent = getIntent();

        reportItems = new ArrayList<ReportItem>();

        cprItem_01 = (ArrayList<UserItem>) intent.getSerializableExtra("cprItem_01");

        endTime = intent.getIntExtra("endTime", 0);

        score_01 = intent.getIntExtra("score_01", 0);

        cycle_01 = intent.getIntExtra("cycle_01", 0);

        UserName = intent.getStringExtra("UserName");

        room = intent.getStringExtra("room");

        minDepth = intent.getIntExtra("minDepth", 0);
        maxDepth = intent.getIntExtra("maxDepth", 0);

        depth_correct = intent.getIntExtra("depth_correct", 0);
        depth_num = intent.getIntExtra("depth_num", 0);

        position_num = intent.getIntExtra("position_num", 0);
        position_correct = intent.getIntExtra("position_correct", 0);
        lung_num = intent.getIntExtra("lung_num", 0);
        lung_correct = intent.getIntExtra("lung_correct", 0);

        score_cpr_re_01 = (TextView) findViewById(R.id.score_cpr_re_01);

        Score_name_cpr01 = (EditText) findViewById(R.id.Score_name_cpr01);

        Score_name_cpr01.setText(UserName);

        if (score_01 != 0) {
            score_cpr_re_01.setText(String.valueOf(score_01));
        }


        list = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, list);

        ArrayList<String> list_ = getStringArrayPref(CPRScoreActivity.this, "ScoreCheckCPR");
        score_list_cpr01 = (ListView) findViewById(R.id.score_list_cpr01);


        if (!list_.isEmpty() && list_ != null) {
            for (String item : list_) {
                list.add(item);
            }
        }

        score_list_cpr01.setAdapter(adapter);

        score_setting_cpr = (ImageView) findViewById(R.id.score_setting_cpr);
        score_setting_cpr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScoreCheck(CPRScoreActivity.this);
            }
        });

        create_cpr_btn01.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (score_01 > 0)
                    reportItems.add(report_setting(cprItem_01, Score_name_cpr01.getText().toString(), String.valueOf(cycle_01), String.valueOf(score_01),
                            String.valueOf(minDepth), String.valueOf(maxDepth),
                            String.valueOf(depth_num), String.valueOf(depth_correct), String.valueOf(position_num), String.valueOf(position_correct),
                            String.valueOf(lung_num), String.valueOf(lung_correct)));

                Intent main = new Intent(CPRScoreActivity.this, ReportActivity.class);
                main.putExtra("reportItems", reportItems);
                main.putExtra("UserName", UserName);
                main.putExtra("room", room);
                startActivity(main);
                overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                finish();

            }
        });


    } //onCreate end

    private ReportItem report_setting(ArrayList<UserItem> Useritem, String name, String cycle, String score,
                                      String min, String max, String depth_num, String depth_correct,
                                      String position_num, String position_correct, String lung_num, String lung_correct) {

        ReportItem reportItem = null;

        ArrayList<Float> arrayList = new ArrayList<Float>();
        ArrayList<Integer> stopList = new ArrayList<>();

        int Depth_size = 0;
        int position_six = 0;
        int angleSum = 0;
        int hand_off = 0;
        int hand_off_ = 0;
        int position = 0;

        if (!Useritem.isEmpty() && Useritem != null)
            for (UserItem userItem : Useritem) {

                if (userItem.getAngle() != 0)
                    angleSum = angleSum + userItem.getAngle();

                if (userItem.getPosition() == 6)
                    position_six = position_six + 1;

                if (userItem.getPosition() == 1)
                    position = position + 1;

                if (userItem.getDepth_correct() != 0 || userItem.getDepth() != 0) {
                    Depth_size = Depth_size + 1;
                    if (userItem.getDepth_correct() != 0) {
                        if (userItem.getPosition() == 6)
                            arrayList.add((userItem.getDepth_correct() + 100f));
                        else
                            arrayList.add((float) userItem.getDepth_correct());
                    } else {
                        if (userItem.getPosition() == 6)
                            arrayList.add((userItem.getDepth() + 100f));
                        else
                            arrayList.add((float) userItem.getDepth());
                    }
                }

                if (userItem.getHand_off_start() != 0) {

                    arrayList.add((float)0);

                    if (hand_off_ < userItem.getHand_off_start()) {

                        hand_off_ = userItem.getHand_off_start();

                    } else {

                        if (2 == userItem.getHand_off_start()) {
           //                 hand_off = hand_off + userItem.getHand_off_start();
                        } else {
                            hand_off = hand_off + hand_off_;
                            stopList.add(hand_off_);
                            hand_off_ = 0;
                        }

                    }
                }

                if(userItem.getBreath() != 0){
                    arrayList.add((float)userItem.getBreath());
                }

            }
        if (hand_off_ > 0) {
            hand_off = hand_off + hand_off_;
            stopList.add(hand_off_);
        }

        int up_depth = (int) (100 - ((double) position_six / (double) Depth_size) * 100);
        int position_ = (int) (((double) position / (double) Depth_size) * 100);
        int bpm = (int) (((double) (Depth_size / (double) endTime) * 60));
        int angle = (int) (((double) angleSum / (double) Depth_size));

        reportItem = new ReportItem(name
                , String.valueOf(endTime)
                , String.valueOf(hand_off)
                , cycle
                , String.valueOf(position_)
                , String.valueOf(up_depth)
                , score
                , String.valueOf(bpm)
                , String.valueOf(angle)
                , arrayList
                , min
                , max
                , depth_num
                , depth_correct
                , position_num
                , position_correct
                , lung_num
                , lung_correct
                ,stopList);

        return reportItem;
    }

    private void showScoreCheck(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.score_check_layout, null);
        builder.setView(view);


        final EditText score_check = (EditText) view.findViewById(R.id.score_check);
        final Button score_check_add = (Button) view.findViewById(R.id.score_check_add);
        final Button score_check_done = (Button) view.findViewById(R.id.score_check_done);
        final ListView score_check_list = (ListView) view.findViewById(R.id.score_check_list);


        final AlertDialog dialog = builder.create();
        final ArrayAdapter adapter_ = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);


        score_check_list.setAdapter(adapter_);

        score_check_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStringArrayPref(CPRScoreActivity.this, "ScoreCheckCPR", list);
                dialog.dismiss();
            }
        });

        score_check_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.add(score_check.getText().toString());
                adapter.notifyDataSetChanged();
                adapter_.notifyDataSetChanged();
                score_check.setText("");
            }
        });

        score_check_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.remove(position);
                adapter.notifyDataSetChanged();
                adapter_.notifyDataSetChanged();
            }
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public class BackPressCloseHandler {
        private Activity activity;
        private long backKeyPressedTime;


        public BackPressCloseHandler(Activity context) {
            this.backKeyPressedTime = 0;
            this.activity = context;
        }

        public void onBackPressed() {
            final Dialog dialog = new Dialog(CPRScoreActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.back_dialog);
            Button yesBtn =dialog.findViewById(R.id.yesBtn);
            Button noBtn = dialog.findViewById(R.id.noBtn);
            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent main = new Intent(CPRScoreActivity.this, RoomActivity.class);
                    startActivity(main);
                    overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                    finish();
                }
            });
            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();

        }
    }

    public static void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            jsonArray.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, jsonArray.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }

    public static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

}
