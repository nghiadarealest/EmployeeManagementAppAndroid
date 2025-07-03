package com.example.employeemanagementapp.ui.role;

import android.annotation.SuppressLint;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.ui.user.AddUserActivity;
import com.example.employeemanagementapp.ui.user.UserActivity;
import com.example.employeemanagementapp.ui.user.UserDetailsActivity;
import com.example.employeemanagementapp.utils.Constants;

import java.util.Locale;

public class RoleActivity extends AppCompatActivity {

    private ListView roleListView;
    private SimpleCursorAdapter roleAdapter;
    private RoleDAO roleDao;
    private static final int ADD_ROLE_REQUEST_CODE = 1;
    private static final int ROLE_DETAILS_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        roleListView = findViewById(R.id.role_listview);
        roleDao = new RoleDAO(this);

        displayRoles();

        roleListView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            @SuppressLint("Range") long roleId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_ROLE_ID));
            Intent intent = new Intent(RoleActivity.this, RoleDetailActivity.class);
            intent.putExtra("roleId", roleId);
            startActivityForResult(intent, ADD_ROLE_REQUEST_CODE);
        });

        findViewById(R.id.button_add_role).setOnClickListener(v -> {
            Intent intent = new Intent(RoleActivity.this, AddRoleActivity.class);
            startActivityForResult(intent, ROLE_DETAILS_REQUEST_CODE);
        });

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayRoles() {
        Cursor cursor = roleDao.getAllRolesCursor();
        Log.d("RoleActivity", "Role count: " + (cursor != null ? cursor.getCount() : 0));

        if (cursor != null && cursor.moveToFirst()) {
            if (roleAdapter == null) {
                roleAdapter = new SimpleCursorAdapter(
                        this,
                        R.layout.list_item_role,        // layout item của role
                        cursor,
                        new String[]{Constants.COLUMN_ROLE_NAME}, // tên cột role trong DB
                        new int[]{R.id.text_rolename}, // id TextView trong list_item_role.xml
                        0
                );
                roleListView.setAdapter(roleAdapter);
            } else {
                roleAdapter.changeCursor(cursor);
            }
        } else {
            roleListView.setAdapter(null);
            if (cursor != null) {
                cursor.close();
            }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            displayRoles(); // Cập nhật danh sách sau khi thêm hoặc xóa/sửa người dùng
        }
    }
}