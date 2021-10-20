package com.udacity.jdnd.course3.critter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.udacity.jdnd.course3.critter.controller.PetController;
import com.udacity.jdnd.course3.critter.controller.UserController;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.pet.PetDTO;
import com.udacity.jdnd.course3.critter.pet.PetType;
import com.udacity.jdnd.course3.critter.controller.ScheduleController;
import com.udacity.jdnd.course3.critter.schedule.ScheduleDTO;
import com.udacity.jdnd.course3.critter.user.*;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is a set of functional tests to validate the basic capabilities desired for this application.
 * Students will need to configure the application to run these tests by adding application.properties file
 * to the test/resources directory that specifies the datasource. It can run using an in-memory H2 instance
 * and should not try to re-use the same datasource used by the rest of the app.
 *
 * These tests should all pass once the project is complete.
 */
@Transactional
@SpringBootTest(classes = CritterApplication.class)
public class CritterFunctionalTest {

    @Autowired
    private UserController userController;

    @Autowired
    private PetController petController;

    @Autowired
    private ScheduleController scheduleController;

    @Test
    public void testCreateCustomer(){
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.saveCustomer(customerDTO);
        CustomerDTO retrievedCustomer = userController.getAllCustomers().get(0);
        Assertions.assertEquals(newCustomer.getName(), customerDTO.getName());
        Assertions.assertEquals(newCustomer.getId(), retrievedCustomer.getId());
        Assertions.assertTrue(retrievedCustomer.getId() > 0);
    }

    @Test
    public void testCreateEmployee(){
        EmployeeDTO employeeDTO = createEmployeeDTO();
        EmployeeDTO newEmployee = userController.saveEmployee(employeeDTO);
        EmployeeDTO retrievedEmployee = userController.getEmployee(newEmployee.getId());
        Assertions.assertEquals(employeeDTO.getSkills(), newEmployee.getSkills());
        Assertions.assertEquals(newEmployee.getId(), retrievedEmployee.getId());
        Assertions.assertTrue(retrievedEmployee.getId() > 0);
    }

    @Test
    public void testAddPetsToCustomer() {
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.saveCustomer(customerDTO);

        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(newCustomer.getId());
        PetDTO newPet = petController.savePet(petDTO);

        //make sure pet contains customer id
        PetDTO retrievedPet = petController.getPet(newPet.getId());
        Assertions.assertEquals(retrievedPet.getId(), newPet.getId());
        Assertions.assertEquals(retrievedPet.getOwnerId(), newCustomer.getId());

        //make sure you can retrieve pets by owner
        List<PetDTO> pets = petController.getPetsByOwner(newCustomer.getId());
        Assertions.assertEquals(newPet.getId(), pets.get(0).getId());
        Assertions.assertEquals(newPet.getName(), pets.get(0).getName());

        //check to make sure customer now also contains pet
        CustomerDTO retrievedCustomer = userController.getAllCustomers().get(0);
        Assertions.assertTrue(retrievedCustomer.getPetIds() != null && retrievedCustomer.getPetIds().size() > 0);
        Assertions.assertEquals(retrievedCustomer.getPetIds().get(0), retrievedPet.getId());
    }

    @Test
    public void testFindPetsByOwner() {
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.saveCustomer(customerDTO);

        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(newCustomer.getId());
        PetDTO newPet = petController.savePet(petDTO);
        petDTO.setType(PetType.DOG);
        petDTO.setName("DogName");
        PetDTO newPet2 = petController.savePet(petDTO);

        List<PetDTO> pets = petController.getPetsByOwner(newCustomer.getId());
        Assertions.assertEquals(pets.size(), 2);
        Assertions.assertEquals(pets.get(0).getOwnerId(), newCustomer.getId());
        Assertions.assertEquals(pets.get(0).getId(), newPet.getId());
    }

