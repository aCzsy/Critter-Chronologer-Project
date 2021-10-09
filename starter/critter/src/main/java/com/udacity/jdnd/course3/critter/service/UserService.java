package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.UserNotFoundException;
import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.User;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    public Customer updateCustomer(Customer customer){
        return userRepository.findCustomerById(customer.getId())
                .map(cust -> {
                    Optional<String> phoneNumber = Optional.ofNullable(customer.getPhoneNumber());
                    if(phoneNumber.isPresent()) cust.setPhoneNumber(customer.getPhoneNumber()); else cust.setPhoneNumber(cust.getPhoneNumber());
                    Optional<String> name = Optional.ofNullable(customer.getName());
                    if(name.isPresent()) cust.setName(customer.getName()); else cust.setName(cust.getName());
                    Optional<String> notes = Optional.ofNullable(customer.getNotes());
                    if(notes.isPresent()) cust.setNotes(customer.getNotes()); else cust.setNotes(cust.getNotes());
                    return cust;
                }).orElseThrow(UserNotFoundException::new);
    }

    public Employee updateEmployee(Employee employee){
        return userRepository.findEmployeeById(employee.getId())
                .map(emp -> {
                    Optional<String> name = Optional.ofNullable(employee.getName());
                    if(name.isPresent()) emp.setName(employee.getName()); else emp.setName(emp.getName());
                    emp.setSkills(employee.getSkills());
                    emp.setDaysAvailable(employee.getDaysAvailable());
                    return emp;
                }).orElseThrow(UserNotFoundException::new);
    }

}
