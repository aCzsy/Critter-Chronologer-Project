package com.udacity.jdnd.course3.critter.entity;

import com.udacity.jdnd.course3.critter.user.EmployeeSkill;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Employee extends User {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(length = 15)
    //@JoinTable(name = "employee_skills")
    private Set<EmployeeSkill> skills = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_days_available", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(length = 15)
    //@JoinTable(name = "employee_days_available")
    private Set<DayOfWeek> daysAvailable = new HashSet<>();
    @ElementCollection
    @CollectionTable(name = "employee_times_available", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(length = 10)
    private List<LocalTime> availableTimes = new ArrayList<>();

    public Employee() {
    }

    public Set<EmployeeSkill> getSkills() {
        return skills;
    }

    public void setSkills(Set<EmployeeSkill> skills) {
        this.skills = skills;
    }

    public Set<DayOfWeek> getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(Set<DayOfWeek> daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public List<LocalTime> getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(List<LocalTime> availableTimes) {
        this.availableTimes = availableTimes;
    }
}
