package com.example.employeemanagementapp.ui.role;

import android.app.AlertDialog;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class EditRoleActivity extends AppCompatActivity {

    private EditText edtRoleName;
    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;

    private List<String> selectedPermissions = new ArrayList<>();
    private List<String> permissionList = new ArrayList<>();
    private boolean[] selectedStates;

    private long roleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_role);

        edtRoleName = findViewById(R.id.edtRoleName);
        roleDAO = new RoleDAO(this);
        permissionDAO = new PermissionDAO(this);

        roleId = getIntent().getLongExtra("roleId", -1);
        if (roleId == -1) {
            Toast.makeText(this, R.string.invalid_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        permissionList = permissionDAO.getAllPermissions();
        selectedStates = new boolean[permissionList.size()];

        // Load role name and selected permissions
        String roleName = roleDAO.getRoleNameById(roleId);
        List<String> currentPermissions = permissionDAO.getPermissionNamesByRoleId(roleId);
        selectedPermissions.addAll(currentPermissions);
        edtRoleName.setText(roleName);

        for (int i = 0; i < permissionList.size(); i++) {
            selectedStates[i] = currentPermissions.contains(permissionList.get(i));
        }

        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(v -> finish());

        updatePermissionText();
    }

    public void updateRole(View view) {
        String roleName = edtRoleName.getText().toString().trim();

        if (roleName.isEmpty()) {
            Toast.makeText(this, R.string.role_not_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPermissions.isEmpty()) {
            Toast.makeText(this, R.string.please_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = roleDAO.updateRoleName(roleId, roleName);
        if (updated) {
            List<Long> permissionIds = permissionDAO.getPermissionIdsByNames(selectedPermissions);
            roleDAO.updateRolePermissions(roleId, permissionIds);

            Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
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