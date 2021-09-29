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
            throw new RuntimeException("User not found");
        }
    }

    public List<Employee> findAllEmployees() {return userRepository.findAllEmployees();}

    public List<Customer> findAllCustomers(){return userRepository.findAllCustomers();}

    public List<User> findAllUsers(){
        return (List<User>) userRepository.findAll();
    }
}
