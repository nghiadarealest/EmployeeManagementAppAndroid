package com.example.employeemanagementapp.ui.user; // Cập nhật package

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.db.model.Role;
import com.example.employeemanagementapp.utils.Constants;

import java.util.Arrays;
import java.util.List;

public class AddUserActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private UserDAO userDAO;
    private RoleDAO roleDAO;
    private Spinner spinnerRole;
    private Role selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);

        userDAO = new UserDAO(this);

        spinnerRole = findViewById(R.id.spinnerRole);

        roleDAO = new RoleDAO(this);

        List<Role> roles = roleDAO.getAllRoles();

        Log.e("orr", String.valueOf(roles));
        // Adapter để hiển thị role
        ArrayAdapter<Role> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = roles.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = null;
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

    // Hàm xử lý thêm người dùng mới
    public void addUser(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        int roleId = selectedRole != null ? selectedRole.getId() : -1;

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.username_pasword_notempty, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isRegistered = userDAO.registerUser(username, password, roleId);
        if (isRegistered) {
            Toast.makeText(this, R.string.added_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish(); // Quay lại trang trước
        } else {
            Toast.makeText(this, R.string.added_failed, Toast.LENGTH_SHORT).show();
        }
    }
}