    @Test
    public void testFindOwnerByPet() {
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO newCustomer = userController.saveCustomer(customerDTO);

        PetDTO petDTO = createPetDTO();
        petDTO.setOwnerId(newCustomer.getId());
        PetDTO newPet = petController.savePet(petDTO);

        CustomerDTO owner = userController.getOwnerByPet(newPet.getId());
        Assertions.assertEquals(owner.getId(), newCustomer.getId());
        Assertions.assertEquals(owner.getPetIds().get(0), newPet.getId());
    }

    @Test
    public void testChangeEmployeeAvailability() {
        EmployeeDTO employeeDTO = createEmployeeDTO();
        EmployeeDTO emp1 = userController.saveEmployee(employeeDTO);
        Assertions.assertEquals(0, emp1.getDaysAvailable().size());

        Set<DayOfWeek> availability = Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        userController.setAvailability(availability, emp1.getId());

        EmployeeDTO emp2 = userController.getEmployee(emp1.getId());
        Assertions.assertEquals(availability, emp2.getDaysAvailable());
    }

    @Test
    public void testFindEmployeesByServiceAndTime() {
        EmployeeDTO emp1 = createEmployeeDTO();
        EmployeeDTO emp2 = createEmployeeDTO();
        EmployeeDTO emp3 = createEmployeeDTO();

        emp1.setDaysAvailable(Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));
        emp2.setDaysAvailable(Sets.newHashSet(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        emp3.setDaysAvailable(Sets.newHashSet(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        emp1.setSkills(Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.PETTING));
        emp2.setSkills(Sets.newHashSet(EmployeeSkill.PETTING, EmployeeSkill.WALKING));
        emp3.setSkills(Sets.newHashSet(EmployeeSkill.WALKING, EmployeeSkill.SHAVING));

        EmployeeDTO emp1n = userController.saveEmployee(emp1);
        EmployeeDTO emp2n = userController.saveEmployee(emp2);
        EmployeeDTO emp3n = userController.saveEmployee(emp3);

        //make a request that matches employee 1 or 2
        EmployeeRequestDTO er1 = new EmployeeRequestDTO();
        er1.setDate(LocalDate.of(2019, 12, 25)); //wednesday
        er1.setSkills(Sets.newHashSet(EmployeeSkill.PETTING));

        Set<Long> eIds1 = userController.findEmployeesForService(er1).stream().map(EmployeeDTO::getId).collect(Collectors.toSet());
        Set<Long> eIds1expected = Sets.newHashSet(emp1n.getId(), emp2n.getId());
        Assertions.assertEquals(eIds1, eIds1expected);

        //make a request that matches only employee 3
        EmployeeRequestDTO er2 = new EmployeeRequestDTO();
        er2.setDate(LocalDate.of(2019, 12, 27)); //friday
        er2.setSkills(Sets.newHashSet(EmployeeSkill.WALKING, EmployeeSkill.SHAVING));

        Set<Long> eIds2 = userController.findEmployeesForService(er2).stream().map(EmployeeDTO::getId).collect(Collectors.toSet());
        Set<Long> eIds2expected = Sets.newHashSet(emp3n.getId());
        Assertions.assertEquals(eIds2, eIds2expected);
    }

    @Test
    public void testSchedulePetsForServiceWithEmployee() {
        EmployeeDTO employeeTemp = createEmployeeDTO();
        employeeTemp.setDaysAvailable(Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));
        EmployeeDTO employeeDTO = userController.saveEmployee(employeeTemp);
        CustomerDTO customerDTO = userController.saveCustomer(createCustomerDTO());
        PetDTO petTemp = createPetDTO();
        petTemp.setOwnerId(customerDTO.getId());
        PetDTO petDTO = petController.savePet(petTemp);

        LocalDate date = LocalDate.of(2019, 12, 25);
        List<Long> petList = Lists.newArrayList(petDTO.getId());
        List<Long> employeeList = Lists.newArrayList(employeeDTO.getId());
        Set<EmployeeSkill> skillSet =  Sets.newHashSet(EmployeeSkill.PETTING);

        scheduleController.createSchedule(createScheduleDTO(petList, employeeList, date, skillSet));
        ScheduleDTO scheduleDTO = scheduleController.getAllSchedules().get(0);

        Assertions.assertEquals(scheduleDTO.getActivities(), skillSet);
        Assertions.assertEquals(scheduleDTO.getEmployeeIds(), employeeList);
        Assertions.assertEquals(scheduleDTO.getPetIds(), petList);
    }

    @Test
    public void testFindScheduleByEntities() {
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        ScheduleDTO sched2 = populateSchedule(3, 1, LocalDate.of(2019, 12, 26), Sets.newHashSet(EmployeeSkill.PETTING));

        //add a third schedule that shares some employees and pets with the other schedules
        ScheduleDTO sched3 = new ScheduleDTO();
        //sched3.setEmployeeIds(sched1.getEmployeeIds());
        sched3.setPetIds(sched2.getPetIds());
        sched3.setActivities(Sets.newHashSet(EmployeeSkill.SHAVING, EmployeeSkill.PETTING));
        sched3.setDate(LocalDate.of(2020, 3, 23));
        //Adjusting employee stats to avoid constraints conflicts when creating schedule
        EmployeeDTO employee = userController.getEmployee(sched1.getEmployeeIds().get(0));
        employee.getDaysAvailable().add(sched3.getDate().getDayOfWeek());
        employee.getSkills().add(EmployeeSkill.PETTING);
        sched3.setEmployeeIds(Lists.newArrayList(userController.updateEmployee(employee.getId(), employee).getId()));
        scheduleController.createSchedule(sched3);

        /*
            We now have 3 schedule entries. The third schedule entry has the same employees as the 1st schedule
            and the same pets/owners as the second schedule. So if we look up schedule entries for the employee from
            schedule 1, we should get both the first and third schedule as our result.
         */

        //Employee 1 in is both schedule 1 and 3
        List<ScheduleDTO> scheds1e = scheduleController.getScheduleForEmployee(sched1.getEmployeeIds().get(0));
        compareSchedules(sched1, scheds1e.get(0));
        compareSchedules(sched3, scheds1e.get(1));

        //Employee 2 is only in schedule 2
        List<ScheduleDTO> scheds2e = scheduleController.getScheduleForEmployee(sched2.getEmployeeIds().get(0));
        compareSchedules(sched2, scheds2e.get(0));

        //Pet 1 is only in schedule 1
        List<ScheduleDTO> scheds1p = scheduleController.getScheduleForPet(sched1.getPetIds().get(0));
        compareSchedules(sched1, scheds1p.get(0));

        //Pet from schedule 2 is in both schedules 2 and 3
        List<ScheduleDTO> scheds2p = scheduleController.getScheduleForPet(sched2.getPetIds().get(0));
        compareSchedules(sched2, scheds2p.get(0));
        compareSchedules(sched3, scheds2p.get(1));

        //Owner of the first pet will only be in schedule 1
        List<ScheduleDTO> scheds1c = scheduleController.getScheduleForCustomer(userController.getOwnerByPet(sched1.getPetIds().get(0)).getId());
        compareSchedules(sched1, scheds1c.get(0));

        //Owner of pet from schedule 2 will be in both schedules 2 and 3
        List<ScheduleDTO> scheds2c = scheduleController.getScheduleForCustomer(userController.getOwnerByPet(sched2.getPetIds().get(0)).getId());
        compareSchedules(sched2, scheds2c.get(0));
        compareSchedules(sched3, scheds2c.get(1));
    }

    @Test
    public void testDeletePet(){
        PetDTO pet = createPetDTO();
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO createdCustomer = userController.saveCustomer(customerDTO);
        pet.setOwnerId(createdCustomer.getId());
        PetDTO savedPet = petController.savePet(pet);
        Assertions.assertEquals(savedPet.getId(),petController.getPet(savedPet.getId()).getId());

        petController.deletePet(savedPet.getId());
        Assertions.assertThrows(NullPointerException.class, () -> petController.getPet(savedPet.getId()));
    }

    @Test
    public void testDeleteCustomer(){
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO savedCustomer = userController.saveCustomer(customerDTO);
        Assertions.assertEquals(savedCustomer.getId(), userController.getCustomer(savedCustomer.getId()).getId());

        userController.deleteCustomer(savedCustomer.getId());
        Assertions.assertEquals(0,userController.getAllCustomers().size());
    }

    @Test
    public void testDeleteEmployee(){
        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO.setDaysAvailable(Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        EmployeeDTO savedEmployee = userController.saveEmployee(employeeDTO);
        Assertions.assertEquals(savedEmployee.getId(), userController.getEmployee(savedEmployee.getId()).getId());

        userController.deleteEmployee(savedEmployee.getId());
        Assertions.assertEquals(0, userController.getAllEmployees().size());
    }

    @Test
    public void testDeleteSchedule(){
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        Assertions.assertEquals(sched1.getId(), scheduleController.getSchedule(sched1.getId()).getId());

        scheduleController.deleteSchedule(sched1.getId());
        Assertions.assertEquals(0, scheduleController.getAllSchedules().size());
    }

    @Test
    public void testUpdatePet(){
        PetDTO pet = createPetDTO();
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO createdCustomer = userController.saveCustomer(customerDTO);
        pet.setOwnerId(createdCustomer.getId());
        PetDTO savedPet = petController.savePet(pet);

        Assertions.assertEquals(savedPet.getId(), petController.getPet(savedPet.getId()).getId());

        //Creating another pet
        PetDTO pet2 = createPetDTO();
        pet2.setOwnerId(createdCustomer.getId());
        pet2.setType(PetType.BIRD);

        petController.updatePet(savedPet.getId(), pet2);
        //Testing if the type has changed
        Assertions.assertEquals(PetType.BIRD, petController.getPet(savedPet.getId()).getType());
        //Testing that the name doesn't change if it is not specified in pet2
        Assertions.assertEquals(savedPet.getName(),petController.getPet(savedPet.getId()).getName());
    }

    @Test
    public void testUpdateCustomer(){
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO savedCustomer = userController.saveCustomer(customerDTO);
        Assertions.assertEquals(savedCustomer.getId(), userController.getCustomer(savedCustomer.getId()).getId());

        //Creating another customer
        CustomerDTO customerDTO1 = createCustomerDTO();
        customerDTO1.setPhoneNumber("123-456-789");

        userController.updateCustomer(savedCustomer.getId(), customerDTO1);
        //Testing that the name doesn't change if it is not specified in customerDT01
        Assertions.assertEquals(savedCustomer.getName(), userController.getCustomer(savedCustomer.getId()).getName());
        //Testing if phone number changed
        Assertions.assertEquals("123-456-789", userController.getCustomer(savedCustomer.getId()).getPhoneNumber());
    }

    @Test
    public void testUpdateEmployee(){
        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO.setDaysAvailable(Sets.newHashSet(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        EmployeeDTO savedEmployee = userController.saveEmployee(employeeDTO);
        Assertions.assertEquals(savedEmployee.getId(), userController.getEmployee(savedEmployee.getId()).getId());

        //Creating another employee
        EmployeeDTO employeeDTO1 = createEmployeeDTO();
        employeeDTO1.setSkills(Sets.newHashSet(EmployeeSkill.MEDICATING, EmployeeSkill.SHAVING));
        employeeDTO1.setDaysAvailable(Sets.newHashSet(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY));

        userController.updateEmployee(savedEmployee.getId(), employeeDTO1);
        Assertions.assertEquals(employeeDTO1.getDaysAvailable(), userController.getEmployee(savedEmployee.getId()).getDaysAvailable());
        Assertions.assertEquals(employeeDTO1.getSkills(), userController.getEmployee(savedEmployee.getId()).getSkills());
    }

    @Test
    public void testUpdateSchedule(){
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        Assertions.assertEquals(sched1.getId(), scheduleController.getSchedule(sched1.getId()).getId());

        //Creating another schedule but not saving it in order to use it as a requestbody
        ScheduleDTO sched2 = createScheduleWithoutSaving(3, 1, LocalDate.of(2019, 12, 26), Sets.newHashSet(EmployeeSkill.PETTING));

        scheduleController.updateFullSchedule(sched1.getId(), sched2);
        Assertions.assertEquals(3,scheduleController.getSchedule(sched1.getId()).getEmployeeIds().size());
        Assertions.assertEquals(1,scheduleController.getSchedule(sched1.getId()).getPetIds().size());
        Assertions.assertEquals(Sets.newHashSet(EmployeeSkill.PETTING), scheduleController.getSchedule(sched1.getId()).getActivities());

        //Mixing up employees to check if constraints work
        ScheduleDTO sched3 = createScheduleWithoutSaving(2,1,LocalDate.of(2019,12,12),Sets.newHashSet(EmployeeSkill.SHAVING));
        sched3.setEmployeeIds(sched1.getEmployeeIds());
        Assertions.assertThrows(RuntimeException.class,() ->
                scheduleController.updateFullSchedule(sched1.getId(),sched3));
    }

    @Test
    public void testAddEmployeesToSchedule(){
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        Assertions.assertEquals(sched1.getId(), scheduleController.getSchedule(sched1.getId()).getId());
        Assertions.assertEquals(1,scheduleController.getSchedule(sched1.getId()).getEmployeeIds().size());

        EmployeeDTO employeeDTO = createEmployeeDTO();
        employeeDTO.setDaysAvailable(Sets.newHashSet(DayOfWeek.WEDNESDAY));

        EmployeeDTO employeeDTO1 = createEmployeeDTO();
        employeeDTO1.setDaysAvailable(Sets.newHashSet(DayOfWeek.WEDNESDAY));

        EmployeeDTO savedEmployee = userController.saveEmployee(employeeDTO);
        EmployeeDTO savedEmployee2 = userController.saveEmployee(employeeDTO1);

        List<Long> employeeIds = Lists.newArrayList(savedEmployee.getId(),savedEmployee2.getId());

        //employees to be added each have at least one skill that is required for the schedule
        //so they should be added without a problem and test should pass
        scheduleController.addEmployeesToSchedule(sched1.getId(),employeeIds);
        Assertions.assertEquals(3,scheduleController.getSchedule(sched1.getId()).getEmployeeIds().size());

        //Creating another employee which doesn't have any skills that are required for a schedule
        employeeDTO1.setSkills(Sets.newHashSet(EmployeeSkill.MEDICATING, EmployeeSkill.SHAVING));
        EmployeeDTO savedEmployee3 = userController.saveEmployee(employeeDTO1);

        //Trying to add this employee to our schedule which should result in an exception
        Assertions.assertThrows(RuntimeException.class, () ->
                scheduleController.addEmployeesToSchedule(sched1.getId(), Lists.newArrayList(savedEmployee3.getId())));
    }

    @Test void addEmployeesToScheduleAndCheckDays(){
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));

        EmployeeDTO employeeDTO = createEmployeeDTO();
        //Setting employee's available day which matches the schedule
        employeeDTO.setDaysAvailable(Sets.newHashSet(DayOfWeek.WEDNESDAY));
        EmployeeDTO savedEmployee = userController.saveEmployee(employeeDTO);
        List<Long> employeeIds = Lists.newArrayList(savedEmployee.getId());
        //Adding employee with available day that matches the schedule so should be added without a problem
        scheduleController.addEmployeesToSchedule(sched1.getId(), employeeIds);
        Assertions.assertEquals(2,scheduleController.getSchedule(sched1.getId()).getEmployeeIds().size());


        EmployeeDTO employeeDTO1 = createEmployeeDTO();
        employeeDTO1.setDaysAvailable(Sets.newHashSet(DayOfWeek.FRIDAY));
        EmployeeDTO savedEmployee2 = userController.saveEmployee(employeeDTO1);
        List<Long> employeeIds2 = Lists.newArrayList(savedEmployee.getId(),savedEmployee2.getId());
        //Adding employee with available day that don't match the schedule so should fail adding
        Assertions.assertThrows(RuntimeException.class,() ->
                scheduleController.addEmployeesToSchedule(sched1.getId(), employeeIds2));
        Assertions.assertEquals(2,scheduleController.getSchedule(sched1.getId()).getEmployeeIds().size());
    }

    @Test
    public void addEmployeesToScheduleAndCheckDaysAndTime(){
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        //Now setting start and end time on a schedule
        sched1.setStartTime(LocalTime.of(9,30));
        sched1.setEndTime(LocalTime.of(12,30));
        //updating sched1 with recent changes
        scheduleController.updateFullSchedule(sched1.getId(), sched1);

        EmployeeDTO employeeDTO = createEmployeeDTO();
        //Setting day available that match the schedule
        employeeDTO.setDaysAvailable(Sets.newHashSet(DayOfWeek.WEDNESDAY));
        //Setting the time available that matches the schedule
        employeeDTO.setAvailableTimes(Lists.newArrayList(LocalTime.of(9,35)));
        EmployeeDTO savedEmployee = userController.saveEmployee(employeeDTO);

        List<Long> employeeIds = Lists.newArrayList(savedEmployee.getId());
        scheduleController.addEmployeesToSchedule(sched1.getId(), employeeIds);

        //Employee should've been added so test should pass
        Assertions.assertEquals(2,scheduleController.getSchedule(sched1.getId()).getEmployeeIds().size());

        EmployeeDTO employeeDTO1 = createEmployeeDTO();
        //Setting day available that match the schedule
        employeeDTO1.setDaysAvailable(Sets.newHashSet(DayOfWeek.WEDNESDAY));
        //Setting the time available that doesn't match the schedule
        employeeDTO1.setAvailableTimes(Lists.newArrayList(LocalTime.of(8,35)));
        EmployeeDTO savedEmployee2 = userController.saveEmployee(employeeDTO1);
        List<Long> employeeIds2 = Lists.newArrayList(savedEmployee2.getId());
        //Employee's time available doesn't match the schedule so addition should fail
        Assertions.assertThrows(RuntimeException.class, () ->
                scheduleController.addEmployeesToSchedule(sched1.getId(), employeeIds2));
        Assertions.assertEquals(2,scheduleController.getSchedule(sched1.getId()).getEmployeeIds().size());

    }

    @Test
    public void testAddPetsToSchedule(){
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        Assertions.assertEquals(sched1.getId(), scheduleController.getSchedule(sched1.getId()).getId());
        Assertions.assertEquals(2,scheduleController.getSchedule(sched1.getId()).getPetIds().size());

        //Creating another pet and its owner
        PetDTO pet = createPetDTO();
        CustomerDTO customerDTO = createCustomerDTO();
        CustomerDTO createdCustomer = userController.saveCustomer(customerDTO);
        pet.setOwnerId(createdCustomer.getId());
        PetDTO savedPet = petController.savePet(pet);

        List<Long> petIds = Lists.newArrayList(savedPet.getId());

        scheduleController.addPetsToSchedule(sched1.getId(), petIds);

        Assertions.assertEquals(3,scheduleController.getSchedule(sched1.getId()).getPetIds().size());
    }

    @Test
    public void testAddActivitiesToSchedule(){
        ScheduleDTO sched1 = populateSchedule(1, 2, LocalDate.of(2019, 12, 25), Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        Assertions.assertEquals(sched1.getId(), scheduleController.getSchedule(sched1.getId()).getId());
        Assertions.assertEquals(2,scheduleController.getSchedule(sched1.getId()).getActivities().size());

        Set<EmployeeSkill> employeeSkills = Sets.newHashSet(EmployeeSkill.MEDICATING,EmployeeSkill.SHAVING);
        scheduleController.addActivitiesToSchedule(sched1.getId(),employeeSkills);

        Assertions.assertEquals(4,scheduleController.getSchedule(sched1.getId()).getActivities().size());
    }


    private static EmployeeDTO createEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setName("TestEmployee");
        employeeDTO.setSkills(Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.PETTING));
        return employeeDTO;
    }
    private static CustomerDTO createCustomerDTO() {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("TestEmployee");
        customerDTO.setPhoneNumber("123-456-789");
        return customerDTO;
    }

    private static PetDTO createPetDTO() {
        PetDTO petDTO = new PetDTO();
        petDTO.setName("TestPet");
        petDTO.setType(PetType.CAT);
        return petDTO;
    }

    private static EmployeeRequestDTO createEmployeeRequestDTO() {
        EmployeeRequestDTO employeeRequestDTO = new EmployeeRequestDTO();
        employeeRequestDTO.setDate(LocalDate.of(2019, 12, 25));
        employeeRequestDTO.setSkills(Sets.newHashSet(EmployeeSkill.FEEDING, EmployeeSkill.WALKING));
        return employeeRequestDTO;
    }

    private static ScheduleDTO createScheduleDTO(List<Long> petIds, List<Long> employeeIds,LocalDate date,Set<EmployeeSkill> activities) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setPetIds(petIds);
        scheduleDTO.setEmployeeIds(employeeIds);
        scheduleDTO.setDate(date);
        scheduleDTO.setActivities(activities);
        return scheduleDTO;
    }

