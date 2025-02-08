package com.ninjaone.dundie_awards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jms.core.JmsTemplate;

import com.ninjaone.dundie_awards.AwardsCache;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
// @ContextConfiguration(classes = TransactionManagementConfig.class)
// @AutoConfigurationPackage(basePackages = "com.ninjaone.dundie_awards")
class GiveDundieAwardsServiceTest {

    @Mock
    private EmployeeRepository mockEmployeeRepository;

    @Autowired
    private EmployeeRepository realEmployeeRepository;

    @Mock
    private ActivityRepository mockActivityRepository;

    private AwardsCache awardsCache = new AwardsCache();

    @Mock
    private JmsTemplate mockJmsTemplate;

    @InjectMocks
    private GiveDundieAwardsService giveDundieAwardsService;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization1, organization2;

    @BeforeEach
    void setUp() {
        organization1 = organizationRepository.save(Organization.builder().name("Some Organization").build());
        organization2 = organizationRepository.save(Organization.builder().name("Another Organization").build());
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> createTestEmployee(organization1, i))
                .forEach(realEmployeeRepository::save);

        IntStream.rangeClosed(11, 15)
                .mapToObj(i -> createTestEmployee(organization2, i))
                .forEach(realEmployeeRepository::save);
    }

    @Test
    void giveDundieAwardsByOrganization_should_call_the_necessary_component_methods() {
        Organization organization = new Organization();
        int awardCount = 5;
        int expectedCount = 10;

        when(mockEmployeeRepository.addDundieAwardsByOrganization(organization, awardCount)).thenReturn(expectedCount);

        int result = giveDundieAwardsService.giveDundieAwardsByOrganization(organization, awardCount);

        assertEquals(expectedCount, result);
        verify(mockEmployeeRepository, times(1)).addDundieAwardsByOrganization(organization, awardCount);
        verify(mockJmsTemplate, times(1)).convertAndSend(eq(GiveDundieAwardsService.DUNDIE_MESSAGES_QUEUE),
                any(GiveDundieAwardsService.AwardsGivenMessage.class));
    }

    // @Test
    // void giveDundieAwardsByOrganization_should_rollback_database_transaction_when_message_submission_fails() {
    //     // simulate an error when submitting the JMS message
    //     doThrow(new RuntimeException("JMS error")).when(mockJmsTemplate).convertAndSend(eq(GiveDundieAwardsService.DUNDIE_MESSAGES_QUEUE),
    //             any(GiveDundieAwardsService.AwardsGivenMessage.class));
    //             GiveDundieAwardsService underTest = new GiveDundieAwardsService(realEmployeeRepository, mockActivityRepository, awardsCache, mockJmsTemplate);
    //     Map<Employee, Integer> originalOrg1Counts = realEmployeeRepository.findByOrganization(organization1).stream()
    //             .collect(Collectors.toMap(e -> e, Employee::getDundieAwards));
    //     assertThat(originalOrg1Counts, aMapWithSize(10));
    //     int awardCount = 5;
    //     RuntimeException thrown = assertThrows(RuntimeException.class, () -> underTest.giveDundieAwardsByOrganization(organization1, awardCount));
    //     Map<Employee, Integer> newOrg1Counts = realEmployeeRepository.findByOrganization(organization1).stream()
    //             .collect(Collectors.toMap(e -> e, Employee::getDundieAwards));
    //     assertThat(newOrg1Counts, is(originalOrg1Counts));
    // }

    static Employee createTestEmployee(Organization organization, int index) {
        return Employee.builder()
                .organization(organization)
                .firstName("Person" + index)
                .lastName("Person" + index)
                .dundieAwards(index)
                .build();
    }
}