package com.udacity.jdnd.course3.critter.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.entity.User;
import com.udacity.jdnd.course3.critter.service.PetService;
import com.udacity.jdnd.course3.critter.service.UserService;
import com.udacity.jdnd.course3.critter.user.CustomerDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeRequestDTO;
import com.udacity.jdnd.course3.critter.view.Views;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Users.
 *
 * Includes requests for both customers and employees. Splitting this into separate user and customer controllers
 * would be fine too, though that is not part of the required scope for this class.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PetService petService;

    @Autowired
    UserController(UserService userService, PetService petService) {
        this.userService = userService;
        this.petService = petService;
    }

    @PostMapping("/customer")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customerDTO){
        //Saving customer
        Customer savedCustomer = (Customer) userService.saveUser(convertCustomerDTOtoCustomer(customerDTO));
        return convertCustomerToCustomerDTO(savedCustomer);
    }

    @GetMapping("/customer/{customerId}")
    public CustomerDTO getCustomer(@PathVariable Long customerId){
        return convertCustomerToCustomerDTO((Customer)userService.findCustomerById(customerId));
    }

    @GetMapping("/employee/{employeeId}")
    public EmployeeDTO getEmployee(@PathVariable Long employeeId){
        return convertEmployeeToEmployeeDTO((Employee)userService.findEmployeeById(employeeId));
    }

    @GetMapping("/customer")
    public List<CustomerDTO> getAllCustomers(){
        List<CustomerDTO> allCustomers =
                userService.findAllCustomers()
                        .stream()
                        .map(user -> convertCustomerToCustomerDTO((Customer)user))
                        .collect(Collectors.toList());
        return allCustomers;
    }

    @GetMapping("/customer/pet/{petId}")
    public CustomerDTO getOwnerByPet(@PathVariable long petId){
        Pet pet = petService.findPetById(petId);
        Customer customer = (Customer) userService.findUserById(pet.getCustomer().getId());
        return convertCustomerToCustomerDTO(customer);
    }

    @DeleteMapping("/customer/delete/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId){
        userService.deleteUser(customerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/customer/update/{customerId}")
    public CustomerDTO updateCustomer(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO){
        customerDTO.setId(customerId);
        return convertCustomerToCustomerDTO(userService.updateCustomer(convertCustomerDTOtoCustomer(customerDTO)));
    }

    @PostMapping("/employee")
    public EmployeeDTO saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        User savedEmployee = userService.saveUser(convertEmployeeDTOtoEmployee(employeeDTO));
        return convertEmployeeToEmployeeDTO((Employee) userService.findUserById(savedEmployee.getId()));
    }

    @GetMapping("/employee")
    public List<EmployeeDTO> getAllEmployees(){
        List<EmployeeDTO> allEmployees =
                userService.findAllEmployees()
                .stream()
                .map(employee -> convertEmployeeToEmployeeDTO(employee))
                .collect(Collectors.toList());
        return allEmployees;
    }

    @PostMapping("/employee/{employeeId}")
    public EmployeeDTO getEmployee(@PathVariable long employeeId) {
        Employee employee = (Employee) userService.findUserById(employeeId);
        return convertEmployeeToEmployeeDTO(employee);
    }

    @PutMapping("/employee/{employeeId}")
    public void setAvailability(@RequestBody Set<DayOfWeek> daysAvailable, @PathVariable long employeeId) {
        Employee employee = (Employee) userService.findUserById(employeeId);
        employee.setDaysAvailable(daysAvailable);
    }

    @JsonView(Views.Public.class)
    @GetMapping("/getAllUsers")
    public List<User> getAllUsers(){
        return userService.findAllUsers();
    }

    @GetMapping("/employee/availability")
    public List<EmployeeDTO> findEmployeesForService(@RequestBody EmployeeRequestDTO employeeRequestDTO){
        return userService.findEmployeesForSchedule(employeeRequestDTO.getSkills(),employeeRequestDTO.getDate())
                .stream()
                .map(UserController::convertEmployeeToEmployeeDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/employee/delete/{employeeId}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long employeeId){
        userService.deleteUser(employeeId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/employee/update/{employeeId}")
    public EmployeeDTO updateEmployee(@PathVariable Long employeeId, @Valid @RequestBody EmployeeDTO employeeDTO){
        employeeDTO.setId(employeeId);
        return convertEmployeeToEmployeeDTO(userService.updateEmployee(convertEmployeeDTOtoEmployee(employeeDTO)));
    }


    private static Customer convertCustomerDTOtoCustomer(CustomerDTO customerDTO){
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO,customer);
        return customer;
    }

    private static CustomerDTO convertCustomerToCustomerDTO(Customer customer){
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer,customerDTO);
        if(!customer.getPets().isEmpty()){
            customer.getPets().forEach(pet -> customerDTO.getPetIds().add(pet.getId()));
        }
        return customerDTO;
    }

    private static Employee convertEmployeeDTOtoEmployee(EmployeeDTO employeeDTO){
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        return employee;
    }

    private static EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee){
        EmployeeDTO employeeDTO = new EmployeeDTO();
        BeanUtils.copyProperties(employee,employeeDTO);
        return employeeDTO;
    }

}
