package com.example.employeemanagementapp.ui.role;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.dao.PermissionDAO;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.ui.department.DepartmentDetailsActivity;
import com.example.employeemanagementapp.ui.department.EditDepartmentActivity;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoleDetailActivity extends AppCompatActivity {

    private static final int EDIT_REQUEST_CODE = 3;

    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;
    private long roleId;
    private TextView textName;
    private ListView listPositions;
    private ArrayAdapter<String> positionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_role_detail);

        roleId = getIntent().getLongExtra("roleId", -1);
        if (roleId == -1) {
            Toast.makeText(this, R.string.invalid_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roleDAO = new RoleDAO(this);
        permissionDAO = new PermissionDAO(this);

        textName = findViewById(R.id.text_name);
        listPositions = findViewById(R.id.list_positions);

        // Khởi tạo adapter cho ListView
        positionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listPositions.setAdapter(positionsAdapter);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        loadRoleDetails();

        // Xử lý sự kiện nút Back
        ImageView backIcon = findViewById(R.id.image_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Xử lý sự kiện nút Edit
        ImageView actionEdit = findViewById(R.id.action_edit);
        actionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleDetailActivity.this, EditRoleActivity.class);
                intent.putExtra("roleId", roleId);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
            }
        });

        // Xử lý sự kiện nút Delete
        ImageView actionDelete = findViewById(R.id.action_delete);
        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteDepartment();
            }
        });
    }


    private void loadRoleDetails() {
        Cursor roleCursor = null;
        try {
            roleCursor = roleDAO.getRoleByIdCursor(roleId); // Lấy thông tin role từ ID
            if (roleCursor != null && roleCursor.moveToFirst()) {
                int nameIndex = roleCursor.getColumnIndex(Constants.COLUMN_ROLE_NAME);
                if (nameIndex == -1) {
                    Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String roleName = roleCursor.getString(nameIndex);

                textName.setText(roleName != null ? roleName : "");

                // Lấy danh sách permissions của role
                List<String> permissions = permissionDAO.getPermissionsByRoleId(roleId);

                positionsAdapter.clear();
                if (permissions != null && !permissions.isEmpty()) {
                    for (String permission : permissions) {
                        positionsAdapter.add(permission);
                    }
                } else {
                    positionsAdapter.add("Không có quyền");
                }

                positionsAdapter.notifyDataSetChanged();

            } else {
                Log.e("RoleDetails", "No role found with ID: " + roleId);
                Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("RoleDetails", "Error loading role details: " + e.getMessage());
            Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            if (roleCursor != null) {
                roleCursor.close();
            }
        }
    }

    private void confirmDeleteDepartment() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.delete_department_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDepartment();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteDepartment() {
        int rowsDeleted = roleDAO.deleteRole(roleId);
        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.department_deleted_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.department_deleted_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            loadRoleDetails();
            setResult(RESULT_OK);
        }
    }

    private void applyLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedLanguage = preferences.getString("selected_language", "");
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