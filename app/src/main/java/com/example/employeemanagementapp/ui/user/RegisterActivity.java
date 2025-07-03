package com.example.employeemanagementapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.UserDAO;

public class RegisterActivity extends AppCompatActivity {

    EditText emailField, passwordField1, passwordField2;
    Button btnRegister, btnBack;
    ImageView btnTogglePassword2, btnTogglePassword3;

    boolean isPasswordVisible1 = false;
    boolean isPasswordVisible2 = false;

    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout); // layout XML chứa form đăng ký

        emailField = findViewById(R.id.txtdangky);
        passwordField1 = findViewById(R.id.txtpass1);
        passwordField2 = findViewById(R.id.txtpass2);
        btnRegister = findViewById(R.id.dang_ky2);
        btnBack = findViewById(R.id.quay_lai);

        btnTogglePassword2 = findViewById(R.id.btnTogglePassword2);
        btnTogglePassword3 = findViewById(R.id.btnTogglePassword3);

        userDAO = new UserDAO(this); // Khởi tạo SQLite helper

        btnTogglePassword2.setOnClickListener(v -> {
            if (isPasswordVisible1) {
                passwordField1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword2.setImageResource(R.drawable.ic_eye_closed);
            } else {
                passwordField1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword2.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible1 = !isPasswordVisible1;
            passwordField1.setSelection(passwordField1.getText().length());
        });

        btnTogglePassword3.setOnClickListener(v -> {
            if (isPasswordVisible2) {
                passwordField2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword3.setImageResource(R.drawable.ic_eye_closed);
            } else {
                passwordField2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword3.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible2 = !isPasswordVisible2;
            passwordField2.setSelection(passwordField2.getText().length());
        });

        btnRegister.setOnClickListener(v -> {
            String username = emailField.getText().toString().trim();
            String pass1 = passwordField1.getText().toString();
            String pass2 = passwordField2.getText().toString();

            if (username.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            } else if (!pass1.equals(pass2)) {
                Toast.makeText(this, R.string.password_not_match, Toast.LENGTH_SHORT).show();
            } else {
                // Thực hiện đăng ký với SQLite
                boolean isRegistered = userDAO.registerUser(username, pass1, 0);
                if (isRegistered) {
                    Toast.makeText(this, R.string.reg_success, Toast.LENGTH_SHORT).show();
                    // Chuyển về LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this,  R.string.reg_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            finish(); // quay lại LoginActivity
        });
    }
}
