package com.example.employeemanagementapp.ui.department;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;

import java.util.Locale;

public class AddDepartmentActivity extends AppCompatActivity {

    private EditText editTextDeptName, editTextDeptPositions;
    private ImageView imageViewValidate, imageViewBack;
    private DepartmentDAO departmentDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_add_department);

        editTextDeptName = findViewById(R.id.edittext_dept_name);
        editTextDeptPositions = findViewById(R.id.edittext_dept_positions);
        imageViewValidate = findViewById(R.id.image_validate);
        imageViewBack = findViewById(R.id.image_back);

        departmentDAO = new DepartmentDAO(this);

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageViewValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDepartment();
            }
        });
    }

    private void addDepartment() {
        String deptName = editTextDeptName.getText().toString().trim();
        String deptPositions = editTextDeptPositions.getText().toString().trim();

        if (deptName.isEmpty() || deptPositions.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        long result = departmentDAO.insertDepartment(deptName, deptPositions);

        if (result != -1) {
            Toast.makeText(this, R.string.department_added_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.department_added_failed, Toast.LENGTH_SHORT).show();
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