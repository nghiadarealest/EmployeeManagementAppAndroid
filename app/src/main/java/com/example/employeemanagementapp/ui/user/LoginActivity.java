package com.example.employeemanagementapp.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagementapp.MainActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.UserDAO;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginBtn, registerBtn;
    ImageView btnTogglePassword;

    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout); // layout của form đăng nhập

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextNumberPassword);
        loginBtn = findViewById(R.id.dang_nhap);
        registerBtn = findViewById(R.id.dang_ky);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        userDAO = new UserDAO(this); // Khởi tạo SQLite helper

        // Bắt sự kiện giữ để hiện mật khẩu
        btnTogglePassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Hiện mật khẩu
                        passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Ẩn mật khẩu
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
                        return true;
                }
                return false;
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                Log.e("a", "a");
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, R.string.username_pasword_notempty, Toast.LENGTH_SHORT).show();
                } else {
                    boolean isValid = userDAO.checkLogin(username, password);
                    if (isValid) {
                        int userId = userDAO.checkLoginAndGetUserId(username, password);
                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("authUserId", userId);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,  R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
