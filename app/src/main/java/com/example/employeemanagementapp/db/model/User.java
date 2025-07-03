package com.example.employeemanagementapp.db.model;

public class User {
    private long user_id; // Sửa từ int thành long để khớp với UserDAO (db.insert trả về long)
    private String username;
    private String password;
    private Long roleId;       // 1 user chỉ có 1 role (có thể null nếu chưa gán role)
    private Long permissionId; // Nếu bạn vẫn giữ 1 user có 1 permission, hoặc có thể chuyển thành List nếu cần nhiều

    // Constructor đầy đủ
    public User(long user_id, String username, String password, Long roleId) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.roleId = roleId;
    }

    // Constructor không có id (ví dụ dùng khi đăng ký mới)
    public User(String username, String password, Long roleId) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
    }

    // Constructor với id và password (chỉ dùng khi cần)
    public User(long userId, String password) {
        this.user_id = userId;
        this.password = password;
    }

    // Getter & Setter
    public long getId() {
        return user_id;
    }

    public void setId(long id) {
        this.user_id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    // Nếu bạn muốn giữ permissionIds là list thì sửa lại, còn không xóa luôn hoặc để 1 permissionId tương tự roleId
    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + user_id +
                ", username='" + username + '\'' +
                ", roleId=" + roleId +
                ", permissionId=" + permissionId +
                '}';
    }
}
