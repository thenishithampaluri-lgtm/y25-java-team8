package com.app.inputs;

public class Employee {
    private final String id;
    private final String dept;
    private final String level;

    public Employee(String id, String dept, String level) {
        this.id = id;
        this.dept = dept;
        this.level = level;
    }

    public String getId() { return id; }
    public String getDept() { return dept; }
    public String getLevel() { return level; }
}
