package com.example.employeemanagementapp.adapter.employee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.model.Employee;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;

public class EmployeeListAdapter extends ArrayAdapter<Employee> {

    private DepartmentDAO departmentDAO;
    private Context context;

    public EmployeeListAdapter(Context context, ArrayList<Employee> employees) {
        super(context, 0, employees);
        departmentDAO = new DepartmentDAO(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Employee employee = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        ImageView profileImageView = convertView.findViewById(R.id.image_profile);
        TextView firstLastNameTextView = convertView.findViewById(R.id.text_name);
        TextView jobTitleTextView = convertView.findViewById(R.id.text_job);
        View statusBar = convertView.findViewById(R.id.status_bar);

        firstLastNameTextView.setText(employee.getFirstName() + " " + employee.getLastName());

        String departmentName = "Unknown";
        Cursor deptCursor = departmentDAO.getDepartmentById(employee.getDepartmentId());
        if (deptCursor != null && deptCursor.moveToFirst()) {
            @SuppressLint("Range") String name = deptCursor.getString(deptCursor.getColumnIndex(Constants.COLUMN_DEPT_NAME));
            departmentName = name != null ? name : "Unknown";
            deptCursor.close();
        }

        jobTitleTextView.setText(departmentName + " - " + employee.getPosition());

        byte[] imageBytes = employee.getImage(); // giả sử bạn có getter getImage() trả về byte[]
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            profileImageView.setImageBitmap(bitmap);
        } else {
            profileImageView.setImageResource(R.drawable.ic_launcher_background); // ảnh mặc định khi chưa có ảnh
        }

        setStatusBarColor(statusBar, employee.getStatus());

        return convertView;
    }

    private void setStatusBarColor(View statusBar, String status) {
        int color;
        if (status == null) {
            status = "ACTIVE"; // Mặc định là active nếu status null
        }

        switch (status) {
            case "Đang làm việc":
            case "Currently Working":
                color = ContextCompat.getColor(context, R.color.status_active);
                break;
            case "Resigned":
            case "Đã Nghỉ":
                color = ContextCompat.getColor(context, R.color.status_inactive);
                break;
            case "On Maternity Leave":
            case "Đang nghỉ thai sản":
                color = ContextCompat.getColor(context, R.color.status_maternity);
                break;
            default:
                color = ContextCompat.getColor(context, R.color.status_active);
                break;
        }
        statusBar.setBackgroundColor(color);
    }
}