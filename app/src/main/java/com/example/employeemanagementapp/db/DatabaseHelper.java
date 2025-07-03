package com.example.employeemanagementapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.employeemanagementapp.utils.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = Constants.DATABASE_NAME;
    private static final int DATABASE_VERSION = Constants.DATABASE_VERSION;

    // Tạo bảng departments
    private static final String TABLE_CREATE_DEPARTMENTS =
            "CREATE TABLE " + Constants.TABLE_DEPARTMENTS + " (" +
                    Constants.COLUMN_DEPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_DEPT_NAME + " TEXT NOT NULL, " +
                    Constants.COLUMN_DEPT_POSITIONS + " TEXT)";

    private static final String TABLE_CREATE_USERS =
    "CREATE TABLE " + Constants.TABLE_USERS + " (" +
        Constants.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        Constants.COLUMN_USER_NAME + " TEXT UNIQUE, " +
        Constants.COLUMN_USER_PASSWORD + " TEXT, " +
        Constants.COLUMN_USER_ROLE_ID + " INTEGER, " +
        "FOREIGN KEY(" + Constants.COLUMN_USER_ROLE_ID + ") REFERENCES " + Constants.TABLE_ROLES + "(" + Constants.COLUMN_ROLE_ID + "))";

    private static final String TABLE_CREATE_ROLES =
            "CREATE TABLE " + Constants.TABLE_ROLES + " (" +
                    Constants.COLUMN_ROLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_ROLE_NAME + " TEXT NOT NULL)";

    private static final String TABLE_CREATE_PERMISSIONS =
            "CREATE TABLE " + Constants.TABLE_PERMISSIONS + " (" +
                    Constants.COLUMN_PERMISSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_PERMISSION_NAME + " TEXT NOT NULL)";

    private static final String TABLE_CREATE_ROLE_PERMISSIONS =
            "CREATE TABLE " + Constants.TABLE_ROLE_PERMISSIONS + " (" +
                    Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " INTEGER NOT NULL, " +
                    Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + ") REFERENCES " + Constants.TABLE_ROLES + "(" + Constants.COLUMN_ROLE_ID + "), " +
                    "FOREIGN KEY(" + Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + ") REFERENCES " + Constants.TABLE_PERMISSIONS + "(" + Constants.COLUMN_PERMISSION_ID + "), " +
                    "PRIMARY KEY(" + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + ", " + Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + "))";

    
    private static final String TABLE_CREATE_EMPLOYEES =
            "CREATE TABLE " + Constants.TABLE_EMPLOYEE + " (" +
                    Constants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                    Constants.COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                    Constants.COLUMN_IMAGE + " BLOB, " +
                    Constants.COLUMN_PHONE_NUMBER + " TEXT, " +
                    Constants.COLUMN_EMAIL + " TEXT, " +
                    Constants.COLUMN_RESIDENCE + " TEXT, " +
                    Constants.COLUMN_POSITION + " TEXT, " +
                    Constants.COLUMN_DEPARTMENT_ID + " INTEGER, " +
                    Constants.COLUMN_GENDER + " TEXT, " + // Thêm cột giới tính
                    Constants.COLUMN_STATUS + " TEXT, " +
                    Constants.COLUMN_HIRE_DATE + " TEXT, " + // Thêm cột ngày vào làm
                    Constants.COLUMN_SALARY + " REAL, " + // Thêm cột mức lương
                    "FOREIGN KEY(" + Constants.COLUMN_DEPARTMENT_ID + ") REFERENCES " + Constants.TABLE_DEPARTMENTS + "(" + Constants.COLUMN_DEPT_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_DEPARTMENTS); // Tạo bảng departments trước
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_ROLES);
        db.execSQL(TABLE_CREATE_PERMISSIONS);
        db.execSQL(TABLE_CREATE_ROLE_PERMISSIONS);
        db.execSQL(TABLE_CREATE_EMPLOYEES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_EMPLOYEE);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_DEPARTMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_ROLE_PERMISSIONS);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_ROLES);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PERMISSIONS);

            onCreate(db); // Tạo lại tất cả bảng
        }
    }
}
