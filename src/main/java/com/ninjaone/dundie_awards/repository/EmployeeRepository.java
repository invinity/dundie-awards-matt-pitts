package com.ninjaone.dundie_awards.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import jakarta.persistence.Tuple;

@Repository
@Transactional
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Adds Dundie awards to all employees in the organization.
     * <p>
     * This method should be invoked within a transaction.
     * 
     * @param organization
     * @param awardCount
     * @return
     */
    @Modifying
    @Query("UPDATE Employee x SET x.dundieAwards = x.dundieAwards + ?2 WHERE x.organization = ?1")
    int addDundieAwardsByOrganization(Organization organization, int awardCount);

    /**
     * Counts the number of employees in an organization.
     * 
     * @param organization
     * @return
     */
    int countByOrganization(Organization organization);

    /**
     * Finds all employees in an organization.
     * 
     * @param organization
     * @return
     */
    List<Employee> findByOrganization(Organization organization);

    @Query("SELECT x.id, x.dundieAwards FROM Employee x WHERE x.organization = ?1")
    List<Tuple> getEmployeeAwardCountsByOrganization(Organization organization);

    default Map<Long, Integer> getEmployeeAwardCountsMapByOrganization(Organization organization) {
        return getEmployeeAwardCountsByOrganization(organization).stream()
                .collect(Collectors.toMap(r -> r.get(0, Long.class), r -> r.get(1, Integer.class)));
    }
}
