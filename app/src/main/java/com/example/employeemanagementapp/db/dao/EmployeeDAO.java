package com.example.employeemanagementapp.db.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteBlobTooBigException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.example.employeemanagementapp.db.DatabaseHelper;
import com.example.employeemanagementapp.db.model.Employee;
import com.example.employeemanagementapp.utils.Constants;

import java.io.ByteArrayOutputStream;

public class EmployeeDAO {
    private final SQLiteDatabase db;

    public EmployeeDAO(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public long insertEmployee(Employee emp, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_FIRST_NAME, emp.getFirstName());
        values.put(Constants.COLUMN_LAST_NAME, emp.getLastName());
        values.put(Constants.COLUMN_IMAGE, image);
        values.put(Constants.COLUMN_PHONE_NUMBER, emp.getPhoneNumber());
        values.put(Constants.COLUMN_EMAIL, emp.getEmail());
        values.put(Constants.COLUMN_RESIDENCE, emp.getResidence());
        values.put(Constants.COLUMN_DEPARTMENT_ID, emp.getDepartmentId());
        values.put(Constants.COLUMN_POSITION, emp.getPosition());
        values.put(Constants.COLUMN_GENDER, emp.getGender()); // Thêm giới tính
        values.put(Constants.COLUMN_STATUS, emp.getStatus());
        values.put(Constants.COLUMN_HIRE_DATE, emp.getHireDate()); // Thêm ngày vào làm
        values.put(Constants.COLUMN_SALARY, emp.getSalary()); // Thêm mức lương
        return db.insert(Constants.TABLE_EMPLOYEE, null, values);
    }

