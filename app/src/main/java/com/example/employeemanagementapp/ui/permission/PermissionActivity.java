package com.example.employeemanagementapp.ui.permission;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.PermissionDAO;
import com.example.employeemanagementapp.utils.Constants;

import java.util.Locale;

public class PermissionActivity extends AppCompatActivity {

    private SimpleCursorAdapter listAdapter;
    private ListView listView;
    PermissionDAO permissionDAO;
    private static final int ADD_PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_permission);

        permissionDAO = new PermissionDAO(this);

        listView = findViewById(R.id.permission_listview);

        displayPermission();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                @SuppressLint("Range") long permissionId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_PERMISSION_ID));
                Intent intent = new Intent(PermissionActivity.this, PermissionDetailActivity.class);
                intent.putExtra("permissionId", permissionId);
                startActivityForResult(intent, 1);
            }
        });

        findViewById(R.id.button_add_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, AddPermissionActivity.class);
                startActivityForResult(intent, ADD_PERMISSION_REQUEST_CODE);
            }
        });

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayPermission() {
        try {
            Cursor cursor = permissionDAO.getAllPermissionsCursor();
            if (cursor != null && cursor.moveToFirst()) {
                if (listAdapter == null) {
                    listAdapter = new SimpleCursorAdapter(
                            this,
                            R.layout.list_item_permission,
                            cursor,
                            new String[]{Constants.COLUMN_PERMISSION_NAME},
                            new int[]{R.id.text_permission_name},
                            0);
                    listView.setAdapter(listAdapter);
                } else {
                    listAdapter.changeCursor(cursor);
                }
                listView.setVisibility(View.VISIBLE);
            } else {
                Log.d("Activity", "Not found in the database.");
                if (cursor != null) {
                    cursor.close();
                }
                listView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("Activity", "Error accessing database: " + e.getMessage());
            Toast.makeText(this, R.string.error_loading_departments, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_PERMISSION_REQUEST_CODE || requestCode == 1) && resultCode == RESULT_OK) {
            displayPermission(); // Làm mới danh sách sau khi thêm hoặc chỉnh sửa/xóa
        }
    }

    private void applyLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedLanguage = preferences.getString("selected_language", "");
        Log.d("selected language", selectedLanguage);
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