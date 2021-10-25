package com.umar.apps.rule.web.rest;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * A {@link com.umar.apps.rule.domain.BusinessRule} DTO for REST API
 */
public record BusinessRuleDTO(long id, String ruleName, String ruleType, String description, int priority, boolean active,
                              LocalDateTime created, LocalDateTime updated, int version, Set<RuleAttributeDTO> ruleAttributes) {

    /**
     * Constructor for Creating a new BusinessRule
     * @param ruleName The name of the Rule
     * @param ruleType The type of the Rule
     * @param description Description of the Rule
     * @param priority The priority of the Rule
     * @param ruleAttributes The attributes of this Rule
     */
    public BusinessRuleDTO(String ruleName,String ruleType,String description, int priority, Set<RuleAttributeDTO> ruleAttributes) {
        this(0L, ruleName, ruleType, description, priority, true, LocalDateTime.now(), null, 0, ruleAttributes);
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
    public BusinessRuleDTO(long id, String ruleName,String ruleType,String description,int priority, boolean active, LocalDateTime created, int version, Set<RuleAttributeDTO> ruleAttributes) {
        this(id, ruleName, ruleType, description, priority, active, created, LocalDateTime.now(), version, ruleAttributes);
    }
}

