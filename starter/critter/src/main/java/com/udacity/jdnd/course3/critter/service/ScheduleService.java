package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.ScheduleNotFoundException;
import com.udacity.jdnd.course3.critter.Exception.UserNotFoundException;
import com.udacity.jdnd.course3.critter.entity.*;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import com.udacity.jdnd.course3.critter.repository.ScheduleRepository;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PetRepository petRepository;

    public Schedule saveSchedule(Schedule schedule){
        return scheduleRepository.save(schedule);
    }

    public List<Schedule> getAllSchedules(){
        return scheduleRepository.findAll();
    }

    public Schedule getScheduleById(Long id){
        return scheduleRepository.getOne(id);
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

    public void deleteSchedule(Long id){
        scheduleRepository.deleteById(id);
    }

    public Schedule addEmployeesToSchedule(Long scheduleId, List<Long> employeeIds){
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        List<Employee> employees =
            employeeIds
                .stream()
                .map(id -> {
                    Optional<Employee> employee = userRepository.findEmployeeById(id);
                    //If present,add,otherwise do nothing
                    employee.ifPresent(value -> schedule.getEmployeeList().add(value));
                    return employee.get();
                })
                .collect(Collectors.toList());
        return schedule;
    }

    public Schedule addPetsToSchedule(Long scheduleId, List<Long> petIds){
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        List<Pet> pets =
                petIds
                .stream()
                .map(id -> {
                    Optional<Pet> pet = petRepository.findById(id);
                    pet.ifPresent(pet1 -> schedule.getPetList().add(pet1));
                    return pet.get();
                }).collect(Collectors.toList());
        return schedule;
    }

    public Schedule addActivitiesToSchedule(Long scheduleId, Set<EmployeeSkill> activities){
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        schedule.getActivities().addAll(activities);
        return schedule;
    }

    public Schedule updateSchedule(Schedule schedule){
        return scheduleRepository.findById(schedule.getId())
                .map(sched -> {
                    sched.setEmployeeList(schedule.getEmployeeList());
                    sched.setActivities(schedule.getActivities());
                    sched.setPetList(schedule.getPetList());
                    Optional<LocalDate> localDate = Optional.ofNullable(schedule.getDate());
                    if(localDate.isPresent()) sched.setDate(schedule.getDate()); else sched.setDate(sched.getDate());
                    return sched;
                }).orElseThrow(ScheduleNotFoundException::new);
    }

}
