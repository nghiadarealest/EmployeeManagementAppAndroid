package com.example.employeemanagementapp.ui.permission;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.dao.PermissionDAO;
import com.example.employeemanagementapp.utils.Constants;

import java.util.Locale;

public class EditPermissionActivity extends AppCompatActivity {

    private PermissionDAO permissionDAO;
    private long permissionId;
    private EditText editTextPerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_edit_permission);

        permissionDAO = new PermissionDAO(this);

        permissionId = getIntent().getLongExtra("permissionId", -1);
        if (permissionId == -1) {
            Toast.makeText(this, R.string.invalid_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextPerName = findViewById(R.id.edittext_per_name);

        loadPerDetails();

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView validateIcon = findViewById(R.id.image_validate);
        validateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDepartment();
            }
        });
    }

    private void loadPerDetails() {
        Cursor cursor = null;
        try {
            cursor = permissionDAO.getPermissionCursorById(permissionId);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(Constants.COLUMN_PERMISSION_NAME);

                if (nameIndex == -1) {
                    Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String name = cursor.getString(nameIndex);

                editTextPerName.setText(name != null ? name : "");
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

    private void updateDepartment() {
        String deptName = editTextPerName.getText().toString().trim().toUpperCase();

        if (deptName.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsAffected = permissionDAO.updatePermission(permissionId, deptName);
        if (rowsAffected > 0) {
            Toast.makeText(this, R.string.added_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.added_failed, Toast.LENGTH_SHORT).show();
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