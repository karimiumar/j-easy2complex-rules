package com.umar.apps.rule.web.rest;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link com.umar.apps.rule.domain.BusinessRule} DTO for REST API
 */
public record BusinessRuleDTO(Long id, String ruleName, String ruleType, String description, Integer priority, boolean active,
                              LocalDateTime created, LocalDateTime updated, int version, Set<RuleAttributeDTO> ruleAttributes) {

    public BusinessRuleDTO {
        Objects.requireNonNull(ruleName, "ruleName is required");
        Objects.requireNonNull(ruleType, "ruleType is required");
        Objects.requireNonNull(description, "description is required");
        Objects.requireNonNull(priority, "priority is required");
        Objects.requireNonNull(ruleAttributes, "ruleAttributes is required");
    }
    /**
     * Constructor for Creating a new BusinessRule
     * @param ruleName The name of the Rule
     * @param ruleType The type of the Rule
     * @param description Description of the Rule
     * @param priority The priority of the Rule
     * @param ruleAttributes The attributes of this Rule
     */
    public BusinessRuleDTO(String ruleName,String ruleType,String description, Integer priority, Set<RuleAttributeDTO> ruleAttributes) {
        this(null, ruleName, ruleType, description, priority, true, LocalDateTime.now(), null, 0, ruleAttributes);
    }

    /**
     * Constructor for updating a Rule
     * @param id The persisted id of the Rule to be updated
     * @param ruleName The name of the Rule
     * @param ruleType The type of the Rule
     * @param description Description of the Rule
     * @param priority The priority of the Rule
     * @param active Indicates whether the Rule is active or not
     * @param created The timestamp when the Rule was created
     * @param version The database version of the Rule
     * @param ruleAttributes The attributes of this Rule
     */
    public BusinessRuleDTO(Long id, String ruleName,String ruleType,String description,Integer priority, boolean active, LocalDateTime created, int version, Set<RuleAttributeDTO> ruleAttributes) {
        this(id, ruleName, ruleType, description, priority, active, created, LocalDateTime.now(), version, ruleAttributes);
    }
}

