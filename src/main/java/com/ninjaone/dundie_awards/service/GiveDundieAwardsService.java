package com.ninjaone.dundie_awards.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@lombok.RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GiveDundieAwardsService {
    private final EmployeeRepository employeeRepository;
    private final JmsTemplate jmsTemplate;

    /**
     * Executes the businsess logic to give Dundie awards to all employees in an organization.
     * @param organizationId
     * @param awardCount
     * @return The number of employees that were updated
     */
    @Transactional
    public int giveDundieAwardsByOrganization(Organization organization, int awardCount) {
        int count = employeeRepository.addDundieAwardsByOrganization(organization, awardCount);
        jmsTemplate.convertAndSend("dundie-awards", Activity.builder().event("DUNDIE_AWARDS_GIVEN").build());
        return count;
    }
}
