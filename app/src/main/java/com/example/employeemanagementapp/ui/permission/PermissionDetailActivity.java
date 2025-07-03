package com.example.employeemanagementapp.ui.permission;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.dao.PermissionDAO;
import com.example.employeemanagementapp.ui.department.DepartmentDetailsActivity;
import com.example.employeemanagementapp.ui.department.EditDepartmentActivity;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class PermissionDetailActivity extends AppCompatActivity {

    private static final int EDIT_REQUEST_CODE = 3;

    private PermissionDAO permissionDAO;

    private long permissionId;
    private TextView textPerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_permission_detail);

        permissionDAO = new PermissionDAO(this);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        permissionId = getIntent().getLongExtra("permissionId", -1);
        if (permissionId == -1) {
            Toast.makeText(this, R.string.invalid_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textPerName = findViewById(R.id.text_dept_name);

        loadPerDetails();

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
                Intent intent = new Intent(PermissionDetailActivity.this, EditPermissionActivity.class);
                intent.putExtra("permissionId", permissionId);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
            }
        });

        // Xử lý sự kiện nút Delete
        ImageView actionDelete = findViewById(R.id.action_delete);
        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeletePer();
            }
        });
    }

    private void loadPerDetails() {
        Cursor cursor = null;

        try {
            cursor = permissionDAO.getPermissionCursorById(permissionId);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(Constants.COLUMN_PERMISSION_NAME);

                if (nameIndex == -1 ) {
                    Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String name = cursor.getString(nameIndex);
                textPerName.setText(name != null ? name : "");

            } else {
                Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void confirmDeletePer() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_message)
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
        int rowsDeleted = permissionDAO.deletePermission(permissionId);
        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.deleted_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            loadPerDetails();
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