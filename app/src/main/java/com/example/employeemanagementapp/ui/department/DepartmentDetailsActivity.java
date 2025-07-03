package com.example.employeemanagementapp.ui.department;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;


import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class DepartmentDetailsActivity extends AppCompatActivity {

    private static final int EDIT_DEPARTMENT_REQUEST_CODE = 3;

    private DepartmentDAO departmentDAO;

    private long departmentId;
    private TextView textDeptName;
    private ListView listPositions;
    private ArrayAdapter<String> positionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_department_details);

        departmentDAO = new DepartmentDAO(this);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        departmentId = getIntent().getLongExtra("departmentId", -1);
        Log.d("DepartmentDetails", "Received departmentId: " + departmentId);
        if (departmentId == -1) {
            Toast.makeText(this, R.string.invalid_department_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textDeptName = findViewById(R.id.text_dept_name);
        listPositions = findViewById(R.id.list_positions);

        // Khởi tạo adapter cho ListView
        positionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listPositions.setAdapter(positionsAdapter);

        loadDepartmentDetails();

        // Xử lý sự kiện nút Back
        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý sự kiện nút Edit
        ImageView actionEdit = findViewById(R.id.action_edit);
        actionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DepartmentDetailsActivity.this, EditDepartmentActivity.class);
                intent.putExtra("departmentId", departmentId);
                startActivityForResult(intent, EDIT_DEPARTMENT_REQUEST_CODE);
            }
        });

        // Xử lý sự kiện nút Delete
        ImageView actionDelete = findViewById(R.id.action_delete);
        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteDepartment();
            }
        });
    }

    private void loadDepartmentDetails() {
        Cursor cursor = null;

        try {
            cursor = departmentDAO.getDepartmentById(departmentId);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(Constants.COLUMN_DEPT_NAME);
                int positionsIndex = cursor.getColumnIndex(Constants.COLUMN_DEPT_POSITIONS);

                if (nameIndex == -1 || positionsIndex == -1) {
                    Log.e("DepartmentDetails", "Column not found: nameIndex=" + nameIndex + ", positionsIndex=" + positionsIndex);
                    Toast.makeText(this, R.string.department_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String name = cursor.getString(nameIndex);
                String positions = cursor.getString(positionsIndex);
                Log.d("DepartmentDetails", "Loaded department: name=" + name + ", positions=" + positions);

                textDeptName.setText(name != null ? name : "");

                // Tải danh sách chức vụ vào ListView
                positionsAdapter.clear();
                if (positions != null && !positions.isEmpty()) {
                    String[] positionsArray = positions.split(",");
                    for (String position : positionsArray) {
                        positionsAdapter.add(position.trim());
                    }
                } else {
                    positionsAdapter.add("Không có chức vụ");
                }
                positionsAdapter.notifyDataSetChanged();
            } else {
                Log.e("DepartmentDetails", "No department found with ID: " + departmentId);
                Toast.makeText(this, R.string.department_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("DepartmentDetails", "Error loading department details: " + e.getMessage());
            Toast.makeText(this, R.string.department_not_found, Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void confirmDeleteDepartment() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_department_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDepartment();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteDepartment() {
        int rowsDeleted = departmentDAO.deleteDepartment(this, departmentId);
        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.department_deleted_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.department_deleted_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_DEPARTMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            loadDepartmentDetails();
            setResult(RESULT_OK);
        }
    }

    private void applyLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedLanguage = preferences.getString("selected_language", "");
        Locale newLocale;
        if (selectedLanguage.equals("Tiếng Việt")) {
            newLocale = new Locale("vi");
        } else {
            newLocale = Locale.ENGLISH;
        }
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}