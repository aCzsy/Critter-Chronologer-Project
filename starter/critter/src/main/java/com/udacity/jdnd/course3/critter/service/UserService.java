package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.UserNotFoundException;
import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.User;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
                    Optional<String> phoneNumber = Optional.ofNullable(customer.getPhoneNumber());
                    if(phoneNumber.isPresent()) cust.setPhoneNumber(phoneNumber.get()); else cust.setPhoneNumber(cust.getPhoneNumber());
                    Optional<String> name = Optional.ofNullable(customer.getName());
                    if(name.isPresent()) cust.setName(name.get()); else cust.setName(cust.getName());
                    Optional<String> notes = Optional.ofNullable(customer.getNotes());
                    if(notes.isPresent()) cust.setNotes(notes.get()); else cust.setNotes(cust.getNotes());
                    return cust;
                }).orElseThrow(UserNotFoundException::new);
    }

    //Optional check only done to name field as its not annotated with any validation options
    //Other fields must be present as they must pass validation constraints.
    public Employee updateEmployee(Employee employee){
        return userRepository.findEmployeeById(employee.getId())
                .map(emp -> {
                    Optional<String> name = Optional.ofNullable(employee.getName());
                    if(name.isPresent()) emp.setName(name.get()); else emp.setName(emp.getName());
                    emp.setSkills(employee.getSkills());
                    emp.setDaysAvailable(employee.getDaysAvailable());
                    Optional<List<LocalTime>> timesAvailable = Optional.ofNullable(employee.getAvailableTimes());
                    if(timesAvailable.isPresent()) emp.setAvailableTimes(timesAvailable.get()); else emp.setAvailableTimes(emp.getAvailableTimes());
                    return emp;
                }).orElseThrow(UserNotFoundException::new);
    }

}
