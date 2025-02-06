package com.ninjaone.dundie_awards.controller.organization;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrganizationEmployeeAction.GiveDundieAwardsAction.class, name = "GiveDundieAwardsAction")
})
public interface OrganizationEmployeeAction {
    /**
     * Get type of this action, e.g. it's class name
     * @return
     */
    default String getType() {
        return this.getClass().getSimpleName();
    }

    @lombok.Data
    @lombok.RequiredArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder(builderClassName = "Builder")
    public static class GiveDundieAwardsAction implements OrganizationEmployeeAction {
        private int awardCount;
    }
}
