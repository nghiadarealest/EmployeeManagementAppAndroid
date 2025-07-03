package com.example.employeemanagementapp.utils;

public class Constants {

    // Tên cơ sở dữ liệu và phiên bản
    public static final String DATABASE_NAME = "employee_management.db";
    public static final int DATABASE_VERSION = 9; // Cập nhật để khớp với onUpgrade

    // Tên các bảng
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ROLES = "roles";
    public static final String TABLE_PERMISSIONS = "permissions";
    public static final String TABLE_USER_ROLES = "user_roles";
    public static final String TABLE_ROLE_PERMISSIONS = "role_permissions";
    public static final String TABLE_USER_PERMISSIONS = "user_permissions";
    public static final String TABLE_EMPLOYEE = "employees"; // Sử dụng TABLE_EMPLOYEE, xóa TABLE_EMPLOYEES
    public static final String TABLE_DEPARTMENTS = "departments";

    // role
    public static final String VIEW_ROLE = "VIEW_ROLE";
    public static final String VIEW_USER = "VIEW_USER";
    public static final String VIEW_PERMISSION = "VIEW_PERMISSION";
    public static final String VIEW_DEPARTMENT = "VIEW_DEPARTMENT";
    public static final String VIEW_EMPLOYEE = "VIEW_EMPLOYEE";
    public static final String ADD_EMPLOYEE = "ADD_EMPLOYEE";
    public static final String EDIT_EMPLOYEE = "EDIT_EMPLOYEE";
    public static final String DELETE_EMPLOYEE = "DELETE_EMPLOYEE";

    // Cột trong bảng users
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE_ID = "role_id";

    // Cột trong bảng roles
    public static final String COLUMN_ROLE_ID = "_id";
    public static final String COLUMN_ROLE_NAME = "role_name";

    // Cột trong bảng permissions
    public static final String COLUMN_PERMISSION_ID = "_id";
    public static final String COLUMN_PERMISSION_NAME = "permission_name";

    // Cột trong bảng role_permissions
    public static final String COLUMN_ROLE_PERMISSION_ROLE_ID = "role_id";
    public static final String COLUMN_ROLE_PERMISSION_PERMISSION_ID = "permission_id";


    // Cột trong bảng employees
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_RESIDENCE = "residence";
    public static final String COLUMN_POSITION = "employee_position";
    public static final String COLUMN_DEPARTMENT_ID = "department_id";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_HIRE_DATE = "hire_date";
    public static final String COLUMN_SALARY = "salary";

    // Cột trong bảng departments
    public static final String COLUMN_DEPT_ID = "_id";
    public static final String COLUMN_DEPT_NAME = "department_name";
    public static final String COLUMN_DEPT_POSITIONS = "department_positions";
}