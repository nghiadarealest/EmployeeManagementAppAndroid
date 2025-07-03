package com.example.employeemanagementapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.adapter.employee.EmployeeGridAdapter;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.ui.department.DepartmentActivity;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.dao.EmployeeDAO;
import com.example.employeemanagementapp.db.dao.PermissionDAO;
import com.example.employeemanagementapp.db.dao.RoleDAO;
import com.example.employeemanagementapp.db.model.User;
import com.example.employeemanagementapp.ui.employee.AddEmployeeActivity;
import com.example.employeemanagementapp.ui.employee.EmployeeDetails;
import com.example.employeemanagementapp.ui.permission.PermissionActivity;
import com.example.employeemanagementapp.ui.role.RoleActivity;
import com.example.employeemanagementapp.ui.setting.SettingsActivity;
import com.example.employeemanagementapp.ui.user.LoginActivity;
import com.example.employeemanagementapp.ui.user.UserActivity;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_EMPLOYEE_REQUEST_CODE = 1;

    private DepartmentDAO departmentDAO;
    private UserDAO userDAO;
    private SimpleCursorAdapter listAdapter;
    private EmployeeGridAdapter gridAdapter;
    private EditText searchInput;
    private ListView listView;
    private GridLayout gridLayout;
    private TextView noEmployeesText;
    private TextView headerTitle;
    private HashMap<Long, String> departmentMap;
    private LinearLayout menuPanel;
    private View overlay;
    private ImageView imageMenu;
    private ImageView imageSortDepartment;
    private Button btnAddEmployee;
    private LinearLayout llUser, llDepartment, llRole, llPermission;
    private boolean isMenuOpen = false;
    private long selectedDeptId = -1; // -1 means all departments

    private BroadcastReceiver languageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("LANGUAGE_CHANGED".equals(intent.getAction())) {
                String newLanguage = intent.getStringExtra("new_language");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putString("selected_language", newLanguage).apply();
                applyLanguage();
                updateUIText(); // Cập nhật văn bản giao diện ngay lập tức
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage(); // Áp dụng ngôn ngữ trước khi setContentView
        setContentView(R.layout.activity_main);

        // Khởi tạo DAO
        userDAO = new UserDAO(this);
        departmentDAO = new DepartmentDAO(this);
        departmentMap = new HashMap<>();

        // Khởi tạo các view
        searchInput = findViewById(R.id.search_input);
        listView = findViewById(R.id.listview);
        gridLayout = findViewById(R.id.gridlayout);
        noEmployeesText = findViewById(R.id.text_no_employees);
        headerTitle = findViewById(R.id.text_employee_list);
        menuPanel = findViewById(R.id.menu_panel);
        imageMenu = findViewById(R.id.image_menu);
        imageSortDepartment = findViewById(R.id.image_sort_department);
        overlay = findViewById(R.id.overlay);
        llUser = findViewById(R.id.btn_user);
        llDepartment = findViewById(R.id.menu_departments);
        llRole = findViewById(R.id.btn_role);
        llPermission = findViewById(R.id.menu_permission);
        btnAddEmployee = findViewById(R.id.button_add_employee);

        // Đăng ký BroadcastReceiver
        IntentFilter filter = new IntentFilter("LANGUAGE_CHANGED");
        ContextCompat.registerReceiver(this, languageChangeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // Kiểm tra view có tồn tại
        if (listView == null || gridLayout == null) {
            Log.e("MainActivity", "ListView or GridLayout not found in layout");
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Kiểm tra quyền người dùng
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("authUserId", -1);
        if (userId != -1) {
            if (!userDAO.userHasPermission(userId, Constants.VIEW_USER)) {
                llUser.setVisibility(View.GONE);
            }
            if (!userDAO.userHasPermission(userId, Constants.VIEW_DEPARTMENT)) {
                llDepartment.setVisibility(View.GONE);
            }
            if (!userDAO.userHasPermission(userId, Constants.VIEW_ROLE)) {
                llRole.setVisibility(View.GONE);
            }
            if (!userDAO.userHasPermission(userId, Constants.VIEW_PERMISSION)) {
                llPermission.setVisibility(View.GONE);
            }
            if (!userDAO.userHasPermission(userId, Constants.VIEW_EMPLOYEE)) {
                searchInput.setVisibility(View.GONE);
                imageSortDepartment.setVisibility(View.GONE);
            }
            if (!userDAO.userHasPermission(userId, Constants.ADD_EMPLOYEE)) {
                btnAddEmployee.setVisibility(View.GONE);
            }
            if (userDAO.userHasPermission(userId, Constants.VIEW_EMPLOYEE)) {
                displayEmployees();
                loadDepartments();
            }
        }

        // Thiết lập TextWatcher cho ô tìm kiếm
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterEmployeeList(query);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Thiết lập sự kiện click
        imageMenu.setOnClickListener(v -> {
            if (isMenuOpen) {
                closeMenu();
            } else {
                openMenu();
            }
        });

        imageSortDepartment.setOnClickListener(v -> showDepartmentListDialog());

        overlay.setOnClickListener(v -> closeMenu());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            @SuppressLint("Range") long employeeId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_ID));
            showEmployeeDetails(employeeId);
        });

        btnAddEmployee.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEmployeeActivity.class);
            startActivityForResult(intent, ADD_EMPLOYEE_REQUEST_CODE);
        });

        // Cập nhật văn bản giao diện ban đầu
        updateUIText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "Unregistering receiver and closing DAOs");
        unregisterReceiver(languageChangeReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyLanguage();
        updateUIText();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        applyLanguage();
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("authUserId", -1);
        if (userId != -1 && userDAO.userHasPermission(userId, Constants.VIEW_EMPLOYEE)) {
            departmentMap.clear();
            loadDepartments();
            displayEmployees();
            updateUIText();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EMPLOYEE_REQUEST_CODE && resultCode == RESULT_OK) {
            displayEmployees();
        }
    }

    @SuppressLint("Range")
    private void updateGridLayout(GridLayout gridLayout, Cursor cursor) {
        gridLayout.removeAllViews();
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        while (!cursor.isAfterLast()) {
            View itemView = inflater.inflate(R.layout.grid_item_layout, gridLayout, false);
            TextView nameTextView = itemView.findViewById(R.id.text_name);
            TextView lastNameTextView = itemView.findViewById(R.id.text_lastname);
            TextView jobTextView = itemView.findViewById(R.id.text_job);

            String firstName = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_NAME));
            long deptId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DEPARTMENT_ID));
            String position = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_POSITION)) != null ?
                    cursor.getString(cursor.getColumnIndex(Constants.COLUMN_POSITION)) : "";

            String deptName = departmentMap.getOrDefault(deptId, "Unknown");
            nameTextView.setText(firstName != null ? firstName : "");
            lastNameTextView.setText(lastName != null ? lastName : "");
            jobTextView.setText(deptName + " - " + position);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(8, 8, 8, 8);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f);
            itemView.setLayoutParams(params);

            final long employeeId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_ID));
            itemView.setOnClickListener(v -> showEmployeeDetails(employeeId));

            gridLayout.addView(itemView);
            cursor.moveToNext();
        }
    }

    private void updateHeaderTitle() {
        if (selectedDeptId == -1) {
            headerTitle.setText(R.string.list);
        } else {
            String deptName = departmentMap.getOrDefault(selectedDeptId, "Unknown");
            headerTitle.setText(getString(R.string.employees_in_department, deptName));
        }
    }

    private void showDepartmentListDialog() {
        Cursor cursor = departmentDAO.getAllDepartments();
        ArrayList<String> departmentNames = new ArrayList<>();
        ArrayList<Long> departmentIds = new ArrayList<>();

        // Thêm tùy chọn "All Departments"
        departmentNames.add(getString(R.string.all_departments));
        departmentIds.add(-1L);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DEPT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEPT_NAME));
                departmentNames.add(name != null ? name : "Unknown");
                departmentIds.add(id);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (departmentNames.isEmpty()) {
            Toast.makeText(this, R.string.no_departments_found, Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.department_list_item, R.id.department_name, departmentNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.department_name);
                ImageView checkIcon = view.findViewById(R.id.check_icon);

                long deptId = departmentIds.get(position);
                if (deptId == selectedDeptId) {
                    view.setBackgroundColor(getResources().getColor(R.color.selected_item_background));
                    textView.setTextColor(getResources().getColor(R.color.selected_item_text));
                    checkIcon.setVisibility(View.VISIBLE);
                } else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                    checkIcon.setVisibility(View.GONE);
                }
                return view;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_department)
                .setAdapter(adapter, (dialog, which) -> {
                    long deptId = departmentIds.get(which);
                    selectedDeptId = deptId;
                    filterEmployeesByDepartment(deptId);
                    updateHeaderTitle();
                    searchInput.setText(""); // Xóa ô tìm kiếm
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void filterEmployeesByDepartment(long deptId) {
        Cursor cursor = null;
        EmployeeDAO employeeDAO = new EmployeeDAO(this);
        try {
            if (deptId == -1) {
                cursor = employeeDAO.getAllEmployees();
            } else {
                cursor = employeeDAO.getEmployeesByDepartment(deptId);
            }
            if (cursor == null) {
                Log.e("MainActivity", "Cursor is null in filterEmployeesByDepartment for deptId: " + deptId);
                listView.setAdapter(null);
                gridLayout.removeAllViews();
                noEmployeesText.setVisibility(View.VISIBLE);
                return;
            }
            listAdapter.changeCursor(cursor);
            updateGridLayout(gridLayout, cursor);
            noEmployeesText.setVisibility(cursor.moveToFirst() ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            Log.e("MainActivity", "Error filtering employees by department: " + e.getMessage());
            listView.setAdapter(null);
            gridLayout.removeAllViews();
            noEmployeesText.setVisibility(View.VISIBLE);
        }
    }

    private void loadDepartments() {
        Cursor cursor = departmentDAO.getAllDepartments();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DEPT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEPT_NAME));
                departmentMap.put(id, name != null ? name : "Unknown");
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void displayEmployees() {
        Cursor cursor = null;
        EmployeeDAO employeeDAO = new EmployeeDAO(this);
        try {
            cursor = selectedDeptId == -1 ? employeeDAO.getAllEmployees() : employeeDAO.getEmployeesByDepartment(selectedDeptId);
            if (cursor != null && cursor.moveToFirst()) {
                if (listAdapter == null) {
                    listAdapter = new SimpleCursorAdapter(
                            this,
                            R.layout.list_item_layout,
                            cursor,
                            new String[]{Constants.COLUMN_FIRST_NAME, Constants.COLUMN_LAST_NAME, Constants.COLUMN_POSITION},
                            new int[]{R.id.text_name, R.id.text_lastname, R.id.text_job},
                            0) {
                        @Override
                        public void setViewText(TextView v, String text) {
                            if (v.getId() == R.id.text_job) {
                                Cursor cursor = getCursor();
                                if (cursor == null) {
                                    Log.e("MainActivity", "Cursor is null in setViewText");
                                    super.setViewText(v, "Unknown");
                                    return;
                                }
                                try {
                                    int positionIndex = cursor.getColumnIndex(Constants.COLUMN_POSITION);
                                    int deptIdIndex = cursor.getColumnIndex(Constants.COLUMN_DEPARTMENT_ID);
                                    String position = positionIndex != -1 && cursor.getString(positionIndex) != null ? cursor.getString(positionIndex) : "";
                                    long deptId = deptIdIndex != -1 ? cursor.getLong(deptIdIndex) : -1;
                                    String deptName = deptId != -1 ? departmentMap.getOrDefault(deptId, "Unknown") : "Unknown";
                                    text = deptName + " - " + position;
                                } catch (Exception e) {
                                    Log.e("MainActivity", "Error in setViewText: " + e.getMessage());
                                    text = "Unknown";
                                }
                            }
                            super.setViewText(v, text);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            super.bindView(view, context, cursor);

                            ImageView imageView = view.findViewById(R.id.image_profile);
                            int idIndex = cursor.getColumnIndex(Constants.COLUMN_ID);
                            if (idIndex != -1) {
                                long employeeId = cursor.getLong(idIndex);
                                new Thread(() -> {
                                    byte[] imageBytes = employeeDAO.getEmployeeProfileImage(employeeId);
                                    Bitmap bitmap = null;
                                    if (imageBytes != null && imageBytes.length > 0) {
                                        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    }
                                    Bitmap finalBitmap = bitmap;
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (finalBitmap != null) {
                                            imageView.setImageBitmap(finalBitmap);
                                        } else {
                                            imageView.setImageResource(R.drawable.ic_launcher_background);
                                        }
                                    });
                                }).start();
                            }

                            View statusBar = view.findViewById(R.id.status_bar);
                            int statusIndex = cursor.getColumnIndex(Constants.COLUMN_STATUS);
                            if (statusIndex != -1) {
                                String status = cursor.getString(statusIndex);
                                int color;

                                if (status == null) {
                                    status = "Đang làm việc";
                                }

                                switch (status) {
                                    case "Đang làm việc":
                                    case "Currently Working":
                                        color = ContextCompat.getColor(context, R.color.status_active);
                                        break;
                                    case "Resigned":
                                    case "Đã Nghỉ":
                                        color = ContextCompat.getColor(context, R.color.status_inactive);
                                        break;
                                    case "On Maternity Leave":
                                    case "Đang nghỉ thai sản":
                                        color = ContextCompat.getColor(context, R.color.status_maternity);
                                        break;
                                    default:
                                        color = ContextCompat.getColor(context, R.color.status_active);
                                        break;
                                }
                                statusBar.setBackgroundColor(color);
                            }
                        }
                    };
                    listView.setAdapter(listAdapter);
                } else {
                    listAdapter.changeCursor(cursor);
                }
                listView.setVisibility(View.VISIBLE);
                gridLayout.setVisibility(View.GONE);
                noEmployeesText.setVisibility(View.GONE);

                if (gridAdapter == null) {
                    gridAdapter = new EmployeeGridAdapter(this, cursor, departmentMap);
                    updateGridLayout(gridLayout, cursor);
                } else {
                    gridAdapter.changeCursor(cursor);
                    updateGridLayout(gridLayout, cursor);
                }
            } else {
                Log.d("Employee Details", "No employees found in the database.");
                listView.setVisibility(View.GONE);
                gridLayout.setVisibility(View.GONE);
                listView.setAdapter(null);
                gridLayout.removeAllViews();
                noEmployeesText.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("Employee Details", "Error accessing database: " + e.getMessage());
            Toast.makeText(this, R.string.not_found + e.getMessage(), Toast.LENGTH_LONG).show();
            listView.setVisibility(View.GONE);
            gridLayout.setVisibility(View.GONE);
            noEmployeesText.setVisibility(View.VISIBLE);
        }
    }

    private void filterEmployeeList(String query) {
        Cursor cursor = null;
        EmployeeDAO employeeDAO = new EmployeeDAO(this);
        try {
            if (selectedDeptId == -1) {
                cursor = employeeDAO.getAllEmployeesFiltered(query);
            } else {
                cursor = employeeDAO.getEmployeesByDepartmentFiltered(selectedDeptId, query);
            }
            if (cursor == null) {
                Log.e("MainActivity", "Cursor is null in filterEmployeeList for query: " + query);
                listView.setAdapter(null);
                gridLayout.removeAllViews();
                noEmployeesText.setVisibility(View.VISIBLE);
                return;
            }
            listAdapter.changeCursor(cursor);
            updateGridLayout(gridLayout, cursor);
            noEmployeesText.setVisibility(cursor.moveToFirst() ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            Log.e("MainActivity", "Error filtering employees: " + e.getMessage());
            listView.setAdapter(null);
            gridLayout.removeAllViews();
            noEmployeesText.setVisibility(View.VISIBLE);
        }
    }

    private void showEmployeeDetails(long employeeId) {
        Intent intent = new Intent(MainActivity.this, EmployeeDetails.class);
        intent.putExtra("employeeId", employeeId);
        startActivity(intent);
    }

    public void goToDepartments(View view) {
        Intent intent = new Intent(MainActivity.this, DepartmentActivity.class);
        startActivity(intent);
        closeMenu();
    }

    private void refreshMenu() {
        LinearLayout departmentsLayout = menuPanel.findViewById(R.id.menu_departments);
        LinearLayout settingsLayout = menuPanel.findViewById(R.id.menu_settings);
        LinearLayout userLayout = menuPanel.findViewById(R.id.btn_user);
        LinearLayout roleLayout = menuPanel.findViewById(R.id.btn_role);
        LinearLayout logoutLayout = menuPanel.findViewById(R.id.menu_logout);
        LinearLayout permissionLayout = menuPanel.findViewById(R.id.menu_permission);

        if (departmentsLayout != null) {
            TextView departmentsText = departmentsLayout.findViewById(R.id.text_department);
            if (departmentsText != null) {
                departmentsText.setText(getString(R.string.menu_departments));
            }
        }
        if (settingsLayout != null) {
            TextView settingsText = settingsLayout.findViewById(R.id.text_settings);
            if (settingsText != null) {
                settingsText.setText(getString(R.string.menu_settings));
            }
        }
        if (userLayout != null) {
            TextView usersText = userLayout.findViewById(R.id.text_username);
            if (usersText != null) {
                usersText.setText(getString(R.string.menu_user));
            }
        }
        if (roleLayout != null) {
            TextView roleText = roleLayout.findViewById(R.id.text_role);
            if (roleText != null) {
                roleText.setText(getString(R.string.menu_role));
            }
        }
        if (logoutLayout != null) {
            TextView logoutText = logoutLayout.findViewById(R.id.text_logout);
            if (logoutText != null) {
                logoutText.setText(getString(R.string.menu_logout));
            }
        }
        if (permissionLayout != null) {
            TextView permissionText = permissionLayout.findViewById(R.id.text_permission);
            if (permissionText != null) {
                permissionText.setText(getString(R.string.menu_permission));
            }
        }
    }

    private void openMenu() {
        menuPanel.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().alpha(0.5f).setDuration(200).start();
        menuPanel.setTranslationX(-menuPanel.getWidth());
        menuPanel.animate().translationX(0).setDuration(300).start();
        isMenuOpen = true;
        refreshMenu();
    }

    private void closeMenu() {
        overlay.animate().alpha(0f).setDuration(200).withEndAction(() -> overlay.setVisibility(View.GONE)).start();
        menuPanel.animate().translationX(-menuPanel.getWidth()).setDuration(300).withEndAction(() -> menuPanel.setVisibility(View.GONE)).start();
        isMenuOpen = false;
    }

    public void GoToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        closeMenu();
    }

    public void GoToUsers(View view) {
        Intent intent = new Intent(MainActivity.this, UserActivity.class);
        startActivity(intent);
        closeMenu();
    }

    public void GoToRole(View view) {
        Intent intent = new Intent(MainActivity.this, RoleActivity.class);
        startActivity(intent);
        closeMenu();
    }

    public void GoToPermission(View view) {
        Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
        startActivity(intent);
        closeMenu();
    }

    public void GoToLogout(View view) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout_confirm_title))
                .setMessage(getString(R.string.logout_confirm_message))
                .setPositiveButton(getString(R.string.logout_confirm_yes), (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.logout_confirm_no), null)
                .show();
    }

    private void applyLanguage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedLanguage = preferences.getString("selected_language", "");
        Log.d("MainActivity", "Applying language: " + selectedLanguage);
        Locale newLocale;
        if ("Tiếng Việt".equals(selectedLanguage)) {
            newLocale = new Locale("vi");
        } else {
            newLocale = Locale.ENGLISH;
        }
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void updateUIText() {
        // Cập nhật ô tìm kiếm
        searchInput.setHint(getString(R.string.search_hint));

        // Cập nhật tiêu đề
        updateHeaderTitle();

        // Cập nhật văn bản "no employees"
        noEmployeesText.setText(getString(R.string.no_employees_found));

        // Cập nhật menu
        refreshMenu();

        // Cập nhật dialog chọn phòng ban (nếu đang mở)
        if (selectedDeptId != -1) {
            showDepartmentListDialog();
        }
    }
}