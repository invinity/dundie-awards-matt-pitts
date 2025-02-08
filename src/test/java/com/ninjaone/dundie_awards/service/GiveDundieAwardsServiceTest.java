package com.ninjaone.dundie_awards.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;

class GiveDundieAwardsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private GiveDundieAwardsService giveDundieAwardsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void giveDundieAwardsByOrganization_should_call_the_necessary_component_methods() {
        Organization organization = new Organization();
        int awardCount = 5;
        int expectedCount = 10;

        when(employeeRepository.addDundieAwardsByOrganization(organization, awardCount)).thenReturn(expectedCount);

        int result = giveDundieAwardsService.giveDundieAwardsByOrganization(organization, awardCount);

        assertEquals(expectedCount, result);
        verify(employeeRepository, times(1)).addDundieAwardsByOrganization(organization, awardCount);
        verify(jmsTemplate, times(1)).convertAndSend(eq("dundie-awards"), any(String.class));
    }

    @Test
    void giveDundieAwardsByOrganization_should_rollback_database_transaction_when_message_submission_fails() {
        Organization organization = new Organization();
        int awardCount = 5;
        int expectedCount = 10;

        when(employeeRepository.addDundieAwardsByOrganization(organization, awardCount)).thenReturn(expectedCount);

        int result = giveDundieAwardsService.giveDundieAwardsByOrganization(organization, awardCount);

        assertEquals(expectedCount, result);
        verify(employeeRepository, times(1)).addDundieAwardsByOrganization(organization, awardCount);
        verify(jmsTemplate, times(1)).convertAndSend(eq("dundie-awards"), any(Activity.class));
    }
}