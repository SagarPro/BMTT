package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sagsaguz.bmtt.MilestoneActivity;
import com.sagsaguz.bmtt.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpandableMilestoneAdapter extends BaseExpandableListAdapter {

    private static Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private Map<String, List<String>> _listDataChild;
    private HashMap<String, Integer> videoCount;

    public ExpandableMilestoneAdapter(Context context,
                                      List<String> listDataHeader,
                                      Map<String, List<String>> listChildData,
                                      HashMap<String, Integer> videoCount) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.videoCount = videoCount;
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
            convertView = infalInflater.inflate(R.layout.milestone_list_item, null);
        }

        TextView tvChild = convertView.findViewById(R.id.tvVideoName);

        //String subKey = childText.substring(childText.indexOf("_")+1, childText.indexOf("."));
        String key = childText.substring(childText.indexOf("_", childText.indexOf("_") + 1), childText.indexOf("."));
        String subKey = key.substring(1);
        subKey = subKey.replace("_", " ");
        tvChild.setText(subKey);

        final TextView tvVideoCount = convertView.findViewById(R.id.tvVideoCount);

        if (videoCount.containsKey(subKey)) {
            String vCount = String.valueOf(videoCount.get(subKey));
            tvVideoCount.setText(vCount);
        } else {
            tvVideoCount.setText("0");
        }

        final String finalSubKey = subKey;
        tvVideoCount.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Dialog dialog = new Dialog(_context);
                dialog.setContentView(R.layout.update_counter_dialog);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                TextView tvMessage = dialog.findViewById(R.id.tvMessage);
                tvMessage.setText(finalSubKey);
                final EditText etCountValue = dialog.findViewById(R.id.etCountValue);
                etCountValue.setText(tvVideoCount.getText().toString());
                etCountValue.setSelection(etCountValue.getText().length());
                Button btnUpdate = dialog.findViewById(R.id.btnUpdate);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int count = Integer.valueOf(etCountValue.getText().toString());
                        if(TextUtils.isEmpty(etCountValue.getText().toString()) ||
                                count < 0 || count > 3){
                            Toast.makeText(_context, "Please enter value between 0 & 3", Toast.LENGTH_SHORT).show();
                        } else {
                            videoCount.put(finalSubKey, Integer.valueOf(etCountValue.getText().toString()));
                            notifyDataSetChanged();
                            if (_context instanceof MilestoneActivity) {
                                ((MilestoneActivity) _context).updateCountValues(finalSubKey, videoCount.get(finalSubKey));
                            }
                            dialog.dismiss();
                        }
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });

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
            convertView = inflateInflater.inflate(R.layout.milestone_list_group, null);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

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
