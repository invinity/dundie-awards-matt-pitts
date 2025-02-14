package com.ninjaone.dundie_awards.controller.organization;

import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
        @JsonSubTypes.Type(value = OrganizationEmployeeActionResult.GiveDundieAwardsResult.class, name = "GiveDundieAwardsResult")
})
public interface OrganizationEmployeeActionResult {
    @lombok.Data
    @lombok.RequiredArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder(builderClassName = "Builder")
    public static final class GiveDundieAwardsResult implements OrganizationEmployeeActionResult {
        private int employeeUpdateCount;
    }
}
