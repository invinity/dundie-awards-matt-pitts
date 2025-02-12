package com.ninjaone.dundie_awards.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@Controller
@RequestMapping("/employees")
@lombok.RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    /**
     * Get all employees
     * 
     * @return
     */
    @GetMapping
    @ResponseBody
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Create employee
     * 
     * @param employee
     * @return
     */
    @PostMapping
    @ResponseBody
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Get employee by id
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ResponseBody
    @Operation(summary = "Get an employee by ID",
            parameters = {@Parameter(name = "id", description = "The ID of the employee to retrieve",
                    in = ParameterIn.PATH, required = true)},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Employee retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Employee.class))),
                    @ApiResponse(responseCode = "404",
                            description = "The given employee could not be found")})
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") Long id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isPresent()) {
            return ResponseEntity.ok(optionalEmployee.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Update employee by id
     * 
     * @param id
     * @param employeeDetails
     * @return
     */
    @PutMapping("/{id}")
    @ResponseBody
    @Operation(summary = "Update an employee by ID",
            parameters = {@Parameter(name = "id", description = "The ID of the employee to update",
                    in = ParameterIn.PATH, required = true)},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = Employee.class))),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Employee updated successfully"),
                    @ApiResponse(responseCode = "404",
                            description = "The given employee could not be found")})
    public ResponseEntity<Employee> updateEmployee(@PathVariable("id") Long id,
            @RequestBody Employee employeeDetails) {
        return employeeRepository.findById(id).map(existing -> {
            existing.setFirstName(employeeDetails.getFirstName());
            existing.setLastName(employeeDetails.getLastName());
            existing.setOrganization(employeeDetails.getOrganization());
            existing.setDundieAwards(employeeDetails.getDundieAwards());
            return ResponseEntity.ok(employeeRepository.save(existing));
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Delete employee by id
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    @Operation(summary = "Delete an employee by ID",
            parameters = {@Parameter(name = "id", description = "The ID of the employee to delete",
                    in = ParameterIn.PATH, required = true)},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Employee deleted successfully"),
                    @ApiResponse(responseCode = "404",
                            description = "The given employee could not be found")})
    public ResponseEntity<Map<String, Boolean>> deleteEmployee(@PathVariable("id") Long id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (!optionalEmployee.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Employee employee = optionalEmployee.get();
        employeeRepository.delete(employee);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}
