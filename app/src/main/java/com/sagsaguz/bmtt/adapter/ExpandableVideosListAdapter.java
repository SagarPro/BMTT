package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sagsaguz.bmtt.QAActivity;
import com.sagsaguz.bmtt.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpandableVideosListAdapter extends BaseExpandableListAdapter {

    private static Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private Map<String, List<String>> _listDataChild;

    public ExpandableVideosListAdapter(Context context,
                                 List<String> listDataHeader,
                                 Map<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.fragment_list_item, null);
        }

        TextView tvChild = convertView.findViewById(R.id.videoName);

        //String subKey = childText.substring(childText.indexOf("_")+1, childText.indexOf("."));
        String key = childText.substring(childText.indexOf("_", childText.indexOf("_") + 1), childText.indexOf("."));
        String subKey = key.substring(1);
        subKey = subKey.replace("_", " ");
        tvChild.setText(subKey);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String subKey = (String) getGroup(groupPosition);
        String headerTitle = subKey.substring(subKey.indexOf("_")+1);
        if (convertView == null) {
            LayoutInflater inflateInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflateInflater.inflate(R.layout.fragment_list_group, null);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageView ivListHeader = convertView.findViewById(R.id.ivListHeader);

        switch (headerTitle){
            case "Introduction":
                ivListHeader.setBackgroundResource(R.drawable.introduction);
                //lblListHeader.setBackgroundResource(R.drawable.introduction);
                break;
            case "Montessori Orientation":
                ivListHeader.setBackgroundResource(R.drawable.montessori_orientation);
                //lblListHeader.setBackgroundResource(R.drawable.montessori_orientation);
                break;
            case "Introduction to Exercise of Practical Life":
                ivListHeader.setBackgroundResource(R.drawable.introduction_to_epl);
                //lblListHeader.setBackgroundResource(R.drawable.introduction_to_epl);
                break;
            case "Exercise of Practical Life Presentations":
                ivListHeader.setBackgroundResource(R.drawable.class_management);
                //lblListHeader.setBackgroundResource(R.drawable.class_management);
                break;
            case "Sensorial Introduction and Presentations":
                ivListHeader.setBackgroundResource(R.drawable.sensorial_introductions_and_presentations);
                //lblListHeader.setBackgroundResource(R.drawable.sensorial_introductions_and_presentations);
                break;
            case "Class Management":
                ivListHeader.setBackgroundResource(R.drawable.epl_presentations);
                //lblListHeader.setBackgroundResource(R.drawable.epl_presentations);
                break;
            case "Arithmetic":
                ivListHeader.setBackgroundResource(R.drawable.arithmetic);
                //lblListHeader.setBackgroundResource(R.drawable.arithmetic);
                break;
            case "Language":
                ivListHeader.setBackgroundResource(R.drawable.language);
                //lblListHeader.setBackgroundResource(R.drawable.language);
                break;
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getGroupTypeCount() {
        return super.getGroupTypeCount();
    }

    @Override
    public int getGroupType(int groupPosition) {
        return super.getGroupType(groupPosition);
    }

    @Override
    public int getChildTypeCount() {
        return super.getChildTypeCount();
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return super.getChildType(groupPosition, childPosition);
    }
}
