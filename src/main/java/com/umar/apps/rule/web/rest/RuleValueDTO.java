package com.umar.apps.rule.web.rest;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A {@link com.umar.apps.rule.domain.RuleValue} DTO for REST API
 */
public record RuleValueDTO(Long id, String operand, LocalDateTime created, LocalDateTime updated, int version) {

    public RuleValueDTO {
        Objects.requireNonNull(operand, "operand is required");
    }
    /**
     * Constructor for creating a fresh RuleValue
     *
     * @param operand The operand
     */
    public RuleValueDTO(String operand) {
        this(null, operand, LocalDateTime.now(), null, 0);
    }

    /**
     * Constructor for updating a RuleValue
     *
     * @param id The persistent id of the RuleValue to be updated
     * @param operand The operand
     * @param created The timestamp when the RuleValue was created
     * @param version The database version
     */
    public RuleValueDTO(Long id, String operand, LocalDateTime created, int version) {
        this(id, operand, created, LocalDateTime.now(), version);
    }
}