    private ScheduleDTO populateSchedule(int numEmployees, int numPets,LocalDate date,Set<EmployeeSkill> activities) {
        List<Long> employeeIds = IntStream.range(0, numEmployees)
                .mapToObj(i -> createEmployeeDTO())
                .map(e -> {
                    e.setSkills(activities);
                    e.setDaysAvailable(Sets.newHashSet(date.getDayOfWeek()));
                    return userController.saveEmployee(e).getId();
                }).collect(Collectors.toList());
        CustomerDTO cust = userController.saveCustomer(createCustomerDTO());
        List<Long> petIds = IntStream.range(0, numPets)
                .mapToObj(i -> createPetDTO())
                .map(p -> {
                    p.setOwnerId(cust.getId());
                    return petController.savePet(p).getId();
                }).collect(Collectors.toList());
        return scheduleController.createSchedule(createScheduleDTO(petIds, employeeIds,date, activities));
    }

    private ScheduleDTO createScheduleWithoutSaving(int numEmployees, int numPets,LocalDate date, Set<EmployeeSkill> activities){
        List<Long> employeeIds = IntStream.range(0, numEmployees)
                .mapToObj(i -> createEmployeeDTO())
                .map(e -> {
                    e.setSkills(activities);
                    e.setDaysAvailable(Sets.newHashSet(date.getDayOfWeek()));
                    return userController.saveEmployee(e).getId();
                }).collect(Collectors.toList());
        CustomerDTO cust = userController.saveCustomer(createCustomerDTO());
        List<Long> petIds = IntStream.range(0, numPets)
                .mapToObj(i -> createPetDTO())
                .map(p -> {
                    p.setOwnerId(cust.getId());
                    return petController.savePet(p).getId();
                }).collect(Collectors.toList());
        return createScheduleDTO(petIds, employeeIds,date,activities);
    }

    private static void compareSchedules(ScheduleDTO sched1, ScheduleDTO sched2) {
        Assertions.assertEquals(sched1.getPetIds(), sched2.getPetIds());
        Assertions.assertEquals(sched1.getActivities(), sched2.getActivities());
        Assertions.assertEquals(sched1.getEmployeeIds(), sched2.getEmployeeIds());
        Assertions.assertEquals(sched1.getDate(),sched2.getDate());
        Assertions.assertEquals(sched1.getStartTime(), sched2.getStartTime());
        Assertions.assertEquals(sched1.getEndTime(),sched1.getEndTime());
    }

}
