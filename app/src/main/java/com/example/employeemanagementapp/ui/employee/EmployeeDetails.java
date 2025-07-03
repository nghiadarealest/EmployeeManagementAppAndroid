package com.example.employeemanagementapp.ui.employee;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.employeemanagementapp.MainActivity;
import com.example.employeemanagementapp.R;
import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.dao.DepartmentDAO;
import com.example.employeemanagementapp.db.dao.EmployeeDAO;
import com.example.employeemanagementapp.db.dao.UserDAO;
import com.example.employeemanagementapp.db.model.Employee;
import com.example.employeemanagementapp.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmployeeDetails extends AppCompatActivity {

    private EmployeeDAO employeeDAO;
    private DepartmentDAO departmentDAO;
    private boolean isEditMode = false;
    private ImageView profileImageView;
    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextEmail, editTextResidence;
    private EditText editTextHireDate, editTextSalary;
    private Spinner spinnerDepartment, spinnerPosition, spinnerGender, spinnerStatus;
    private List<Department> departments;
    private long selectedDepartmentId;
    private ImageView iconUpdate, iconDelete;
    private Button btnUpdate;

    private UserDAO userDAO;

    private static class Department {
        long id;
        String name;
        String[] positions;

        Department(long id, String name, String positions) {
            this.id = id;
            this.name = name;
            this.positions = positions != null ? positions.split(",\\s*") : new String[]{};
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLanguage();
        setContentView(R.layout.activity_employee_details);

        employeeDAO = new EmployeeDAO(this);
        departmentDAO = new DepartmentDAO(this);

        userDAO = new UserDAO(this);

        editTextFirstName = findViewById(R.id.edittext_first_name);
        editTextLastName = findViewById(R.id.edittext_last_name);
        editTextPhoneNumber = findViewById(R.id.edittext_phone_number);
        editTextEmail = findViewById(R.id.edittext_email);
        editTextResidence = findViewById(R.id.edittext_residence);
        editTextHireDate = findViewById(R.id.edittext_hire_date);
        editTextSalary = findViewById(R.id.edittext_salary);
        spinnerDepartment = findViewById(R.id.spinner_department);
        spinnerPosition = findViewById(R.id.spinner_position);
        spinnerGender = findViewById(R.id.spinner_gender);
        spinnerStatus = findViewById(R.id.spinner_status);
        profileImageView = findViewById(R.id.image_profile2);

        iconUpdate = findViewById(R.id.modiff);
        iconDelete = findViewById(R.id.supp);
        btnUpdate = findViewById(R.id.btn_update);

        // Thiết lập Spinner giới tính
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.employment_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Thiết lập DatePicker cho ngày vào làm
        editTextHireDate.setOnClickListener(v -> {
            if (isEditMode) {
                showDatePickerDialog();
            }
        });

        long employeeId = getIntent().getLongExtra("employeeId", -1);
        loadDepartments();

        if (employeeId != -1) {
            Cursor cursor = employeeDAO.getEmployeeById(employeeId);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String firstName = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_FIRST_NAME));
                @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_LAST_NAME));
                @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PHONE_NUMBER));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_EMAIL));
                @SuppressLint("Range") String residence = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_RESIDENCE));
                @SuppressLint("Range") long departmentId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DEPARTMENT_ID));
                @SuppressLint("Range") String position = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_POSITION));
                @SuppressLint("Range") String gender = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_GENDER));
                @SuppressLint("Range") String status = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_STATUS));
                @SuppressLint("Range") String hireDate = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_HIRE_DATE));
                @SuppressLint("Range") double salary = cursor.getDouble(cursor.getColumnIndex(Constants.COLUMN_SALARY));

                editTextFirstName.setText(firstName);
                editTextLastName.setText(lastName);
                editTextPhoneNumber.setText(phoneNumber);
                editTextEmail.setText(email);
                editTextResidence.setText(residence);
                editTextHireDate.setText(hireDate);
                editTextSalary.setText(String.valueOf(salary));

                setEditTextReadonly(editTextFirstName);
                setEditTextReadonly(editTextLastName);
                setEditTextReadonly(editTextPhoneNumber);
                setEditTextReadonly(editTextEmail);
                setEditTextReadonly(editTextResidence);
                setEditTextReadonly(editTextHireDate);
                setEditTextReadonly(editTextSalary);
                spinnerDepartment.setEnabled(false);
                spinnerPosition.setEnabled(false);
                spinnerGender.setEnabled(false);
                spinnerStatus.setEnabled(false);

                // Set department spinner
                for (int i = 0; i < departments.size(); i++) {
                    if (departments.get(i).id == departmentId) {
                        spinnerDepartment.setSelection(i);
                        selectedDepartmentId = departmentId;
                        updatePositionSpinner(departments.get(i).positions);
                        // Set position spinner
                        for (int j = 0; j < departments.get(i).positions.length; j++) {
                            if (departments.get(i).positions[j].equals(position)) {
                                spinnerPosition.setSelection(j);
                                break;
                            }
                        }
                        break;
                    }
                }

                // Set gender spinner
                if (gender != null) {
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerGender.getAdapter();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).toString().equals(gender)) {
                            spinnerGender.setSelection(i);
                            break;
                        }
                    }
                }

                if (status != null) {
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerStatus.getAdapter();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).toString().equals(status)) {
                            spinnerStatus.setSelection(i);
                            break;
                        }
                    }
                }

                profileImageView.setImageResource(R.drawable.ic_launcher_background);
                byte[] imageData = employeeDAO.getEmployeeProfileImage(employeeId);
                if (imageData != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    profileImageView.setImageBitmap(bitmap);
                } else {
                    profileImageView.setImageResource(R.drawable.rounded_button_background);
                }

                cursor.close();
            } else {
                Log.d("Employee Details", "Không tìm thấy nhân viên với ID: " + employeeId);
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Log.d("Employee Details", "ID nhân viên không hợp lệ");
        }

        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartmentId = departments.get(position).id;
                updatePositionSpinner(departments.get(position).positions);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDepartmentId = -1;
                updatePositionSpinner(new String[]{});
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("authUserId", -1);
        if (userId != -1) {
            if (!userDAO.userHasPermission(userId, Constants.EDIT_EMPLOYEE)) {
                iconUpdate.setVisibility(View.GONE);
                btnUpdate.setVisibility(View.GONE);
            }

            if (!userDAO.userHasPermission(userId, Constants.DELETE_EMPLOYEE)) {
                iconDelete.setVisibility(View.GONE);
            }
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editTextHireDate.setText(formattedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadDepartments() {
        departments = new ArrayList<>();
        Cursor cursor = departmentDAO.getAllDepartments();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DEPT_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEPT_NAME));
                @SuppressLint("Range") String positions = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DEPT_POSITIONS));
                departments.add(new Department(id, name, positions));
            } while (cursor.moveToNext());
            cursor.close();
        }

        List<String> departmentNames = new ArrayList<>();
        for (Department dept : departments) {
            departmentNames.add(dept.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);

        if (!departments.isEmpty()) {
            selectedDepartmentId = departments.get(0).id;
            updatePositionSpinner(departments.get(0).positions);
        } else {
            spinnerPosition.setEnabled(false);
        }
    }

    private void updatePositionSpinner(String[] positions) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);
        spinnerPosition.setEnabled(isEditMode && positions.length > 0);
    }

    private void setEditTextReadonly(EditText editText) {
        editText.setFocusable(false);
        editText.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    public void toggleEditMode(View view) {
        isEditMode = !isEditMode;

        if (isEditMode) {
            setEditTextEditable(editTextFirstName);
            setEditTextEditable(editTextLastName);
            setEditTextEditable(editTextPhoneNumber);
            setEditTextEditable(editTextEmail);
            setEditTextEditable(editTextResidence);
            setEditTextEditable(editTextHireDate);
            setEditTextEditable(editTextSalary);
            spinnerDepartment.setEnabled(true);
            spinnerPosition.setEnabled(spinnerPosition.getAdapter().getCount() > 0);
            spinnerGender.setEnabled(true);
            spinnerStatus.setEnabled(true);
        } else {
            setEditTextReadonly(editTextFirstName);
            setEditTextReadonly(editTextLastName);
            setEditTextReadonly(editTextPhoneNumber);
            setEditTextReadonly(editTextEmail);
            setEditTextReadonly(editTextResidence);
            setEditTextReadonly(editTextHireDate);
            setEditTextReadonly(editTextSalary);
            spinnerDepartment.setEnabled(false);
            spinnerPosition.setEnabled(false);
            spinnerGender.setEnabled(false);
            spinnerStatus.setEnabled(false);
        }
    }

    private void setEditTextEditable(EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    public byte[] convertImageToByteArray() {
        try {
            Bitmap bitmap = null;
            if (profileImageView.getDrawable() instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
            }
            if (bitmap == null) {
                throw new IllegalStateException("Drawable is not a BitmapDrawable or is null");
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (Exception e) {
            Log.e("EmployeeDetails", "Error converting image to byte array: " + e.getMessage());
            return null;
        }
    }

    public void openCamera2(View view) {
        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, 0);
    }

    public void openGallery2(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Bitmap bmp = (Bitmap) bundle.get("data");
                        if (bmp != null) {
                            Bitmap resized = Bitmap.createScaledBitmap(bmp, 100, 100, true);
                            profileImageView.setImageBitmap(resized);
                        }
                    }
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        profileImageView.setImageURI(selectedImage);
                    }
                }
                break;
        }
    }

    public void goBack(View view) {
        finish();
    }

    public void SendMail(View view) {
        String recipientEmail = editTextEmail.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + Uri.encode(recipientEmail)));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No email app found. Please install an email app.", Toast.LENGTH_SHORT).show();
        }
    }

    public void MakeCall(View view) {
        String recipientPhone = editTextPhoneNumber.getText().toString();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + recipientPhone));
        startActivity(intent);
    }

    public void MakeSMS(View view) {
        String recipientPhone = editTextPhoneNumber.getText().toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + recipientPhone));
        startActivity(intent);
    }

    public void deleteEmployee(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_confirm_title))
                .setMessage(getString(R.string.delete_confirm_message))
                .setPositiveButton(getString(R.string.delete_positive), (dialog, which) -> {
                    long employeeId = getIntent().getLongExtra("employeeId", -1);
                    if (employeeId != -1) {
                        int rowsDeleted = employeeDAO.deleteEmployee(employeeId);
                        if (rowsDeleted > 0) {
                            Toast.makeText(this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Delete Employee", "ID nhân viên không hợp lệ");
                    }
                })
                .setNegativeButton(getString(R.string.delete_negative), (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void updateEmployee(View view) {
        long employeeId = getIntent().getLongExtra("employeeId", -1);
        if (employeeId != -1) {
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();
            String phoneNumber = editTextPhoneNumber.getText().toString();
            String email = editTextEmail.getText().toString();
            String residence = editTextResidence.getText().toString();
            String position = spinnerPosition.getSelectedItem() != null ? spinnerPosition.getSelectedItem().toString() : "";
            String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "";
            String status = spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "";
            String hireDate = editTextHireDate.getText().toString();
            String salaryStr = editTextSalary.getText().toString();

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                    TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(residence) || selectedDepartmentId == -1 ||
                    TextUtils.isEmpty(position) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(status) ||
                    TextUtils.isEmpty(hireDate) || TextUtils.isEmpty(salaryStr)) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phoneNumber.matches("\\d{10}")) {
                Toast.makeText(this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                return;
            }


            // Chuyển đổi mức lương
            double salary;
            try {
                salary = Double.parseDouble(salaryStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.invalid_salary), Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] imageBytes = convertImageToByteArray();
            if (imageBytes == null) {
                Toast.makeText(this, getString(R.string.image_processing_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            Employee employee = new Employee(firstName, lastName, phoneNumber, email,
                    selectedDepartmentId, position, residence, gender, status, hireDate, salary);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                int rowsAffected = employeeDAO.updateEmployee(employeeId, employee, imageBytes);

                runOnUiThread(() -> {
                    if (rowsAffected > 0) {
                        Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
                        finish(); // đóng màn hình sau khi cập nhật thành công
                    } else {
                        Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else {
            Log.d("Update Employee", "ID nhân viên không hợp lệ");
        }
    }


    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}