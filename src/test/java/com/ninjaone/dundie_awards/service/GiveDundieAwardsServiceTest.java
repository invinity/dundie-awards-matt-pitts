package com.ninjaone.dundie_awards.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.ninjaone.dundie_awards.AwardsCache;
import com.ninjaone.dundie_awards.config.TransactionManagementConfig;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
@ContextConfiguration(classes = TransactionManagementConfig.class)
@AutoConfigurationPackage(basePackages = "com.ninjaone.dundie_awards")
class GiveDundieAwardsServiceTest {
    @Mock
    private PlatformTransactionManager mockTransactionManager;

    @Mock
    private EmployeeRepository mockEmployeeRepository;

    @Mock
    private ActivityRepository mockActivityRepository;

    private AwardsCache awardsCache = new AwardsCache();

    @Mock
    private JmsTemplate mockJmsTemplate;

    @Autowired
    private EntityManager entityManager;


    @Autowired
    private EmployeeRepository realEmployeeRepository;

    @Autowired
    private OrganizationRepository realOrganizationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Organization organization1, organization2;

    @BeforeEach
    void setUp() {
        organization1 = realOrganizationRepository
                .saveAndFlush(Organization.builder().name("Some Organization").build());
        organization2 = realOrganizationRepository
                .saveAndFlush(Organization.builder().name("Another Organization").build());
        IntStream.rangeClosed(1, 10).mapToObj(i -> createTestEmployee(organization1, i))
                .forEach(realEmployeeRepository::saveAndFlush);

        IntStream.rangeClosed(11, 15).mapToObj(i -> createTestEmployee(organization2, i))
                .forEach(realEmployeeRepository::saveAndFlush);
    }

    @Test
    void giveDundieAwardsByOrganization_should_call_the_necessary_component_methods() {
        Organization organization = Organization.builder().id(1L).name("Some Organization").build();
        int awardCount = 5;
        int expectedCount = 10;
        when(mockEmployeeRepository.addDundieAwardsByOrganization(eq(organization), eq(awardCount)))
                .thenReturn(expectedCount);
        GiveDundieAwardsService underTest = new GiveDundieAwardsService(transactionTemplate, mockEmployeeRepository,
                mockActivityRepository, awardsCache, mockJmsTemplate);
        Integer result = underTest.giveDundieAwardsByOrganization(organization, awardCount);
        assertEquals(expectedCount, result);
        verify(mockEmployeeRepository, times(1)).addDundieAwardsByOrganization(eq(organization),
                eq(awardCount));
        verify(mockJmsTemplate, times(1)).convertAndSend(
                eq(GiveDundieAwardsService.DUNDIE_MESSAGES_QUEUE),
                any(GiveDundieAwardsService.AwardsGivenMessage.class));
    }

    @Test
    void giveDundieAwardsByOrganization_should_rollback_database_transaction_when_message_submission_fails() {
        // simulate an error when submitting the JMS message
        doThrow(new RuntimeException("JMS error")).when(mockJmsTemplate).convertAndSend(
                eq(GiveDundieAwardsService.DUNDIE_MESSAGES_QUEUE),
                any(GiveDundieAwardsService.AwardsGivenMessage.class));
        GiveDundieAwardsService underTest = new GiveDundieAwardsService(transactionTemplate, realEmployeeRepository,
                mockActivityRepository, awardsCache, mockJmsTemplate);
        Map<Long, Integer> originalOrg1Counts = getEmployeeAwardCounts(organization1);
        assertThat(originalOrg1Counts, aMapWithSize(10));
        int awardCount = 5;
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> underTest.giveDundieAwardsByOrganization(organization1, awardCount));
        assertThat(thrown.getMessage(), is("JMS error"));
        Map<Long, Integer> newOrg1Counts = getEmployeeAwardCounts(organization1);
        assertThat(newOrg1Counts, is(originalOrg1Counts));
    }

    @Test
    void giveDundieAwardsByOrganization_should_add_dundie_awards_only_to_the_specified_organization() {
        Map<Long, Integer> originalOrg1Counts = getEmployeeAwardCounts(organization1);
        assertThat(originalOrg1Counts, aMapWithSize(10));
        Map<Long, Integer> originalOrg2Counts = getEmployeeAwardCounts(organization2);
        assertThat(originalOrg2Counts, aMapWithSize(5));
        int awardCount = 5;
        GiveDundieAwardsService underTest = new GiveDundieAwardsService(transactionTemplate, realEmployeeRepository,
                mockActivityRepository, awardsCache, mockJmsTemplate);
        Integer updateCount = underTest.giveDundieAwardsByOrganization(organization1, awardCount);
        assertThat(updateCount, is(10));
        Map<Long, Integer> newOrg1Counts = getEmployeeAwardCounts(organization1);
        Map<Long, Integer> newOrg2Counts = getEmployeeAwardCounts(organization2);
        originalOrg1Counts.forEach((employee, count) -> assertThat(newOrg1Counts.get(employee),
                is(count + awardCount)));
        originalOrg2Counts
                .forEach((employee, count) -> assertThat(newOrg2Counts.get(employee), is(count)));
    }


    Map<Long, Integer> getEmployeeAwardCounts(Organization organization) {
        return realEmployeeRepository.findByOrganization(organization).stream()
        .map(e -> {
            entityManager.refresh(e);
            return e;
        })
        .collect(Collectors.toMap(Employee::getId, Employee::getDundieAwards));
    }

    static Employee createTestEmployee(Organization organization, int index) {
        return Employee.builder().organization(organization).firstName("Person" + index)
                .lastName("Person" + index).dundieAwards(index).build();
    }
}
