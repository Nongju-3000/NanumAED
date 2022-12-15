package com.wook.web.lighten.aio_client.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.wook.web.lighten.aio_client.R;
import com.wook.web.lighten.aio_client.activity.ReportItem;
import com.wook.web.lighten.aio_client.db.AppDatabase;
import com.wook.web.lighten.aio_client.db.Report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ReportAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<String> mParentList;
    private HashMap<String, ArrayList<String>> mChildHashMap;

    public ReportAdapter(Context context, ArrayList<String> parentList, HashMap<String, ArrayList<String>> childHashMap) {
        this.mContext = context;
        this.mParentList = parentList;
        this.mChildHashMap = childHashMap;
    }

    @Override
    public String getGroup(int groupPosition) {
        return mParentList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mParentList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater groupInfla = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = groupInfla.inflate(R.layout.group_row, parent, false);
        }

        TextView parentText = (TextView) convertView.findViewById(R.id.groupName);
        parentText.setText(getGroup(groupPosition));

        return convertView;
    }


    @Override
    public String getChild(int groupPosition, int childPosition) {

        return this.mChildHashMap.get(mParentList.get(groupPosition)).get(childPosition);

    }

    @Override
    public int getChildrenCount(int groupPosition) {

        return this.mChildHashMap.get(mParentList.get(groupPosition)).size();

    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater childInfla = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = childInfla.inflate(R.layout.child_row, parent, false);
        }

        TextView childText = (TextView) convertView.findViewById(R.id.childName);
        TextView chilScore = (TextView) convertView.findViewById(R.id.chilScore);

        String item[] = mChildHashMap.get(mParentList.get(groupPosition)).get(childPosition).split(",");

        childText.setText(item[0]);
        chilScore.setText(item[3]);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void permissionCheck() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
    }
}