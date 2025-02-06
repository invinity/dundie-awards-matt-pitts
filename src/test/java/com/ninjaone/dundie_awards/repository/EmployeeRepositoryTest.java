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

import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization1, organization2;

    @BeforeEach
    void setUp() {
        organization1 = organizationRepository.save(Organization.builder().name("Some Organization").build());
        organization2 = organizationRepository.save(Organization.builder().name("Another Organization").build());
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> createTestEmployee(organization1, i))
                .forEach(employeeRepository::save);

        IntStream.rangeClosed(11, 15)
                .mapToObj(i -> createTestEmployee(organization2, i))
                .forEach(employeeRepository::save);
    }

    @Test
    void addDundieAwardsByOrganization_should_add_dundie_awards_only_to_the_specified_organization() {
        Map<Employee, Integer> currentOrg1Counts = employeeRepository.findByOrganization(organization1).stream()
                .collect(Collectors.toMap(e -> e, Employee::getDundieAwards));
        assertThat(currentOrg1Counts, aMapWithSize(10));
        Map<Employee, Integer> currentOrg2Counts = employeeRepository.findByOrganization(organization2).stream()
                .collect(Collectors.toMap(e -> e, Employee::getDundieAwards));
        assertThat(currentOrg2Counts, aMapWithSize(5));
        int awardCount = 5;
        int updateCount = employeeRepository.addDundieAwardsByOrganization(organization1, awardCount);
        assertThat(updateCount, is(10));
        Map<Employee, Integer> newOrg1Counts = employeeRepository.findByOrganization(organization1).stream()
                .collect(Collectors.toMap(e -> e, Employee::getDundieAwards));
        Map<Employee, Integer> newOrg2Counts = employeeRepository.findByOrganization(organization2).stream()
                .collect(Collectors.toMap(e -> e, Employee::getDundieAwards));
        currentOrg1Counts.forEach((employee, count) -> assertThat(newOrg1Counts.get(employee), is(count + awardCount)));
        currentOrg2Counts.forEach((employee, count) -> assertThat(newOrg2Counts.get(employee), is(count)));
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