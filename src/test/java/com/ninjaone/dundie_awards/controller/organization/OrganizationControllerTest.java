package com.ninjaone.dundie_awards.controller.organization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.controller.organization.OrganizationEmployeeAction.GiveDundieAwardsAction;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.GiveDundieAwardsService;

@WebMvcTest(OrganizationController.class)
public class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganizationRepository organizationRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private GiveDundieAwardsService giveDundieAwardsService;

    @InjectMocks
    private OrganizationController organizationController;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getEmployees_should_return_employees_json_when_request_is_valid() throws Exception {
        Organization organization = new Organization();
        organization.setId(1L);

        Employee employee = new Employee();
        employee.setId(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(employeeRepository.findByOrganization(organization))
                .thenReturn(Collections.singletonList(employee));

        mockMvc.perform(get("/organizations/1/employees")).andExpect(status().isOk());
    }

    @Test
    public void getEmployees_should_return_a_404_response_when_the_employee_is_not_found() throws Exception {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/organizations/1/employees")).andExpect(status().isNotFound());
    }

    @Test
    public void performEmployeeAction_should_return_a_success_response_when_the_request_is_valid() throws Exception {
        Organization organization = new Organization();
        organization.setId(1L);

        GiveDundieAwardsAction action = new GiveDundieAwardsAction();
        action.setAwardCount(5);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(giveDundieAwardsService.giveDundieAwardsByOrganization(any(Organization.class),
                any(Integer.class))).thenReturn(5);

        mockMvc.perform(post("/organizations/1/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(action))).andExpect(status().isOk());
    }

    @Test
    public void performEmployeeAction_should_return_404_response_when_the_organization_is_not_found() throws Exception {
        GiveDundieAwardsAction action = new GiveDundieAwardsAction();
        action.setAwardCount(5);

        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/organizations/1/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(action))).andExpect(status().isNotFound());
    }

    @Test
    public void performEmployeeAction_should_return_a_400_response_when_the_request_is_invalid() throws Exception {
        Organization organization = new Organization();
        organization.setId(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

        mockMvc.perform(post("/organizations/1/employees").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
    }
}
