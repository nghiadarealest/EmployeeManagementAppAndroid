package com.example.employeemanagementapp.ui.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.db.model.Role;
import com.example.employeemanagementapp.db.model.User;
import com.example.employeemanagementapp.ui.role.EditRoleActivity;
import com.example.employeemanagementapp.ui.role.RoleDetailActivity;

public class UserDetailsActivity extends AppCompatActivity {

    private static final int EDIT_USER_REQUEST_CODE = 2;

    private TextView textUsername, textRole;
    private UserDAO userDAO;
    private RoleDAO roleDAO;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        textUsername = findViewById(R.id.text_username);
        textRole = findViewById(R.id.text_role);

        userDAO = new UserDAO(this);
        roleDAO = new RoleDAO(this);


        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayUserDetails();


        // Xử lý sự kiện nút Delete
        ImageView actionDelete = findViewById(R.id.action_delete);
        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteUser();
            }
        });


        // Xử lý sự kiện nút Edit
        ImageView actionEdit = findViewById(R.id.action_edit);
        actionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailsActivity.this, EditUserActivity.class);
                intent.putExtra("userId", userId);
                startActivityForResult(intent, EDIT_USER_REQUEST_CODE);
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

    private void displayUserDetails() {
        User user = userDAO.getUserById(userId);
        String role = roleDAO.getRoleNameByIdLong(user.getRoleId());
        if (user != null) {
            textUsername.setText(String.format("%s: %s", getString(R.string.username), user.getUsername()));
            textRole.setText(String.format("%s: %s", getString(R.string.role_name), String.valueOf(role)));
        } else {
            Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void confirmDeleteUser() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUser();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteUser() {
        int rowsDeleted = userDAO.deleteUser(userId);
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
        if (requestCode == EDIT_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            displayUserDetails(); // Cập nhật thông tin sau khi sửa
            setResult(RESULT_OK); // Thông báo cho UserActivity cập nhật danh sách
        }
    }
}