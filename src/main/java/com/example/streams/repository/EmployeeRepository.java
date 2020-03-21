package com.example.streams.repository;

import com.example.streams.domain.Employee;

import java.util.HashMap;
import java.util.Map;

public class EmployeeRepository {

    private static Map<Long, Employee> hashMap;

    static {
        hashMap = new HashMap<>();
        hashMap.put(1L, new Employee(1L, "Jeff Bezos", 100_000.0));
        hashMap.put(2L, new Employee(2L, "Bill Gates", 200_000.0));
        hashMap.put(3L, new Employee(3L, "Mark Zuckerberg", 300_000.0));
    }

    public Employee findById(final Long id) {
        return hashMap.get(id);
    }
}
