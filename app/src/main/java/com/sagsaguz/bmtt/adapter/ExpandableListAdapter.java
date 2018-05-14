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
import android.widget.TextView;
import android.widget.Toast;

import com.sagsaguz.bmtt.QAActivity;
import com.sagsaguz.bmtt.R;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private static Context _context;
    private String userType;
    private List<String> _listDataHeader; // header titles
    private HashMap<String, String> _listSubHeader;
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;

    public ExpandableListAdapter(Context context,
                                 String userType,
                                 List<String> listDataHeader,
                                 HashMap<String, String> listSubHeader,
                                 HashMap<String, List<String>> listChildData) {
        this._context = context;
        this.userType = userType;
        this._listDataHeader = listDataHeader;
        this._listSubHeader = listSubHeader;
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
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        final EditText etListChild = convertView.findViewById(R.id.lblListItem);

        etListChild.setText(childText);
        etListChild.getBackground().setColorFilter(_context.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);

        final ImageView ivEdit = convertView.findViewById(R.id.ivEdit);
        final ImageView ivSave = convertView.findViewById(R.id.ivSave);

        if(userType.equals("user")){
            ivEdit.setVisibility(View.GONE);
            ivSave.setVisibility(View.GONE);
        }

        final InputMethodManager imm = (InputMethodManager) _context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivEdit.setVisibility(View.GONE);
                etListChild.setCursorVisible(true);
                etListChild.setFocusable(true);
                etListChild.setFocusableInTouchMode(true);
                etListChild.setClickable(true);
                etListChild.setSelection(etListChild.getText().length());
                ivSave.setVisibility(View.VISIBLE);
                if (imm != null) {
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivSave.setVisibility(View.GONE);
                etListChild.setCursorVisible(false);
                etListChild.setFocusable(false);
                etListChild.setFocusableInTouchMode(false);
                etListChild.setClickable(false);
                ivEdit.setVisibility(View.VISIBLE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                if(TextUtils.isEmpty(etListChild.getText().toString())){
                    QAActivity.updateAnswer(_listDataHeader.get(groupPosition), "null");
                } else {
                    QAActivity.updateAnswer(_listDataHeader.get(groupPosition), etListChild.getText().toString());
                }
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
        if (_listDataHeader.size() != 0)
            return this._listDataHeader.get(groupPosition);
        return "null";
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
        String headerTitle = (String) getGroup(groupPosition);
        String subHeader = getSubHeader(groupPosition);
        if (convertView == null) {
            LayoutInflater inflateInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflateInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        TextView lblSubHeader = convertView.findViewById(R.id.lblSubHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        lblSubHeader.setText(subHeader);

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

    private String getSubHeader(int groupPosition){
        if (_listDataHeader.size() != 0 && _listSubHeader.size() != 0)
            return this._listSubHeader.get(this._listDataHeader.get(groupPosition));
        return "null";
    }

}
