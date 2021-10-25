package com.umar.apps.rule.web.rest;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * A RuleAttribute DTO for REST API
 */
public record RuleAttributeDTO(long id, String attributeName, String ruleName, String displayText, LocalDateTime created, LocalDateTime updated, int version, Set<RuleValueDTO> ruleValues) {

    /**
     * Constructor for creating a new RuleAttribute
     *
     * @param attributeName The name of the RuleAttribute
     * @param ruleName The name of the Rule
     * @param displayText The text to be displayed for this RuleAttribute
     * @param ruleValues The values of this RuleAttribute
     */
    public RuleAttributeDTO(String attributeName,String ruleName, String displayText, Set<RuleValueDTO> ruleValues) {
        this(0L, attributeName, ruleName, displayText, LocalDateTime.now(), null, 0, ruleValues);
    }

    /**
     * Constructor for updating a RuleAttribute
     *
     * @param id The persisted id of RuleAttribute
     * @param attributeName The name of the RuleAttribute
     * @param ruleName The name of the Rule
     * @param displayText The text to be displayed for this RuleAttribute
     * @param created The creation time of RuleAttribute
     * @param version The version of RuleAttribute
     * @param ruleValues The values of this RuleAttribute
     */
    public RuleAttributeDTO(long id, String attributeName,String ruleName, String displayText, LocalDateTime created, int version, Set<RuleValueDTO> ruleValues) {
        this(id, attributeName, ruleName, displayText , created , LocalDateTime.now(), version, ruleValues);
    }

}
