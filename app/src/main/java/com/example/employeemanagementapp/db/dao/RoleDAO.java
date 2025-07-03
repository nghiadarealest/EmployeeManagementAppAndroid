package com.example.employeemanagementapp.db.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.model.Role;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public RoleDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public List<String> getUserRoles(int userId) {
        List<String> roles = new ArrayList<>();

        String query = "SELECT r." + Constants.COLUMN_ROLE_NAME + " " +
                "FROM " + Constants.TABLE_USERS + " u " +
                "JOIN " + Constants.TABLE_ROLES + " r " +
                "ON u." + Constants.COLUMN_USER_ROLE_ID + " = r." + Constants.COLUMN_ROLE_ID + " " +
                "WHERE u." + Constants.COLUMN_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            int nameIndex = cursor.getColumnIndex(Constants.COLUMN_ROLE_NAME);
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                do {
                    String roleName = cursor.getString(nameIndex);
                    if (roleName != null) {
                        roles.add(roleName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi lấy roles: " + e.getMessage());
        }

        return roles;
    }

    public List<Long> getRoleIdsByNames(List<String> roleNames) {
        List<Long> roleIds = new ArrayList<>();
        if (roleNames == null || roleNames.isEmpty()) {
            return roleIds;
        }

        String query = "SELECT " + Constants.COLUMN_ROLE_ID + " " +
                "FROM " + Constants.TABLE_ROLES + " " +
                "WHERE " + Constants.COLUMN_ROLE_NAME + " = ?";

        for (String name : roleNames) {
            try (Cursor cursor = db.rawQuery(query, new String[]{name})) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_ROLE_ID));
                    roleIds.add(id);
                }
            } catch (Exception e) {
                android.util.Log.e("RoleDAO", "Lỗi khi lấy role ID cho " + name + ": " + e.getMessage());
            }
        }

        return roleIds;
    }

    public boolean hasRole(int userId, String roleName) {
        String query = "SELECT 1 FROM " + Constants.TABLE_USERS + " u " +
                "INNER JOIN " + Constants.TABLE_ROLES + " r " +
                "ON u." + Constants.COLUMN_USER_ROLE_ID + " = r." + Constants.COLUMN_ROLE_ID + " " +
                "WHERE u." + Constants.COLUMN_USER_ID + " = ? AND r." + Constants.COLUMN_ROLE_NAME + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), roleName})) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi kiểm tra role " + roleName + ": " + e.getMessage());
            return false;
        }
    }
    public List<Role> getAllRoles() {
        List<Role> roleList = new ArrayList<>();
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + Constants.COLUMN_ROLE_ID + ", " +
                        Constants.COLUMN_ROLE_NAME +
                        " FROM " + Constants.TABLE_ROLES,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                roleList.add(new Role(id, name));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return roleList;
    }
    public Cursor getAllRolesCursor() {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + Constants.COLUMN_ROLE_ID + " AS _id, " +  // BẮT BUỘC phải có _id cho SimpleCursorAdapter
                        Constants.COLUMN_ROLE_NAME +
                        " FROM " + Constants.TABLE_ROLES,
                null
        );
    }
    public Role getRoleById(int roleId) {
        Role role = null;

        String query = "SELECT " + Constants.COLUMN_ROLE_ID + ", " + Constants.COLUMN_ROLE_NAME +
                " FROM " + Constants.TABLE_ROLES +
                " WHERE " + Constants.COLUMN_ROLE_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(roleId)})) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(Constants.COLUMN_ROLE_ID);
                int nameIndex = cursor.getColumnIndex(Constants.COLUMN_ROLE_NAME);

                if (idIndex >= 0 && nameIndex >= 0) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    role = new Role(id, name);
                } else {
                    android.util.Log.e("RoleDAO", "Cột trong cursor không tồn tại");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi lấy Role theo ID: " + e.getMessage());
        }

        return role;
    }

    public Cursor getRoleByIdCursor(long roleId) {
        String query = "SELECT " + Constants.COLUMN_ROLE_ID + ", " + Constants.COLUMN_ROLE_NAME +
                " FROM " + Constants.TABLE_ROLES +
                " WHERE " + Constants.COLUMN_ROLE_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(roleId)});
    }
    public long insertRole(String roleName) {
        long result = -1;

        String query = "INSERT INTO " + Constants.TABLE_ROLES + " (" + Constants.COLUMN_ROLE_NAME + ") VALUES (?)";

        try {
            db.beginTransaction();
            db.execSQL(query, new Object[]{roleName});
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            if (cursor.moveToFirst()) {
                result = cursor.getLong(0);  // Trả về ID của role vừa tạo
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi tạo role: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return result;
    }

    public void insertRolePermissions(long roleId, List<Long> permissionIds) {
        String insertQuery = "INSERT INTO " + Constants.TABLE_ROLE_PERMISSIONS +
                " (" + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + ", " +
                Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + ") VALUES (?, ?)";

        try {
            db.beginTransaction();
            for (Long permissionId : permissionIds) {
                db.execSQL(insertQuery, new Object[]{roleId, permissionId});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi thêm role_permissions: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public int deleteRole(long roleId) {
        int rowsDeleted = 0;

        try {
            db.beginTransaction();

            // Xóa các quyền liên kết với role trong bảng role_permissions trước
            db.delete(
                    Constants.TABLE_ROLE_PERMISSIONS,
                    Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " = ?",
                    new String[]{String.valueOf(roleId)}
            );

            // Sau đó xóa role chính trong bảng roles
            rowsDeleted = db.delete(
                    Constants.TABLE_ROLES,
                    Constants.COLUMN_ROLE_ID + " = ?",
                    new String[]{String.valueOf(roleId)}
            );

            db.setTransactionSuccessful();
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi khi xóa role: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return rowsDeleted;
    }

    public String getRoleNameById(long roleId) {
        String roleName = null;
        String query = "SELECT " + Constants.COLUMN_ROLE_NAME + " FROM " + Constants.TABLE_ROLES +
                " WHERE " + Constants.COLUMN_ROLE_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(roleId)})) {
            if (cursor.moveToFirst()) {
                roleName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ROLE_NAME));
            }
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi getRoleNameById: " + e.getMessage());
        }

        return roleName;
    }
    public boolean updateRoleName(long roleId, String newName) {
        boolean success = false;

        String query = "UPDATE " + Constants.TABLE_ROLES +
                " SET " + Constants.COLUMN_ROLE_NAME + " = ? WHERE " + Constants.COLUMN_ROLE_ID + " = ?";

        try {
            db.beginTransaction();
            db.execSQL(query, new Object[]{newName, roleId});
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi updateRoleName: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return success;
    }
    public void updateRolePermissions(long roleId, List<Long> newPermissionIds) {
        String deleteQuery = "DELETE FROM " + Constants.TABLE_ROLE_PERMISSIONS +
                " WHERE " + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " = ?";
        String insertQuery = "INSERT INTO " + Constants.TABLE_ROLE_PERMISSIONS +
                " (" + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + ", " +
                Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + ") VALUES (?, ?)";

        try {
            db.beginTransaction();

            // Xoá quyền cũ
            db.execSQL(deleteQuery, new Object[]{roleId});

            // Thêm quyền mới
            for (Long permissionId : newPermissionIds) {
                db.execSQL(insertQuery, new Object[]{roleId, permissionId});
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            android.util.Log.e("RoleDAO", "Lỗi updateRolePermissions: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }
    public String getRoleNameByIdLong(long roleId) {
        String roleName = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    Constants.TABLE_ROLES,
                    new String[]{Constants.COLUMN_ROLE_NAME},
                    Constants.COLUMN_ROLE_ID + " = ?",
                    new String[]{String.valueOf(roleId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                roleName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ROLE_NAME));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return roleName;
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}