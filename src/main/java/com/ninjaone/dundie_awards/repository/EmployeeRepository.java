package com.ninjaone.dundie_awards.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Employee x SET x.dundieAwards = x.dundieAwards + ?2 WHERE x.organization = ?1")
    int addDundieAwardsByOrganization(Organization organization, int awardCount);

    int countByOrganization(Organization organization);

    @Transactional
    List<Employee> findByOrganization(Organization organization);
}
