package com.example.employeemanagementapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.employeemanagementapp.db.model.User;

import java.util.List;

public class EditUserActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Spinner spinnerRole;
    private Role selectedRole;
    private UserDAO userDAO;
    private RoleDAO roleDAO;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        spinnerRole = findViewById(R.id.spinnerRole);

        userDAO = new UserDAO(this);
        roleDAO = new RoleDAO(this);

        // Lấy userId từ intent
        int userId = getIntent().getIntExtra("userId", -1);
        currentUser = userDAO.getUserById(userId); // Bạn cần cài đặt hàm này trong UserDAO

        if (currentUser == null) {
            Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtUsername.setText(currentUser.getUsername());
        edtPassword.setText(currentUser.getPassword()); // Chỉ nếu cho phép chỉnh sửa password

        List<Role> roles = roleDAO.getAllRoles();
        ArrayAdapter<Role> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Chọn role hiện tại của user
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getId() == currentUser.getRoleId()) {
                spinnerRole.setSelection(i);
                selectedRole = roles.get(i);
                break;
            }
        }

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

    public void updateUser(View view) {
        String newUsername = edtUsername.getText().toString().trim();
        String newPassword = edtPassword.getText().toString().trim();
        int newRoleId = selectedRole != null ? selectedRole.getId() : -1;

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, R.string.username_pasword_notempty, Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setUsername(newUsername);
        currentUser.setPassword(newPassword);
        currentUser.setRoleId(Long.valueOf(newRoleId));

        int updateResult = userDAO.updateUser(currentUser);

        if (updateResult > 0) {
            Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
        }
    }
}