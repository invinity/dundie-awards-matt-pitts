package com.ninjaone.dundie_awards.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.AwardsCache;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@lombok.RequiredArgsConstructor(onConstructor = @__(@Autowired))
@lombok.Getter
@Slf4j
public class GiveDundieAwardsService {
    public static final String DUNDIE_MESSAGES_QUEUE = "dundie-messages";
    private final EmployeeRepository employeeRepository;
    private final ActivityRepository activityRepository;
    private final AwardsCache awardsCache;
    private final JmsTemplate jmsTemplate;

    /**
     * XXX this is very bad design, b/c it consumes memory indefinitely this is just here for
     * illustration
     */
    private final List<AwardsGivenMessage> consumedAwardsMessages = new ArrayList<>();

    /**
     * Executes the businsess logic to give Dundie awards to all employees in an organization.
     * <p>
     * This service method performs the following activities in a programmatic transaction:
     * <ol>
     * <li>Updates the employees in the organization with the specified number of Dundie awards</li>
     * <li>Posts a message to the JMS queue to indicate that the awards have been given</li>
     * </ol>
     * If the transaction is successful, the method returns the number of employees that were
     * updated. If an exception occurs while submitting the JMS message, the transaction will be
     * rolled back and no Employees will be updated.
     * 
     * @param organization the {@link Organization} of the {@link Employee}s to update
     * @param awardCount the number of awards to give to each {@link Employee}
     * @return The number of {@link Employee}s were updated
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public Integer giveDundieAwardsByOrganization(Organization organization, int awardCount) {
        int employeeCount =
                employeeRepository.addDundieAwardsByOrganization(organization, awardCount);
        jmsTemplate.convertAndSend(DUNDIE_MESSAGES_QUEUE,
                AwardsGivenMessage.builder().awardCount(awardCount)
                        .affectedEmployeeCount(employeeCount)
                        .thread(Thread.currentThread().getName()).build());
        return employeeCount;
    }

    @JmsListener(destination = DUNDIE_MESSAGES_QUEUE)
    public void receiveDundieMessage(final Message message) throws JMSException {
        log.info("Received message: {}; in thread {}", message, Thread.currentThread().getName());
        if (message instanceof ObjectMessage) {
            AwardsGivenMessage agMessage =
                    ((ObjectMessage) message).getBody(AwardsGivenMessage.class);
            consumedAwardsMessages.add(agMessage);
            activityRepository.save(Activity.builder().event("DUNDIE_AWARDS_GIVEN")
                    .occurredInThread(agMessage.getThread()).build());
            awardsCache.incrementTotalAwards(
                    agMessage.getAwardCount() * agMessage.getAffectedEmployeeCount());
        }
    }

    @lombok.Data
    @lombok.Builder(builderClassName = "Builder")
    static class AwardsGivenMessage implements Serializable {
        private int awardCount;
        private long affectedEmployeeCount;
        private String thread;
    }
}
