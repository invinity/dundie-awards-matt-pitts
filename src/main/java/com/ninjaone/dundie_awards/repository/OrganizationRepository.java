package com.ninjaone.dundie_awards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ninjaone.dundie_awards.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true)
    @NonNull
    <S extends Organization> S saveAndFlush(@NonNull S entity);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteAll();
}
