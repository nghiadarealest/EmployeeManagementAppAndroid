package com.example.employeemanagementapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.model.User;
import com.example.employeemanagementapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private SQLiteDatabase db;
    private final DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // Lấy tất cả người dùng
    public Cursor getAllUsers() {
        return db.rawQuery("SELECT user_id AS _id, username, password FROM " + Constants.TABLE_USERS, null);
    }

    // Tìm kiếm người dùng theo tên
    public Cursor searchUsers(String keyword) {
        String query = "SELECT user_id AS _id, username, password FROM " + Constants.TABLE_USERS + " WHERE " + Constants.COLUMN_USER_NAME + " LIKE ?";
        return db.rawQuery(query, new String[]{"%" + keyword + "%"});
    }

    // Các phương thức khác giữ nguyên...
    public boolean registerUser(String username, String password, int role) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, username);
        values.put(Constants.COLUMN_USER_PASSWORD, password);

        // Chỉ thêm role nếu nó không null và không rỗng
        if (role > 0) {
            values.put(Constants.COLUMN_USER_ROLE_ID, role);
        }

        // Kiểm tra user đã tồn tại chưa
        Cursor cursor = db.query(Constants.TABLE_USERS, null,
                Constants.COLUMN_USER_NAME + " = ?", new String[]{username},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();

        if (exists) {
            return false;
        }

        long result = db.insert(Constants.TABLE_USERS, null, values);
        return result != -1;
    }


    public User getUserById(int userId) {
        Cursor cursor = db.query(
                Constants.TABLE_USERS,
                null,
                Constants.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_NAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_PASSWORD));
            Long roleId = cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_ROLE_ID));
            Log.e("role", String.valueOf(roleId));
            cursor.close();
            return new User(userId, username, password, roleId);
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }

    public boolean checkLogin(String username, String password) {
        Cursor cursor = db.query(Constants.TABLE_USERS,
                new String[]{Constants.COLUMN_USER_ID},
                Constants.COLUMN_USER_NAME + "=? AND " + Constants.COLUMN_USER_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public int checkLoginAndGetUserId(String username, String password) {
        Cursor cursor = db.query(Constants.TABLE_USERS,
                new String[]{Constants.COLUMN_USER_ID},
                Constants.COLUMN_USER_NAME + "=? AND " + Constants.COLUMN_USER_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        int userId = -1; // giá trị mặc định nếu không tìm thấy
        if (cursor.moveToFirst()) {  // di chuyển đến dòng đầu tiên nếu có
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }

    public long addUser(User user) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, user.getUsername());
        values.put(Constants.COLUMN_USER_PASSWORD, user.getPassword());

        long userId = db.insert(Constants.TABLE_USERS, null, values);

        return userId;
    }

    public boolean userHasPermission(int userId, String permissionName) {
        String query = "SELECT 1 " +
                "FROM " + Constants.TABLE_PERMISSIONS + " p " +
                "JOIN " + Constants.TABLE_ROLE_PERMISSIONS + " rp " +
                "ON p." + Constants.COLUMN_PERMISSION_ID + " = rp." + Constants.COLUMN_ROLE_PERMISSION_PERMISSION_ID + " " +
                "JOIN " + Constants.TABLE_USERS + " u " +
                "ON rp." + Constants.COLUMN_ROLE_PERMISSION_ROLE_ID + " = u." + Constants.COLUMN_USER_ROLE_ID + " " +
                "WHERE u." + Constants.COLUMN_USER_ID + " = ? AND p." + Constants.COLUMN_PERMISSION_NAME + " = ? " +
                "LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), permissionName})) {
            return cursor.moveToFirst(); // true nếu có quyền, false nếu không
        } catch (Exception e) {
            android.util.Log.e("PermissionDAO", "Lỗi khi kiểm tra permission: " + e.getMessage());
            return false;
        }
    }

    public int updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, user.getUsername());
        values.put(Constants.COLUMN_USER_PASSWORD, user.getPassword());
        values.put(Constants.COLUMN_USER_ROLE_ID, user.getRoleId()); // <- Thêm dòng này

        return db.update(Constants.TABLE_USERS, values,
                Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user.getId())});
    }

    public int updateUsername(long userId, String newUsername) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_NAME, newUsername);

        return db.update(Constants.TABLE_USERS, values,
                Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public int updatePassword(long userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_PASSWORD, newPassword);

        return db.update(Constants.TABLE_USERS, values,
                Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }


    public int deleteUser(int userId) {
        return db.delete(Constants.TABLE_USERS, Constants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public int getUserRole(int userId) {
        String query = "SELECT " + Constants.COLUMN_USER_ROLE_ID +
                " FROM " + Constants.TABLE_USER_ROLES +
                " WHERE " + Constants.COLUMN_USER_ID + " = ? LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            int roleIdIndex = cursor.getColumnIndex(Constants.COLUMN_USER_ROLE_ID);
            if (roleIdIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getInt(roleIdIndex); // dùng getInt thay vì getLong
            } else if (roleIdIndex < 0) {
                android.util.Log.e("UserDAO", "Cột " + Constants.COLUMN_USER_ROLE_ID + " không tồn tại trong truy vấn");
            }
        } catch (Exception e) {
            android.util.Log.e("UserDAO", "Lỗi khi lấy vai trò người dùng", e);
        }

        return -1; // Trả về -1 để biểu thị không tìm thấy role (hoặc bạn chọn giá trị mặc định khác)
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