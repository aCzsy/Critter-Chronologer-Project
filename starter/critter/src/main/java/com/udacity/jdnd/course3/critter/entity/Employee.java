package com.udacity.jdnd.course3.critter.entity;

import com.udacity.jdnd.course3.critter.user.EmployeeSkill;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Employee extends User {
    @NotNull(message = "Skills list is required")
    @NotEmpty(message = "Skills list can not be empty")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(length = 15)
    //@JoinTable(name = "employee_skills")
    private Set<EmployeeSkill> skills = new HashSet<>();
    @NotNull(message = "List of days available is required")
    @NotEmpty(message = "List of days available can not be empty")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_days_available", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(length = 15)
    //@JoinTable(name = "employee_days_available")
    private Set<DayOfWeek> daysAvailable = new HashSet<>();

    public Employee() {
    }

//    public Employee(Set<EmployeeSkill> skills, Set<DayOfWeek> daysAvailable) {
//        this.skills = skills;
//        this.daysAvailable = daysAvailable;
//    }

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
}
