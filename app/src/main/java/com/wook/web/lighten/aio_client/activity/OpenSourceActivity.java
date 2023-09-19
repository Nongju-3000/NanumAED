package com.wook.web.lighten.aio_client.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wook.web.lighten.aio_client.R;

import java.util.ArrayList;
import java.util.List;

public class OpenSourceActivity extends Activity {
    private BackPressCloseHandler backPressCloseHandler;
    private ListView listView;
    private SDKAdaptor sdkAdaptor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_opensource);

        backPressCloseHandler = new BackPressCloseHandler(this);

        listView = (ListView) findViewById(R.id.sourceList);
        sdkAdaptor = new SDKAdaptor();
        listView.setAdapter(sdkAdaptor);
    }

    public void onBackPressed() {
        this.backPressCloseHandler.onBackPressed();
    }

    class SDKAdaptor extends BaseAdapter {
        private List<SDK> sdks;
        private LayoutInflater layoutInflater;

        public SDKAdaptor() {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            sdks = new ArrayList<SDK>();
            sdks.add(new SDK("google-gson", "https://github.com/google/gson", "Copyright 2008 Google Inc.\nLicensed under the Apache License, Version 2.0 (the \"License\")"));
            sdks.add(new SDK("Neumorphism", "https://github.com/fornewid/neumorphism/blob/master/LICENSE", "Copyright 2004 fornewid\nLicensed under the Apache License, Version 2.0 (the \"License\")"));
            sdks.add(new SDK("MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart/blob/master/LICENSE", "Copyright 2020 Philipp Jahoda\nLicensed under the Apache License, Version 2.0 (the \"License\")"));
            sdks.add(new SDK("Jitsi-meet", "https://github.com/jitsi/jitsi-meet/blob/master/LICENSE", "Copyright 2004 \nLicensed under the Apache License, Version 2.0 (the \"License\")"));
            sdks.add(new SDK("Firebase", "https://github.com/firebase/firebase-android-sdk/blob/master/LICENSE", "Copyright 2004 \nLicensed under the Apache License, Version 2.0 (the \"License\")"));
            sdks.add(new SDK("Youtube-player", "https://github.com/PierfrancescoSoffritti/android-youtube-player/blob/master/LICENSE", "Copyright (c) 2018 Pierfrancesco Soffritti \nLicensed under the MIT License (the \"License\")"));
            sdks.add(new SDK("MPAndroidChart", "https://github.com/philippeauriach/MPAndroidChart/tree/custom-labels", "Copyright 2016 Philipp Jahoda \n Licensed under the Apache License, Version 2.0 (the \"License\")"));
        }

        @Override
        public int getCount() {
            return sdks == null ? 0 : sdks.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.item_source, parent, false);
            TextView titleLabel = (TextView) view.findViewById(R.id.tlt);
            TextView linkLabel = (TextView) view.findViewById(R.id.link);
            TextView explainLabel = (TextView) view.findViewById(R.id.explain);
            Space space = (Space) view.findViewById(R.id.space3);
            SDK sdk = sdks.get(position);

            titleLabel.setText(sdk.title);
            linkLabel.setText(sdk.link);
            explainLabel.setText(sdk.explain);
            space.setVisibility(getCount() -1 == position ? View.VISIBLE : View.GONE);
            return view;
        }
    }

    class SDK {
        private String title, link, explain;
        public SDK(String title, String link, String explain) {
            this.title = title;
            this.link = link;
            this.explain = explain;
        }
    }
    public class BackPressCloseHandler {
        public BackPressCloseHandler(Activity context) {
        }
        public void onBackPressed() {
            Intent main = new Intent(OpenSourceActivity.this, RoomActivity.class);
            startActivity(main);
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
            finish();
        }
    }
}
