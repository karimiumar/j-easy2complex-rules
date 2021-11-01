package com.umar.apps.rule.web.rest;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * A RuleAttribute DTO for REST API
 */
public record RuleAttributeDTO(Long id, String attributeName, String ruleType, String displayText, LocalDateTime created, LocalDateTime updated, int version, Set<RuleValueDTO> ruleValues) {

    public RuleAttributeDTO {
        Objects.requireNonNull(attributeName, "attributeName is required");
        Objects.requireNonNull(ruleType, "ruleType is required");
        Objects.requireNonNull(displayText, "displayText is required");
        Objects.requireNonNull(ruleValues, "ruleValues is required");
    }
    /**
     * Constructor for creating a new RuleAttribute
     *
     * @param attributeName The name of the RuleAttribute
     * @param ruleType The type of the Rule
     * @param displayText The text to be displayed for this RuleAttribute
     * @param ruleValues The values of this RuleAttribute
     */
    public RuleAttributeDTO(String attributeName,String ruleType, String displayText, Set<RuleValueDTO> ruleValues) {
        this(null, attributeName, ruleType, displayText, LocalDateTime.now(), null, 0, ruleValues);
    }

    /**
     * Constructor for updating a RuleAttribute
     *
     * @param id The persisted id of RuleAttribute
     * @param attributeName The name of the RuleAttribute
     * @param ruleType The type of the Rule
     * @param displayText The text to be displayed for this RuleAttribute
     * @param created The creation time of RuleAttribute
     * @param version The version of RuleAttribute
     * @param ruleValues The values of this RuleAttribute
     */
    public RuleAttributeDTO(Long id, String attributeName,String ruleType, String displayText, LocalDateTime created, int version, Set<RuleValueDTO> ruleValues) {
        this(id, attributeName, ruleType, displayText , created , LocalDateTime.now(), version, ruleValues);
    }

}
