package com.example.employeemanagementapp.ui.user;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.utils.Constants;

import java.util.Locale;

public class UserActivity extends AppCompatActivity {

    private EditText searchInput;
    private ListView userListView;
    private SimpleCursorAdapter userAdapter;
    private UserDAO userDAO;

    private static final int ADD_USER_REQUEST_CODE = 1;
    private static final int USER_DETAILS_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        searchInput = findViewById(R.id.search_user_input);
        userListView = findViewById(R.id.user_listview);
        userDAO = new UserDAO(this);

        displayUsers();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().trim());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        userListView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id")); // Sử dụng _id đã ánh xạ
            Intent intent = new Intent(UserActivity.this, UserDetailsActivity.class);
            intent.putExtra("userId", userId);
            startActivityForResult(intent, USER_DETAILS_REQUEST_CODE);
        });

        findViewById(R.id.button_add_user).setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, AddUserActivity.class);
            startActivityForResult(intent, ADD_USER_REQUEST_CODE);
        });

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayUsers() {
        Cursor cursor = userDAO.getAllUsers();
        Log.d("UserActivity", "User count: " + (cursor != null ? cursor.getCount() : 0));
        if (cursor != null && cursor.moveToFirst()) {
            if (userAdapter == null) {
                userAdapter = new SimpleCursorAdapter(
                        this,
                        R.layout.list_item_user,
                        cursor,
                        new String[]{Constants.COLUMN_USER_NAME},
                        new int[]{R.id.text_username},
                        0
                );
                userListView.setAdapter(userAdapter);
            } else {
                userAdapter.changeCursor(cursor);
            }
        } else {
            userListView.setAdapter(null);
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void filterUsers(String query) {
        Cursor cursor = userDAO.searchUsers(query);
        if (cursor != null && cursor.moveToFirst()) {
            userAdapter.changeCursor(cursor);
        } else {
            userListView.setAdapter(null);
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
            displayUsers(); // Cập nhật danh sách sau khi thêm hoặc xóa/sửa người dùng
        }
    }
}