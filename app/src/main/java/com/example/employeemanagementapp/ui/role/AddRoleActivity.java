package com.example.employeemanagementapp.ui.role;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.PermissionDAO;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.dao.UserDAO;

import java.util.ArrayList;
import java.util.List;

public class AddRoleActivity extends AppCompatActivity {

    private EditText edtRoleName;
    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;

    private List<String> selectedPermissions = new ArrayList<>();
    private List<String> permissionList = new ArrayList<>();
    private boolean[] selectedStates ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_role);

        edtRoleName = findViewById(R.id.edtRoleName);

        roleDAO = new RoleDAO(this);
        permissionDAO = new PermissionDAO(this);
        permissionList = permissionDAO.getAllPermissions(); // giả sử trả về List<String>
        selectedStates = new boolean[permissionList.size()];

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Hàm xử lý thêm người dùng mới
    public void addRole(View view) {
        String roleName = edtRoleName.getText().toString().trim();

        if (roleName.isEmpty()) {
            Toast.makeText(this, R.string.role_not_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPermissions.isEmpty()) {
            Toast.makeText(this, R.string.please_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        long newRoleId = roleDAO.insertRole(roleName);
        if (newRoleId != -1) {
            // ✅ Gán permissions cho role
            PermissionDAO permissionDAO = new PermissionDAO(this);
            List<Long> permissionIds = permissionDAO.getPermissionIdsByNames(selectedPermissions);
            roleDAO.insertRolePermissions(newRoleId, permissionIds);

            Toast.makeText(this, R.string.added_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish(); // Quay lại trang trước
        } else {
            Toast.makeText(this, R.string.added_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public void showPermissionDialog(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_permission_for_role)
                .setMultiChoiceItems(permissionList.toArray(new CharSequence[0]), selectedStates, (dialog, which, isChecked) -> {
                    selectedStates[which] = isChecked;
                })
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    selectedPermissions.clear();
                    for (int i = 0; i < permissionList.size(); i++) {
                        if (selectedStates[i]) {
                            selectedPermissions.add(permissionList.get(i));
                        }
                    }
                    updatePermissionText();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updatePermissionText() {
        TextView tv = findViewById(R.id.tvSelectedPermissions);
        if (selectedPermissions.isEmpty()) {
            tv.setText("Chưa chọn quyền nào");
        } else {
            tv.setText("Đã chọn: " + android.text.TextUtils.join(", ", selectedPermissions));
        }
    }
}