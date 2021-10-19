package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.ScheduleNotFoundException;
import com.udacity.jdnd.course3.critter.Exception.UserNotFoundException;
import com.udacity.jdnd.course3.critter.entity.*;
import com.udacity.jdnd.course3.critter.pet.PetType;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import com.udacity.jdnd.course3.critter.repository.ScheduleRepository;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
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

    /**
     * This method only saves a schedule that passes all the constraint checks
     * Otherwise throws a RuntimeException stating IDs of pets/employees with corresponding reason(s)
     * @param schedule
     * @return Schedule is saved or an exception is thrown.
     */
    public Schedule saveSchedule(Schedule schedule){
        //Lists with pets/employees IDs that didnt pass constraints checks
        List<Long> employees_without_needed_skills = filterEmployeesWithSuitableSkillsAndAvailable(schedule);
        List<Long> pets_without_match = filterPetsSuitableForActivitiesOnSchedule(schedule);

        //If lists are empty, its safe to save a schedule, otherwise exceptions are thrown
        if(employees_without_needed_skills.isEmpty() && pets_without_match.isEmpty()){
            return scheduleRepository.save(schedule);
        } else if(employees_without_needed_skills.isEmpty()) {
            throw new RuntimeException("Pets with IDs: " + pets_without_match.toString() + " not suitable for scheduled activities");
        } else if(pets_without_match.isEmpty()){
            throw new RuntimeException("Employees with IDs: " + employees_without_needed_skills.toString() + " don't have skills required for a schedule");
        } else{
            throw new RuntimeException("Employees with IDs " + employees_without_needed_skills.toString() + " don't have skills required for a schedule." +
                    " Pets with IDs " + pets_without_match.toString() + " not suitable for scheduled activities");
        }
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
                        boolean hasSkill = schedule.getActivities()
                                .stream()
                                .anyMatch(element -> emp.getSkills().contains(element));
                        //if employee will be available on a schedule day
                        if(emp.getDaysAvailable().contains(schedule.getDate().getDayOfWeek())){
                                //if there are start and end times set on a schedule
                                if(schedule.getStartTime() != null && schedule.getEndTime() != null) {
                                    //if employee will be available on a schedule day
                                    //if employee has available time slots mentioned
                                    if (emp.getDaysAvailable().size() > 0) {
                                        //check if an employee will be available during start and end time of a schedule session
                                        boolean timeMatch = emp.getAvailableTimes()
                                                .stream()
                                                .anyMatch(time -> ((time.isBefore(schedule.getEndTime()) && time.isAfter(schedule.getStartTime())) || time == schedule.getStartTime()));
                                        if (!timeMatch)
                                            throw new RuntimeException("Employee's [ ID:" + emp.getId() + " ] time slots don't match with the schedule");
                                    }
                                }
                            } else throw new RuntimeException("Employee's [ ID:" + emp.getId() + " ] available days don't match with the schedule");
                        //Employees that will reach this statement:
                        //1.employees that don't have timesAvailable set
                        //2.If start and end times are set in a schedule and employees are available within this timeframe
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
                    sched.setDate(schedule.getDate());
                    Optional<LocalTime> startTime = Optional.ofNullable(schedule.getStartTime());
                    Optional<LocalTime> endTime = Optional.ofNullable(schedule.getEndTime());
                    if(startTime.isPresent()) sched.setStartTime(startTime.get()); else sched.setStartTime(sched.getStartTime());
                    if(endTime.isPresent()) sched.setEndTime(endTime.get()); else sched.setEndTime(schedule.getEndTime());
                    return this.saveSchedule(sched);
                }).orElseThrow(ScheduleNotFoundException::new);
    }


    /**
     * This method checks list of pets within a schedule which is about to be added.
     * It scans each dog and checks if scheduled activities can be applied for it.
     * @param schedule
     * @return List of dog IDs that don't match with the scheduled activities
     *
     */

    private List<Long> filterPetsSuitableForActivitiesOnSchedule(Schedule schedule){
        List<Pet> petsForException = new ArrayList<>(schedule.getPetList());
        List<Long> petIdsForException = new ArrayList<>();

        //Hashmap with all possible pet types and activities that can be applied to each
        Map<PetType,List<EmployeeSkill>> activitiesForPets = new HashMap<>();

        activitiesForPets.put(PetType.CAT,Lists.newArrayList(
                EmployeeSkill.PETTING,EmployeeSkill.SHAVING,EmployeeSkill.MEDICATING,EmployeeSkill.FEEDING,EmployeeSkill.WALKING));
        activitiesForPets.put(PetType.DOG, Lists.newArrayList(
                EmployeeSkill.SHAVING,EmployeeSkill.MEDICATING,EmployeeSkill.FEEDING,EmployeeSkill.WALKING
        ));
        activitiesForPets.put(PetType.LIZARD,Lists.newArrayList(
                EmployeeSkill.FEEDING,EmployeeSkill.MEDICATING,EmployeeSkill.PETTING
        ));
        activitiesForPets.put(PetType.BIRD,Lists.newArrayList(
                EmployeeSkill.PETTING,EmployeeSkill.MEDICATING,EmployeeSkill.FEEDING
        ));
        activitiesForPets.put(PetType.FISH,Lists.newArrayList(
                EmployeeSkill.PETTING,EmployeeSkill.MEDICATING,EmployeeSkill.FEEDING
        ));
        activitiesForPets.put(PetType.SNAKE,Lists.newArrayList(
                EmployeeSkill.PETTING,EmployeeSkill.MEDICATING,EmployeeSkill.FEEDING
        ));

        //Iterating through a list of pets in a schedule
        //and checking if there are activities that match pet types.
        //This list will only contain pets that match scheduled activities.
        List<Pet> petList =
                schedule.getPetList()
                .stream()
                .filter(pet -> activitiesForPets
                        .get(pet.getType())
                        .stream()
                        .anyMatch(skill -> schedule.getActivities().contains(skill)))
                .collect(Collectors.toList());

        //Comparing with unfiltered list and returning IDs of pets that match with scheduled activities
        if(petList.size() < schedule.getPetList().size()){
            petsForException.removeAll(petList);
            petIdsForException =
                    petsForException
                            .stream()
                            .map(Pet::getId)
                            .collect(Collectors.toList());
        }
        return petIdsForException;
    }

    /**
     * This method checks list of employees within a schedule which is about to be added.
     * It scans each employee and checks if they have required skills for scheduled activitirs.
     * @param schedule
     * @return List of employee IDs that don't have required skills for scheduled activities
     *
     */
    private List<Long> filterEmployeesWithSuitableSkillsAndAvailable(Schedule schedule){
        List<Employee> employeesForException = new ArrayList<>(schedule.getEmployeeList());
        List<Long> employeeIdsForException = new ArrayList<>();

        List<Employee> filteredEmployees =
                schedule.getEmployeeList()
                        .stream()
                        .filter(employee -> {
                            boolean hasSkill = schedule.getActivities()
                                    .stream()
                                    .anyMatch(element -> employee.getSkills().contains(element));
                                //if employee will be available on a schedule day
                                if(employee.getDaysAvailable().contains(schedule.getDate().getDayOfWeek())) {
                                    //if there are start and end times set on a schedule
                                    if(schedule.getStartTime() != null && schedule.getEndTime() != null) {
                                        //if employee has available time slots mentioned
                                        if (employee.getAvailableTimes().size() > 0) {
                                            //check if an employee will be available during start and end time of a schedule session
                                            boolean timeMatch = employee.getAvailableTimes()
                                                    .stream()
                                                    .anyMatch(time -> ((time.isBefore(schedule.getEndTime()) && time.isAfter(schedule.getStartTime())) || time == schedule.getStartTime()));
                                            if (!timeMatch)
                                                throw new RuntimeException("Employee's [ ID:" + employee.getId() + " ] time slots don't match with the schedule");
                                        }
                                    }
                                } else throw new RuntimeException("Employee's [ ID:" + employee.getId() + " ] available days don't match with the schedule");
                            //Employees that will reach this statement:
                            //1.employees that don't have timesAvailable set
                            //2.If start and end times are set in a schedule and employees are available within this timeframe
                            //@return boolean that further checks if employee has required skills for a schedule
                            return hasSkill;
                        })
                        .collect(Collectors.toList());


        //Comparing with unfiltered list and returning IDs of employees that have required skills.
        if(filteredEmployees.size() < schedule.getEmployeeList().size()){
            employeesForException.removeAll(filteredEmployees);
            employeeIdsForException =
                    employeesForException
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

        }

        return employeeIdsForException;
    }

}
