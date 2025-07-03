package com.example.employeemanagementapp.db.model;

public class Permission {
    private int id;
    private String name;

    public Permission(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
