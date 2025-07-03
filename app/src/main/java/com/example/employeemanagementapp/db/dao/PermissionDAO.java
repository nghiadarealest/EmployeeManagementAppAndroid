package com.example.employeemanagementapp.db.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public PermissionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public List<String> getUserPermissions(int userId) {
        Set<String> permissions = new HashSet<>();

        // Truy vấn hợp nhất để lấy quyền từ role_permissions và user_permissions
        String query = "SELECT DISTINCT p." + Constants.COLUMN_PERMISSION_NAME + " " +
                "FROM " + Constants.TABLE_PERMISSIONS + " p " +
                "JOIN " + Constants.TABLE_ROLE_PERMISSIONS + " rp " +
                "ON p." + Constants.COLUMN_PERMISSION_ID + " = rp." + Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + " " +
                "JOIN " + Constants.TABLE_USERS + " u " +
                "ON rp." + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " = u." + Constants.COLUMN_USER_ROLE_ID + " " +
                "WHERE u." + Constants.COLUMN_USER_ID + " = ?";


        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            int nameIndex = cursor.getColumnIndex(Constants.COLUMN_PERMISSION_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                do {
                    String permissionName = cursor.getString(nameIndex);
                    if (permissionName != null) {
                        permissions.add(permissionName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi lấy permissions: " + e.getMessage());
        }

        return new ArrayList<>(permissions);
    }

    public List<Long> getPermissionIdsByNames(List<String> permissionNames) {
        List<Long> permissionIds = new ArrayList<>();
        if (permissionNames == null || permissionNames.isEmpty()) {
            return permissionIds;
        }

        String query = "SELECT " + Constants.COLUMN_PERMISSION_ID + " " +
                "FROM " + Constants.TABLE_PERMISSIONS + " " +
                "WHERE " + Constants.COLUMN_PERMISSION_NAME + " = ?";

        for (String name : permissionNames) {
            try (Cursor cursor = db.rawQuery(query, new String[]{name})) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_PERMISSION_ID));
                    permissionIds.add(id);
                }
            } catch (Exception e) {
                android.util.Log.e("PermissionDAO", "Lỗi khi lấy permission ID cho " + name + ": " + e.getMessage());
            }
        }

        return permissionIds;
    }

    public Cursor getAllPermissionsCursor() {
        String query = "SELECT " + Constants.COLUMN_PERMISSION_ID + ", " + Constants.COLUMN_PERMISSION_NAME +
                " FROM " + Constants.TABLE_PERMISSIONS;
        return db.rawQuery(query, null);
    }

    public long insertPermission(String permissionName) {
        long newRowId = -1;

        if (permissionName == null || permissionName.trim().isEmpty()) {
            android.util.Log.e("PermissionDAO", "Tên permission không được để trống");
            return newRowId;
        }

        // Kiểm tra xem permission đã tồn tại chưa
        String checkQuery = "SELECT " + Constants.COLUMN_PERMISSION_ID +
                " FROM " + Constants.TABLE_PERMISSIONS +
                " WHERE " + Constants.COLUMN_PERMISSION_NAME + " = ?";
        try (Cursor cursor = db.rawQuery(checkQuery, new String[]{permissionName})) {
            if (cursor.moveToFirst()) {
                android.util.Log.i("PermissionDAO", "Permission đã tồn tại: " + permissionName);
                return -1; // đã tồn tại
            }
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi kiểm tra permission: " + e.getMessage());
            return -1;
        }

        // Nếu chưa tồn tại thì thêm mới
        String insertQuery = "INSERT INTO " + Constants.TABLE_PERMISSIONS + " (" +
                Constants.COLUMN_PERMISSION_NAME + ") VALUES (?)";
        try {
            db.execSQL(insertQuery, new Object[]{permissionName});
            // Lấy ID vừa insert
            String lastIdQuery = "SELECT last_insert_rowid()";
            try (Cursor cursor = db.rawQuery(lastIdQuery, null)) {
                if (cursor.moveToFirst()) {
                    newRowId = cursor.getLong(0);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi thêm permission: " + e.getMessage());
        }

        return newRowId;
    }

    public Cursor getPermissionCursorById(long permissionId) {
        String query = "SELECT " + Constants.COLUMN_PERMISSION_ID + ", " + Constants.COLUMN_PERMISSION_NAME +
                " FROM " + Constants.TABLE_PERMISSIONS +
                " WHERE " + Constants.COLUMN_PERMISSION_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(permissionId)});
    }

    public int updatePermission(long permissionId, String newPermissionName) {
        if (newPermissionName == null || newPermissionName.trim().isEmpty()) {
            android.util.Log.e("PermissionDAO", "Tên permission không được để trống");
            return 0;
        }

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(Constants.COLUMN_PERMISSION_NAME, newPermissionName);

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(
                    Constants.TABLE_PERMISSIONS,
                    values,
                    Constants.COLUMN_PERMISSION_ID + " = ?",
                    new String[]{String.valueOf(permissionId)}
            );
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi cập nhật permission: " + e.getMessage());
        }

        return rowsAffected;
    }

    public int deletePermission(long permissionId) {
        int rowsDeleted = 0;
        try {
            rowsDeleted = db.delete(
                    Constants.TABLE_PERMISSIONS,
                    Constants.COLUMN_PERMISSION_ID + " = ?",
                    new String[]{String.valueOf(permissionId)}
            );
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi xóa permission: " + e.getMessage());
        }

        return rowsDeleted;
    }

    public List<String> getPermissionsByRoleId(long roleId) {
        List<String> permissions = new ArrayList<>();
        String query = "SELECT p." + Constants.COLUMN_PERMISSION_NAME +
                " FROM " + Constants.TABLE_PERMISSIONS + " p " +
                "JOIN " + Constants.TABLE_ROLE_PERMISSIONS + " rp " +
                "ON p." + Constants.COLUMN_PERMISSION_ID + " = rp." + Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + " " +
                "WHERE rp." + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(roleId)})) {
            int nameIndex = cursor.getColumnIndex(Constants.COLUMN_PERMISSION_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                do {
                    permissions.add(cursor.getString(nameIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PermissionDAO", "Lỗi khi lấy quyền theo role ID: " + e.getMessage());
        }
        return permissions;
    }
    public List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>();

        String query = "SELECT " + Constants.COLUMN_PERMISSION_NAME +
                " FROM " + Constants.TABLE_PERMISSIONS;

        try (Cursor cursor = db.rawQuery(query, null)) {
            int nameIndex = cursor.getColumnIndex(Constants.COLUMN_PERMISSION_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                do {
                    String permissionName = cursor.getString(nameIndex);
                    if (permissionName != null) {
                        permissions.add(permissionName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi lấy danh sách permissions: " + e.getMessage());
        }

        return permissions;
    }

    public List<String> getPermissionNamesByRoleId(long roleId) {
        List<String> permissionNames = new ArrayList<>();

        String query = "SELECT p." + Constants.COLUMN_PERMISSION_NAME +
                " FROM " + Constants.TABLE_PERMISSIONS + " p " +
                "JOIN " + Constants.TABLE_ROLE_PERMISSIONS + " rp " +
                "ON p." + Constants.COLUMN_PERMISSION_ID + " = rp." + Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + " " +
                "WHERE rp." + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(roleId)})) {
            int nameIndex = cursor.getColumnIndex(Constants.COLUMN_PERMISSION_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                do {
                    String permissionName = cursor.getString(nameIndex);
                    if (permissionName != null) {
                        permissionNames.add(permissionName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PermissionDAO", "Lỗi khi lấy tên permission theo roleId: " + e.getMessage());
        }

        return permissionNames;
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        dbHelper.close();
    }
}