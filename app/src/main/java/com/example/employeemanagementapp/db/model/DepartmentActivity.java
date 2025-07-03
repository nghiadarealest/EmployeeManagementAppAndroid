package com.example.employeemanagementapp.db.model;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.ui.department.AddDepartmentActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.utils.Constants;

import java.util.Locale;

public class DepartmentActivity extends AppCompatActivity {

    private static final int ADD_DEPARTMENT_REQUEST_CODE = 2;

    private DepartmentDAO departmentDAO;
    private SimpleCursorAdapter listAdapter;
    private EditText searchInput;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_department);

        departmentDAO = new DepartmentDAO(this);

        listView = findViewById(R.id.department_listview);
        searchInput = findViewById(R.id.search_input);

        displayDepartments();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterDepartmentList(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.button_add_department).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DepartmentActivity.this, AddDepartmentActivity.class);
                startActivityForResult(intent, ADD_DEPARTMENT_REQUEST_CODE);
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

    private void displayDepartments() {
        try {
            Cursor cursor = departmentDAO.getAllDepartments();
            if (cursor != null && cursor.moveToFirst()) {
                if (listAdapter == null) {
                    listAdapter = new SimpleCursorAdapter(
                            this,
                            R.layout.list_item_department,
                            cursor,
                            new String[]{Constants.COLUMN_DEPT_NAME, Constants.COLUMN_DEPT_POSITIONS},
                            new int[]{R.id.text_dept_name, R.id.text_dept_positions_label},
                            0);
                    listView.setAdapter(listAdapter);
                } else {
                    listAdapter.changeCursor(cursor);
                }
                listView.setVisibility(View.VISIBLE);
            } else {
                Log.d("DepartmentActivity", "No departments found in the database.");
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e("DepartmentActivity", "Error accessing database: " + e.getMessage());
        }
    }

    private void filterDepartmentList(String query) {
        Cursor cursor = departmentDAO.getAllDepartments(); // Simplified; can add filtering if needed
        if (cursor != null) {
            listAdapter.changeCursor(cursor);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_DEPARTMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            displayDepartments();
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