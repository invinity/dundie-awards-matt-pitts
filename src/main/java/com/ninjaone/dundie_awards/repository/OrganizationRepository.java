package com.ninjaone.dundie_awards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ninjaone.dundie_awards.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
