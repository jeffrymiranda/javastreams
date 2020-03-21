package com.example.streams.domain;

public class Employee {

    private Long id;
    private String fullName;
    private double salary;

    public Employee(Long id, String fullName, double salary) {
        this.id = id;
        this.fullName = fullName;
        this.salary = salary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public void salaryIncrement(double percent) {
        if (percent > 0) {
            this.salary = this.salary * (percent / 100) + this.salary;
        }
    }
}
