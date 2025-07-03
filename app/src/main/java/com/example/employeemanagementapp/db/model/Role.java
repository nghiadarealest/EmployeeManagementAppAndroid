package com.example.employeemanagementapp.db.model;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private int roleId;
    private String name;
    private List<Integer> permissionIds;

    public Role(int roleId, String name) {
        this.roleId = roleId;
        this.name = name;
        this.permissionIds = new ArrayList<>();
    }

    // Getters and setters
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Integer> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public int getId() {
        return roleId;
    }

    @Override
    public String toString() {
        return name;
    }
}
