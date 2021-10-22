package com.udacity.jdnd.course3.critter.repository;

import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.User;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select c from Customer c")
    List<Customer> findAllCustomers();
    @Query("select e from Employee e")
    List<Employee> findAllEmployees();
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findCustomerById(Long id);
    @Query("select e from Employee e where e.id = :id")
    Optional<Employee> findEmployeeById(Long id);

    @Query("select e from Employee e where :dayOfWeek member of e.daysAvailable and :skills" +
            " member of e.skills")
    List<Employee> findEmployeesForSchedule(Set<EmployeeSkill> skills, DayOfWeek dayOfWeek);

}
