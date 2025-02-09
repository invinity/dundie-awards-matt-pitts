package com.ninjaone.dundie_awards.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Organization organization1, organization2;

    @BeforeEach
    void setUp() {
        organization1 = organizationRepository.saveAndFlush(Organization.builder().name("Some Organization").build());
        organization2 = organizationRepository.saveAndFlush(Organization.builder().name("Another Organization").build());
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> createTestEmployee(organization1, i))
                .forEach(employeeRepository::saveAndFlush);

        IntStream.rangeClosed(11, 15)
                .mapToObj(i -> createTestEmployee(organization2, i))
                .forEach(employeeRepository::saveAndFlush);
    }

    @Test
    void addDundieAwardsByOrganization_should_add_dundie_awards_only_to_the_specified_organization() {
        Map<Employee, Integer> originalOrg1Counts = getEmployeeAwardCounts(organization1);
        assertThat(originalOrg1Counts, aMapWithSize(10));
        Map<Employee, Integer> originalOrg2Counts = getEmployeeAwardCounts(organization2);
        assertThat(originalOrg2Counts, aMapWithSize(5));
        int awardCount = 5;
        Integer updateCount = new TransactionTemplate(transactionManager).execute(status -> employeeRepository.addDundieAwardsByOrganization(organization1, awardCount));
        assertThat(updateCount, is(10));
        Map<Employee, Integer> newOrg1Counts = getEmployeeAwardCounts(organization1);
        Map<Employee, Integer> newOrg2Counts = getEmployeeAwardCounts(organization2);
        originalOrg1Counts.forEach((employee, count) -> assertThat(newOrg1Counts.get(employee), is(count + awardCount)));
        originalOrg2Counts.forEach((employee, count) -> assertThat(newOrg2Counts.get(employee), is(count)));
    }

    Map<Employee, Integer> getEmployeeAwardCounts(Organization organization) {
        return employeeRepository.findByOrganization(organization).stream()
                .collect(Collectors.toMap(e -> e, Employee::getDundieAwards));
    }

    static Employee createTestEmployee(Organization organization, int index) {
        return Employee.builder()
                .organization(organization)
                .firstName("Person" + index)
                .lastName("Person" + index)
                .dundieAwards(index)
                .build();
    }
}