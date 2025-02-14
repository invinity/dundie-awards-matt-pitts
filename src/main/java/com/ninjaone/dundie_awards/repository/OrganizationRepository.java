package com.ninjaone.dundie_awards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default Organization saveOrganization(Organization organization) {
        return saveAndFlush(organization);
    }
}