    public int updateEmployee(long id, Employee emp, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_FIRST_NAME, emp.getFirstName());
        values.put(Constants.COLUMN_LAST_NAME, emp.getLastName());
        values.put(Constants.COLUMN_IMAGE, image);
        values.put(Constants.COLUMN_PHONE_NUMBER, emp.getPhoneNumber());
        values.put(Constants.COLUMN_EMAIL, emp.getEmail());
        values.put(Constants.COLUMN_RESIDENCE, emp.getResidence());
        values.put(Constants.COLUMN_DEPARTMENT_ID, emp.getDepartmentId());
        values.put(Constants.COLUMN_POSITION, emp.getPosition());
        values.put(Constants.COLUMN_GENDER, emp.getGender()); // Thêm giới tính
        values.put(Constants.COLUMN_STATUS, emp.getStatus());
        values.put(Constants.COLUMN_HIRE_DATE, emp.getHireDate()); // Thêm ngày vào làm
        values.put(Constants.COLUMN_SALARY, emp.getSalary()); // Thêm mức
        return db.update(Constants.TABLE_EMPLOYEE, values, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getEmployeesByDepartment(long deptId) {
        String query = "SELECT * FROM " + Constants.TABLE_EMPLOYEE + " WHERE " + Constants.COLUMN_DEPARTMENT_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(deptId)});
    }

    // New method: Filter employees by department and name query
    public Cursor getEmployeesByDepartmentFiltered(long deptId, String query) {
        String selection = Constants.COLUMN_DEPARTMENT_ID + " = ? AND (" + Constants.COLUMN_FIRST_NAME + " LIKE ? OR " + Constants.COLUMN_LAST_NAME + " LIKE ?)";
        String[] selectionArgs = new String[]{String.valueOf(deptId), "%" + query + "%", "%" + query + "%"};
        return db.query(Constants.TABLE_EMPLOYEE, null, selection, selectionArgs, null, null, null);
    }

    public boolean hasEmployeesInDepartment(long departmentId) {
        Cursor cursor = db.query(Constants.TABLE_EMPLOYEE, new String[]{Constants.COLUMN_ID},
                Constants.COLUMN_DEPARTMENT_ID + "=?", new String[]{String.valueOf(departmentId)},
                null, null, null);
        boolean hasEmployees = cursor.getCount() > 0;
        cursor.close();
        return hasEmployees;
    }


    public Cursor getAllEmployees() {
        return db.rawQuery(
                "SELECT " +
                        Constants.COLUMN_ID + ", " +
                        Constants.COLUMN_FIRST_NAME + ", " +
                        Constants.COLUMN_LAST_NAME + ", " +
                        Constants.COLUMN_POSITION + ", " +
                        Constants.COLUMN_STATUS + ", " +
//                        Constants.COLUMN_IMAGE + ", " +
                        Constants.COLUMN_DEPARTMENT_ID +
                        " FROM " + Constants.TABLE_EMPLOYEE,
                null
        );
    }

    public Cursor getEmployeeById(long id) {
        String[] columns = {
                Constants.COLUMN_ID,
                Constants.COLUMN_FIRST_NAME,
                Constants.COLUMN_LAST_NAME,
                Constants.COLUMN_PHONE_NUMBER,
                Constants.COLUMN_EMAIL,
                Constants.COLUMN_RESIDENCE,
                Constants.COLUMN_POSITION,
                Constants.COLUMN_DEPARTMENT_ID,
                Constants.COLUMN_GENDER,
                Constants.COLUMN_STATUS,
                Constants.COLUMN_HIRE_DATE,
                Constants.COLUMN_SALARY
        };
        return db.query(Constants.TABLE_EMPLOYEE, columns, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }

//    public byte[] getEmployeeProfileImage(long employeeId) {
//        Cursor cursor = db.query(Constants.TABLE_EMPLOYEE, new String[]{Constants.COLUMN_IMAGE}, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(employeeId)}, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            @SuppressLint("Range") byte[] image = cursor.getBlob(cursor.getColumnIndex(Constants.COLUMN_IMAGE));
//            cursor.close();
//            return image;
//        }
//        return null;
//    }

    public byte[] getEmployeeProfileImage(long employeeId) {
        // BƯỚC 1: Kiểm tra kích thước ảnh trước
        if (!isImageLoadable(employeeId)) {
            Log.w("EmployeeDAO", "Image too large to load safely for employee: " + employeeId);
            return createPlaceholderImage(); // Hoặc return null
        }

        // BƯỚC 2: Load ảnh an toàn
        Cursor cursor = null;
        try {
            cursor = db.query(Constants.TABLE_EMPLOYEE,
                    new String[]{Constants.COLUMN_IMAGE},
                    Constants.COLUMN_ID + "=?",
                    new String[]{String.valueOf(employeeId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range")
                byte[] image = cursor.getBlob(cursor.getColumnIndex(Constants.COLUMN_IMAGE));

                if (image != null && image.length > 0) {
                    // Resize ảnh để tiết kiệm memory
                    return resizeImage(image, 600, 600); // Giảm từ 800 xuống 600
                }
            }
            return null;

        } catch (SQLiteBlobTooBigException e) {
            Log.e("EmployeeDAO", "Blob too big for employee " + employeeId + ": " + e.getMessage());
            return createPlaceholderImage();
        } catch (Exception e) {
            Log.e("EmployeeDAO", "Error loading image for employee " + employeeId + ": " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Method kiểm tra kích thước ảnh TRƯỚC KHI load
    private boolean isImageLoadable(long employeeId) {
        Cursor cursor = null;
        try {
            // Chỉ lấy kích thước, không lấy data
            cursor = db.rawQuery("SELECT LENGTH(" + Constants.COLUMN_IMAGE + ") as image_size FROM " +
                            Constants.TABLE_EMPLOYEE + " WHERE " + Constants.COLUMN_ID + "=?",
                    new String[]{String.valueOf(employeeId)});

            if (cursor != null && cursor.moveToFirst()) {
                long imageSize = cursor.getLong(0);
                // Giới hạn 1.5MB để an toàn (CursorWindow limit ~2MB)
                return imageSize <= (1536 * 1024);
            }
            return true; // Nếu không có ảnh thì OK

        } catch (Exception e) {
            Log.e("EmployeeDAO", "Error checking image size: " + e.getMessage());
            return false; // Conservative approach
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Tạo ảnh placeholder đơn giản
    private byte[] createPlaceholderImage() {
        try {
            // Tạo bitmap đơn giản 200x200 màu xám
            Bitmap placeholder = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            placeholder.eraseColor(Color.LTGRAY);

            // Convert thành byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            placeholder.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] result = stream.toByteArray();

            placeholder.recycle();
            stream.close();

            return result;
        } catch (Exception e) {
            Log.e("EmployeeDAO", "Error creating placeholder: " + e.getMessage());
            return null;
        }
    }

    // Cải thiện method resizeImage
    private byte[] resizeImage(byte[] imageBytes, int maxWidth, int maxHeight) {
        try {
            // Decode với options để tiết kiệm memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);

            // Tính sample size
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565; // Tiết kiệm memory

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
            if (bitmap == null) return null;

            // Resize chính xác
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

            if (ratio < 1.0f) {
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                bitmap.recycle();
                bitmap = resized;
            }

            // Compress
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            byte[] result = stream.toByteArray();

            bitmap.recycle();
            stream.close();

            return result;

        } catch (OutOfMemoryError e) {
            Log.e("EmployeeDAO", "Out of memory resizing image: " + e.getMessage());
            return createPlaceholderImage();
        } catch (Exception e) {
            Log.e("EmployeeDAO", "Error resizing image: " + e.getMessage());
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

//    public byte[] resizeImage(byte[] originalImage, int maxWidth, int maxHeight) {
//        // Giải mã byte[] thành Bitmap
//        Bitmap bitmap = BitmapFactory.decodeByteArray(originalImage, 0, originalImage.length);
//        if (bitmap == null) return null;
//
//        // Tính tỉ lệ resize giữ tỉ lệ gốc
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        float ratio = Math.min((float)maxWidth / width, (float)maxHeight / height);
//
//        int newWidth = Math.round(width * ratio);
//        int newHeight = Math.round(height * ratio);
//
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
//
//        // Nén Bitmap resized về byte[]
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
//
//        // Giải phóng bộ nhớ bitmap cũ
//        bitmap.recycle();
//        resizedBitmap.recycle();
//
//        return outputStream.toByteArray();
//    }


    public int deleteEmployee(long id) {
        int rowsDeleted = db.delete(Constants.TABLE_EMPLOYEE, Constants.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public Cursor getAllEmployeesFiltered(String query) {
        String selection = Constants.COLUMN_FIRST_NAME + " LIKE ? OR " + Constants.COLUMN_LAST_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        return db.query(Constants.TABLE_EMPLOYEE, null, selection, selectionArgs, null, null, null);
    }


}