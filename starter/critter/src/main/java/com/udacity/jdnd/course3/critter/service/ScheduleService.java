package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.UserNotFoundException;
import com.udacity.jdnd.course3.critter.entity.*;
import com.udacity.jdnd.course3.critter.repository.ScheduleRepository;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    UserRepository userRepository;

    public Schedule saveSchedule(Schedule schedule){
        return scheduleRepository.save(schedule);
    }

    public List<Schedule> getAllSchedules(){
        return scheduleRepository.findAll();
    }

    public List<Schedule> findSchedulesByEmployee(Employee employee){
        return scheduleRepository.findSchedulesByEmployee(employee);
    }

    public List<Schedule> findSchedulesByPet(Pet pet){
        return scheduleRepository.findSchedulesByPet(pet);
    }

    public List<Schedule> findSchedulesByCustomer(Long customerId){
        Customer foundCustomer;
        Optional<User> customer = userRepository.findById(customerId);
        if(customer.isPresent()){
            foundCustomer = (Customer) customer.get();
        } else {
            throw new UserNotFoundException("Customer not found");
        }
        //pets.forEach(pet -> schedules.addAll(scheduleRepository.findSchedulesByPet(pet)));
        //return schedules.stream().distinct().collect(Collectors.toList());
        return scheduleRepository.findAllByPetListIn(foundCustomer.getPets())
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

}
