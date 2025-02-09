package com.ninjaone.dundie_awards.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import jakarta.persistence.QueryHint;

@Repository
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
    @Modifying(clearAutomatically = true)
    @Transactional(propagation = Propagation.REQUIRED, 
                   isolation = Isolation.DEFAULT, 
                   readOnly = false)
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
    // @QueryHints({
    //     @QueryHint(name = "org.hibernate.cacheable", value = "false"),
    //         @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "REFRESH")})
    List<Employee> findByOrganization(Organization organization);
}
