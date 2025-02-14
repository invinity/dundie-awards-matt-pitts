package com.ninjaone.dundie_awards.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import jakarta.jms.JMSException;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class GiveDundieAwardsServiceTest {
    @MockBean
    private JmsTemplate mockJmsTemplate;

    @Mock
    private ObjectMessage mockObjectMessage;

    @Mock
    private TextMessage mockTextMessage;

    @Autowired
    private EmployeeRepository realEmployeeRepository;

    @Autowired
    private OrganizationRepository realOrganizationRepository;

    @Autowired
    private GiveDundieAwardsService underTest;

    private Organization organization1, organization2;

    @BeforeEach
    void setUp() {
        realEmployeeRepository.deleteAll();
        realOrganizationRepository.deleteAll();
        organization1 = realOrganizationRepository.saveAndFlush(Organization.builder().name("Some Organization").build());
        organization2 = realOrganizationRepository.saveAndFlush(Organization.builder().name("Another Organization").build());
        IntStream.rangeClosed(1, 10).mapToObj(i -> createTestEmployee(organization1, i))
                .forEach(realEmployeeRepository::saveAndFlush);

        IntStream.rangeClosed(11, 15).mapToObj(i -> createTestEmployee(organization2, i))
                .forEach(realEmployeeRepository::saveAndFlush);
    }

    @Test
    void giveDundieAwardsByOrganization_should_rollback_database_transaction_when_message_submission_fails() {
        // simulate an error when submitting the JMS message
        doThrow(new RuntimeException("JMS error")).when(mockJmsTemplate).convertAndSend(
                eq(GiveDundieAwardsService.DUNDIE_MESSAGES_QUEUE),
                any(GiveDundieAwardsService.AwardsGivenMessage.class));
        Map<Long, Integer> originalOrg1Counts = realEmployeeRepository.getEmployeeAwardCountsMapByOrganization(organization1);
        assertThat(originalOrg1Counts, aMapWithSize(10));
        int awardCount = 5;
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> underTest.giveDundieAwardsByOrganization(organization1, awardCount));
        assertThat(thrown.getMessage(), is("JMS error"));
        Map<Long, Integer> newOrg1Counts = realEmployeeRepository.getEmployeeAwardCountsMapByOrganization(organization1);
        assertThat(newOrg1Counts, is(originalOrg1Counts));
    }

    @Test
    void giveDundieAwardsByOrganization_should_add_dundie_awards_only_to_the_specified_organization() {
        Map<Long, Integer> originalOrg1Counts = realEmployeeRepository.getEmployeeAwardCountsMapByOrganization(organization1);
        assertThat(originalOrg1Counts, aMapWithSize(10));
        Map<Long, Integer> originalOrg2Counts = realEmployeeRepository.getEmployeeAwardCountsMapByOrganization(organization2);
        assertThat(originalOrg2Counts, aMapWithSize(5));
        int awardCount = 5;
        Integer updateCount = underTest.giveDundieAwardsByOrganization(organization1, awardCount);
        assertThat(updateCount, is(10));
        Map<Long, Integer> newOrg1Counts = realEmployeeRepository.getEmployeeAwardCountsMapByOrganization(organization1);
        Map<Long, Integer> newOrg2Counts = realEmployeeRepository.getEmployeeAwardCountsMapByOrganization(organization2);
        originalOrg1Counts.forEach((employee, count) -> assertThat(newOrg1Counts.get(employee),
                is(count + awardCount)));
        originalOrg2Counts
                .forEach((employee, count) -> assertThat(newOrg2Counts.get(employee), is(count)));
    }

    @Test
    void receiveDundieMessage_should_process_valid_message() throws JMSException {
        GiveDundieAwardsService.AwardsGivenMessage agMessage = GiveDundieAwardsService.AwardsGivenMessage.builder()
                        .awardCount(5)
                        .affectedEmployeeCount(10)
                        .thread("test-thread")
                        .build();
        when(mockObjectMessage.getBody(GiveDundieAwardsService.AwardsGivenMessage.class)).thenReturn(agMessage);

        underTest.receiveDundieMessage(mockObjectMessage);

        // verify(mockActivityRepository, times(1)).save(any(Activity.class));
        // assertThat(awardsCache.getTotalAwards(), is(50L));
    }

    @Test
    void receiveDundieMessage_should_not_process_invalid_message() throws JMSException {
        underTest.receiveDundieMessage(mockTextMessage);

        // verify(mockActivityRepository, never()).save(any(Activity.class));
        // assertThat(awardsCache.getTotalAwards(), is(0L));
    }

    static Employee createTestEmployee(Organization organization, int index) {
        return Employee.builder().organization(organization).firstName("Person" + index)
                .lastName("Person" + index).dundieAwards(index).build();
    }
}
