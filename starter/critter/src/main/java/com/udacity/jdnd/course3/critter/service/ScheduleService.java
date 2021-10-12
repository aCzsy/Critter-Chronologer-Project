package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.ScheduleNotFoundException;
import com.udacity.jdnd.course3.critter.Exception.UserNotFoundException;
import com.udacity.jdnd.course3.critter.entity.*;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import com.udacity.jdnd.course3.critter.repository.ScheduleRepository;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
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

    //This method adds employees to the list of already existing employees in a schedule
    public Schedule addEmployeesToSchedule(Long scheduleId, List<Long> employeeIds){
        Schedule schedule = this.getScheduleById(scheduleId);
        employeeIds
            .forEach(id -> {
                //Looking for an employee with such id
                Optional<Employee> employee = userRepository.findEmployeeById(id);
                //If employee exists,check if this employee already not on a schedule,
                //If employee doesn't exist, do nothing
                employee.ifPresent(emp -> {
                    if(!(schedule.getEmployeeList().contains(emp))){
                        //Checking if an employee contains any of the skills required for the schedule,
                        //If yes, add employee to the schedule
                        boolean hasSkill = schedule.getActivities()
                                .stream()
                                .anyMatch(element -> emp.getSkills().contains(element));
                        if(hasSkill){
                            schedule.getEmployeeList().add(emp);
                        }
                        else throw new RuntimeException("Employee doesn't have required skills");
                    }
                });
            });
        return schedule;
    }

    //This method adds pets to the list of already existing pets in a schedule
    public Schedule addPetsToSchedule(Long scheduleId, List<Long> petIds){
        Schedule schedule = this.getScheduleById(scheduleId);
        petIds
            .forEach(id -> {
                //Looking for a pet with such id
                Optional<Pet> pet = petRepository.findById(id);
                //If pet exists,check if this pet already not on a schedule,
                //If pet doesn't exist, do nothing
                pet.ifPresent(pet1 -> {
                    if(!(schedule.getPetList().contains(pet1))){
                        schedule.getPetList().add(pet1);
                    }
                });
            });

        return schedule;
    }

    //This method adds activities to the list of already existing activities in a schedule
    public Schedule addActivitiesToSchedule(Long scheduleId, Set<EmployeeSkill> activities){
        Schedule schedule = this.getScheduleById(scheduleId);
        schedule.getActivities().addAll(activities);
        //Getting updated list of activities for this schedule
        List<EmployeeSkill> updatedListOfSkills = Lists.newArrayList(schedule.getActivities());
        //Keeping only distinct activities as there should be no duplicates
        updatedListOfSkills = updatedListOfSkills.stream().distinct().collect(Collectors.toList());
        //Saving updated list as a new list for current schedule
        schedule.setActivities(updatedListOfSkills);
        return schedule;
    }

    //This method should be used to update full schedule
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
