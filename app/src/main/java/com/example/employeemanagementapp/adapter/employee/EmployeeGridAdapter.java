package com.example.employeemanagementapp.adapter.employee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.utils.Constants;

import java.util.HashMap;

public class EmployeeGridAdapter extends BaseAdapter {

    private Context mContext;
    private Cursor mCursor;
    private DatabaseHelper dbHelper;
    private HashMap<Long, String> departmentMap;

    public EmployeeGridAdapter(Context context, Cursor cursor, HashMap<Long, String> departmentMap) {
        mContext = context;
        mCursor = cursor;
        dbHelper = new DatabaseHelper(context);
        this.departmentMap = departmentMap;
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item_layout, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.text_name);
            holder.lastNameTextView = convertView.findViewById(R.id.text_lastname);
            holder.jobTextView = convertView.findViewById(R.id.text_job);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            if (mCursor != null && mCursor.moveToPosition(position)) {
                @SuppressLint("Range") String firstName = mCursor.getString(mCursor.getColumnIndex(Constants.COLUMN_FIRST_NAME));
                @SuppressLint("Range") String lastName = mCursor.getString(mCursor.getColumnIndex(Constants.COLUMN_LAST_NAME));
                @SuppressLint("Range") long departmentId = mCursor.getLong(mCursor.getColumnIndex(Constants.COLUMN_DEPARTMENT_ID));
                @SuppressLint("Range") String employeePosition = mCursor.getString(mCursor.getColumnIndex(Constants.COLUMN_POSITION)) != null ?
                        mCursor.getString(mCursor.getColumnIndex(Constants.COLUMN_POSITION)) : "";

                String departmentName = departmentMap.getOrDefault(departmentId, "Unknown");

                holder.nameTextView.setText(firstName != null ? firstName : "");
                holder.lastNameTextView.setText(lastName != null ? lastName : "");
                holder.jobTextView.setText(departmentName + " - " + employeePosition);
            } else {
                holder.nameTextView.setText("");
                holder.lastNameTextView.setText("");
                holder.jobTextView.setText("");
            }
        } catch (Exception e) {
            Log.e("EmployeeGridAdapter", "Error in getView: " + e.getMessage());
            holder.nameTextView.setText("");
            holder.lastNameTextView.setText("");
            holder.jobTextView.setText("");
        }

        return convertView;
    }

    static class ViewHolder {
        TextView nameTextView;
        TextView lastNameTextView;
        TextView jobTextView;
    }
}