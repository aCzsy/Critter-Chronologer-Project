package com.udacity.jdnd.course3.critter.controller;

import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.entity.Schedule;
import com.udacity.jdnd.course3.critter.schedule.ScheduleDTO;
import com.udacity.jdnd.course3.critter.service.PetService;
import com.udacity.jdnd.course3.critter.service.ScheduleService;
import com.udacity.jdnd.course3.critter.service.UserService;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Schedules.
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final UserService userService;
    private final PetService petService;
    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(UserService userService, PetService petService, ScheduleService scheduleService) {
        this.userService = userService;
        this.petService = petService;
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ScheduleDTO createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        Schedule schedule = scheduleService.saveSchedule(convertScheduleDTOtoSchedule(scheduleDTO));
        return convertScheduleToScheduleDTO(schedule);
    }

    @GetMapping("/{scheduleId}")
    public ScheduleDTO getSchedule(@PathVariable Long scheduleId){
        return convertScheduleToScheduleDTO(scheduleService.getScheduleById(scheduleId));
    }

    @GetMapping
    public List<ScheduleDTO> getAllSchedules() {
        List<ScheduleDTO> scheduleDTOList =
                scheduleService.getAllSchedules()
                .stream()
                .map(schedule -> convertScheduleToScheduleDTO(schedule))
                .collect(Collectors.toList());
        return scheduleDTOList;
    }

    @GetMapping("/pet/{petId}")
    public List<ScheduleDTO> getScheduleForPet(@PathVariable long petId) {
        Pet pet = petService.findPetById(petId);
        List<Schedule> foundList = scheduleService.findSchedulesByPet(pet);
        List<ScheduleDTO> listToReturn =
                foundList
                .stream()
                .map(schedule -> convertScheduleToScheduleDTO(schedule))
                .collect(Collectors.toList());
        return listToReturn;
    }

    @GetMapping("/employee/{employeeId}")
    public List<ScheduleDTO> getScheduleForEmployee(@PathVariable long employeeId) {
        Employee employee = (Employee) userService.findUserById(employeeId);
        List<Schedule> foundList = scheduleService.findSchedulesByEmployee(employee);
        List<ScheduleDTO> listToReturn =
                foundList
                .stream()
                .map(schedule -> convertScheduleToScheduleDTO(schedule))
                .collect(Collectors.toList());
        return listToReturn;
    }

    @GetMapping("/customer/{customerId}")
    public List<ScheduleDTO> getScheduleForCustomer(@PathVariable long customerId) {
        List<Schedule> schedules = scheduleService.findSchedulesByCustomer(customerId);
        List<ScheduleDTO> listToReturn =
                schedules
                .stream()
                .map(schedule -> convertScheduleToScheduleDTO(schedule))
                .collect(Collectors.toList());
        return listToReturn;
    }

    @DeleteMapping("/delete/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId){
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/addEmployees/{scheduleId}")
    public ScheduleDTO addEmployeesToSchedule(@PathVariable Long scheduleId, @RequestBody List<Long> employeeIds){
        return convertScheduleToScheduleDTO(scheduleService.addEmployeesToSchedule(scheduleId,employeeIds));
    }

    @PutMapping("/update/addPets/{scheduleId}")
    public ScheduleDTO addPetsToSchedule(@PathVariable Long scheduleId, @RequestBody List<Long> petIds){
        return convertScheduleToScheduleDTO(scheduleService.addPetsToSchedule(scheduleId,petIds));
    }

    @PutMapping("update/addActivities/{scheduleId}")
    public ScheduleDTO addActivitiesToSchedule(@PathVariable Long scheduleId, @RequestBody Set<EmployeeSkill> activities){
        return convertScheduleToScheduleDTO(scheduleService.addActivitiesToSchedule(scheduleId,activities));
    }

    @PutMapping("/update/{scheduleId}")
    public ScheduleDTO updateFullSchedule(@PathVariable Long scheduleId, @Valid @RequestBody ScheduleDTO scheduleDTO){
        scheduleDTO.setId(scheduleId);
        return convertScheduleToScheduleDTO(scheduleService.updateSchedule(convertScheduleDTOtoSchedule(scheduleDTO)));
    }

    private Schedule convertScheduleDTOtoSchedule(ScheduleDTO scheduleDTO){
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleDTO,schedule);
        scheduleDTO.getEmployeeIds().forEach(employeeId -> {
            Employee employee = (Employee) userService.findUserById(employeeId);
            schedule.getEmployeeList().add(employee);
        });
        scheduleDTO.getPetIds().forEach(petId -> {
            Pet pet = petService.findPetById(petId);
            schedule.getPetList().add(pet);
        });
        schedule.setActivities(new ArrayList<>(scheduleDTO.getActivities()));

        return schedule;
    }

    private ScheduleDTO convertScheduleToScheduleDTO(Schedule schedule){
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        BeanUtils.copyProperties(schedule,scheduleDTO);
        schedule.getEmployeeList().forEach(employee -> scheduleDTO.getEmployeeIds().add(employee.getId()));
        schedule.getPetList().forEach(pet -> scheduleDTO.getPetIds().add(pet.getId()));
        scheduleDTO.setActivities(new HashSet<>(schedule.getActivities()));

        return scheduleDTO;
    }
}
