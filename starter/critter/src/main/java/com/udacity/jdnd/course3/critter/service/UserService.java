package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.UserNotFoundException;
import com.udacity.jdnd.course3.critter.controller.UserController;
import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.User;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import com.udacity.jdnd.course3.critter.user.EmployeeDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    @Autowired
    UserRepository userRepository;

    public User saveUser(User user){
        return userRepository.save(user);
    }

    public User findUserById(Long id){
        Optional<User> foundUser = userRepository.findById(id);
        if(foundUser.isPresent()){
            return foundUser.get();
        } else{
            throw new UserNotFoundException("User not found");
        }
    }

    public List<Employee> findEmployeesForSchedule(Set<EmployeeSkill> skills, LocalDate date){
        List<Employee> employeeList =
            userRepository.findAllEmployees()
            .stream()
            .filter(employee -> employee.getSkills().containsAll(skills)
            && employee.getDaysAvailable().contains(date.getDayOfWeek()))
            .collect(Collectors.toList());
        return employeeList;
    }

    public List<Employee> findAllEmployees() {return userRepository.findAllEmployees();}

    public List<Customer> findAllCustomers(){return userRepository.findAllCustomers();}

    public List<User> findAllUsers(){
        return (List<User>) userRepository.findAll();
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    //As there is no validation for fields of Customer entity,
    //Each field of a Requestbody is checked, if it is null or empty,no change to that field is made,
    //If its not empty, field gets updated
    public Customer updateCustomer(Customer customer){
        return userRepository.findCustomerById(customer.getId())
                .map(cust -> {
                    cust.setPhoneNumber(Optional.ofNullable(customer.getPhoneNumber()).orElse(cust.getPhoneNumber()));
                    cust.setName(Optional.ofNullable(customer.getName()).orElse(cust.getName()));
                    cust.setNotes(Optional.ofNullable(customer.getNotes()).orElse(cust.getNotes()));
                    return cust;
                }).orElseThrow(UserNotFoundException::new);
    }

    //Optional check only done to name field as its not annotated with any validation options
    //Other fields must be present as they must pass validation constraints.
    public Employee updateEmployee(Employee employee){
        return userRepository.findEmployeeById(employee.getId())
                .map(emp -> {
                    emp.setName(Optional.ofNullable(employee.getName()).orElse(emp.getName()));
                    emp.setSkills(employee.getSkills());
                    emp.setDaysAvailable(employee.getDaysAvailable());
                    emp.setAvailableTimes(Optional.ofNullable(employee.getAvailableTimes()).orElse(emp.getAvailableTimes()));
                    return emp;
                }).orElseThrow(UserNotFoundException::new);
    }

}
