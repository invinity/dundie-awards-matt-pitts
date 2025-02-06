package com.ninjaone.dundie_awards.controller.organization;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ninjaone.dundie_awards.controller.organization.OrganizationEmployeeAction.GiveDundieAwardsAction;
import com.ninjaone.dundie_awards.controller.organization.OrganizationEmployeeActionResult.GiveDundieAwardsResult;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Controller
@RequestMapping("/organizations")
@lombok.RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganizationController {
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/{id}/employees")
    @ResponseBody
    @Operation(summary = "Get all employees in an organization",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Employees retrieved successfully",
                            content = {
                                    @Content(schema = @Schema(implementation = Employee.class))}),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The given organization could not be found",
                            content = @Content),
                    @ApiResponse(responseCode = "500",
                            description = "An unexpected error occurred server-side while processing the request",
                            content = @Content)})
    public ResponseEntity<List<Employee>> getEmployees(@PathVariable Long id) {
        return organizationRepository.findById(id).map(employeeRepository::findByOrganization)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Perform an organization-wide action on all employees in the organization.
     * 
     * @param employee
     * @return
     */
    @PostMapping("/{id}/employees")
    @ResponseBody
    @Operation(summary = "Perform organization-wide employee actions",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
                    @Content(
                        schema = @Schema(implementation = GiveDundieAwardsAction.class), 
                        examples = {
                                @ExampleObject(
                                   name = "GiveDundieAwardsAction",
                                   value = "{\"type\": \"GiveDundieAwardsAction\", \"awardCount\": 5}")})}),
            responses = {@ApiResponse(responseCode = "200",
                    description = "Employee action performed successfully",
                    content = {@Content(
                            schema = @Schema(implementation = GiveDundieAwardsResult.class))}),
                    @ApiResponse(responseCode = "400",
                            description = "The given request body could not be processed as a valid action",
                            content = @Content),
                    @ApiResponse(responseCode = "404",
                            description = "The given organization could not be found",
                            content = @Content),
                    @ApiResponse(responseCode = "500",
                            description = "An unexpected error occurred server-side while processing the request",
                            content = @Content)})
    public ResponseEntity<OrganizationEmployeeActionResult> performEmployeeAction(
            @PathVariable Long id, @RequestBody OrganizationEmployeeAction action) {
        return organizationRepository.findById(id)
                .map(org -> performOrganizationAction(org, action))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Perform organization action as appropriate for the given action and produce a response
     * entity.
     * 
     * @param organization
     * @param action
     * @return
     */
    private ResponseEntity<OrganizationEmployeeActionResult> performOrganizationAction(
            Organization organization, OrganizationEmployeeAction action) {
        if (action instanceof GiveDundieAwardsAction) {
            GiveDundieAwardsAction giveDundieAwards = (GiveDundieAwardsAction) action;
            int updateCount = employeeRepository.addDundieAwardsByOrganization(organization,
                    giveDundieAwards.getAwardCount());
            return ResponseEntity
                    .ok(GiveDundieAwardsResult.builder().employeeUpdateCount(updateCount).build());
        }

        return ResponseEntity.badRequest().build();
    }
}
