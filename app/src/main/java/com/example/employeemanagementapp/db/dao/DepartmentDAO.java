package com.example.employeemanagementapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.utils.Constants;

public class DepartmentDAO {
    private final SQLiteDatabase db;

    public DepartmentDAO(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public long insertDepartment(String name, String positions) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DEPT_NAME, name);
        values.put(Constants.COLUMN_DEPT_POSITIONS, positions);
        long result = db.insert(Constants.TABLE_DEPARTMENTS, null, values);
        db.close();
        return result;
    }

    public Cursor getAllDepartments() {
        return db.rawQuery("SELECT * FROM " + Constants.TABLE_DEPARTMENTS, null);
    }

    public Cursor getDepartmentById(long id) {
        return db.query(Constants.TABLE_DEPARTMENTS, null, Constants.COLUMN_DEPT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public int updateDepartment(long id, String name, String positions) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DEPT_NAME, name);
        values.put(Constants.COLUMN_DEPT_POSITIONS, positions);
        int rowsAffected = db.update(Constants.TABLE_DEPARTMENTS, values, Constants.COLUMN_DEPT_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public int deleteDepartment(Context context, long id) {
        EmployeeDAO employeeDAO = new EmployeeDAO(context);
        if (employeeDAO.hasEmployeesInDepartment(id)) {
            return -1;
        }
        int rowsDeleted = db.delete(Constants.TABLE_DEPARTMENTS, Constants.COLUMN_DEPT_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public Cursor searchDepartments(String query) {
        String selection = Constants.COLUMN_DEPT_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        return db.query(Constants.TABLE_DEPARTMENTS, null, selection, selectionArgs, null, null, null);
    }

}
