package com.umar.apps.rule.web.rest.api;

import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleAttributeValue;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.web.rest.BusinessRuleDTO;
import com.umar.apps.rule.web.rest.RuleAttributeDTO;
import com.umar.apps.rule.web.rest.RuleValueDTO;
import com.umar.apps.rule.web.rest.exceptions.ResourceNotFoundException;
import com.umar.apps.util.GenericBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BusinessRuleRestFacade {

    @Autowired
    private BusinessRuleService businessRuleService;

    public List<BusinessRuleDTO> findAll() {
        var businessRules = businessRuleService.findAll();
        var businessRuleDTOs = new ArrayList<BusinessRuleDTO>(businessRules.size());
        businessRules.forEach(br -> {
            var instance = toRuleDTO(br);
            businessRuleDTOs.add(instance);
        });
        return businessRuleDTOs;
    }

    public BusinessRuleDTO findRuleById(long id) {
        var optRule = businessRuleService.findRuleByIdWithSubgraphs(id);
        return optRule.map(this::toRuleDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No BusinessRule exist for the id: " + id));
    }

    public Long createRule(BusinessRuleDTO resource) {
        var rule = toRule(resource);
        var persistedRule = businessRuleService.save(rule);
        return persistedRule.getId();
    }

    public void updateRule(BusinessRuleDTO resource) {
        var rule = toRule(resource);
        businessRuleService.update(rule);
    }

    public void deleteRuleById(long id) {
        businessRuleService.deleteRuleById(id);
    }

    private BusinessRule toRule(BusinessRuleDTO dto) {
        var ruleAttributes = new HashSet<RuleAttribute>();
        var rule = GenericBuilder.of(BusinessRule::new)
                .with(BusinessRule::setId, dto.id())
                .with(BusinessRule::setRuleName, dto.ruleName())
                .with(BusinessRule::setRuleType, dto.ruleType())
                .with(BusinessRule::setDescription, dto.description())
                .with(BusinessRule::setPriority, dto.priority())
                .with(BusinessRule::setCreated, dto.created())
                .with(BusinessRule::setUpdated, dto.updated())
                .with(BusinessRule::setActive, dto.active())
                .with(BusinessRule::setVersion, dto.version())
                .build();
        var attributesDTO = dto.ruleAttributes();
        attributesDTO.forEach(attr -> {
            var ruleAttribute = toRuleAttribute(attr);
            ruleAttributes.add(ruleAttribute);
        });
        rule.setRuleAttributes(ruleAttributes);
        return rule;
    }

    private RuleAttribute toRuleAttribute(RuleAttributeDTO dto) {
        var ruleAttributeValues = new ArrayList<RuleAttributeValue>();
        var ruleValues = dto.ruleValues();
        var ruleAttribute = GenericBuilder.of(RuleAttribute::new)
                .with(RuleAttribute::setId, dto.id())
                .with(RuleAttribute::setAttributeName, dto.attributeName())
                .with(RuleAttribute::setRuleType, dto.ruleType())
                .with(RuleAttribute::setDisplayName, dto.displayText())
                .with(RuleAttribute::setCreated, dto.created())
                .with(RuleAttribute::setUpdated, dto.updated())
                .with(RuleAttribute::setVersion, dto.version())
                .with(RuleAttribute::setRuleAttributeValues, ruleAttributeValues)
                .build();
        ruleValues.forEach(rvDTO -> {
            var rv = toRuleValue(rvDTO);
            ruleAttributeValues.add(new RuleAttributeValue(ruleAttribute, rv));
        });
        return ruleAttribute;
    }

    private RuleValue toRuleValue(RuleValueDTO dto) {
        return GenericBuilder.of(RuleValue::new)
                .with(RuleValue::setId, dto.id())
                .with(RuleValue::setOperand, dto.operand())
                .with(RuleValue::setCreated, dto.created())
                .with(RuleValue::setUpdated, dto.updated())
                .with(RuleValue::setVersion, dto.version())
                .build();
    }

    private BusinessRuleDTO toRuleDTO(BusinessRule rule) {
        var ruleId = rule.getId();
        var ruleName = rule.getRuleName();
        var ruleType = rule.getRuleType();
        var description = rule.getDescription();
        var priority = rule.getPriority();
        var active = rule.isActive();
        var created = rule.getCreated();
        var updated = rule.getUpdated();
        var version = rule.getVersion();
        var ruleAttrs = rule.getRuleAttributes();
        var ruleAttrDTOs = toRuleAttrDTO(ruleAttrs);
        return new BusinessRuleDTO(
                ruleId, ruleName, ruleType, description, priority,
                active, created, updated, version, ruleAttrDTOs
        );
    }

    private Set<RuleAttributeDTO> toRuleAttrDTO(Set<RuleAttribute> ruleAttrs) {
        var ruleAttrDTOs = new HashSet<RuleAttributeDTO>(ruleAttrs.size());
        ruleAttrs.forEach(ra -> {
            var id = ra.getId();
            var attributeName = ra.getAttributeName();
            var ruleType = ra.getRuleType();
            var displayName = ra.getDisplayName();
            var created = ra.getCreated();
            var updated = ra.getUpdated();
            var version = ra.getVersion();
            var ravs = ra.getRuleAttributeValues();
            var ruleValues = toRuleValues(ravs);
            var ruleValueDTOs = toRuleValueDTO(ruleValues);
            var instance = new RuleAttributeDTO(id, attributeName, ruleType, displayName,
                    created, updated, version, ruleValueDTOs);
            ruleAttrDTOs.add(instance);
        });
        return ruleAttrDTOs;
    }

    private Set<RuleValue> toRuleValues(List<RuleAttributeValue> ravs) {
        var ruleValues = new HashSet<RuleValue>(ravs.size());
        ravs.forEach(rav -> {
            var ruleValue = rav.getRuleValue();
            ruleValues.add(ruleValue);
        });
        return ruleValues;
    }

    private Set<RuleValueDTO> toRuleValueDTO(Set<RuleValue> ruleValues) {
        var ruleValueDTOs = new HashSet<RuleValueDTO>(ruleValues.size());
        ruleValues.forEach(rv -> {
            var id = rv.getId();
            var operand = rv.getOperand();
            var version = rv.getVersion();
            var created = rv.getCreated();
            var updated = rv.getUpdated();
            var instance = new RuleValueDTO(id, operand, created, updated, version);
            ruleValueDTOs.add(instance);
        });
        return ruleValueDTOs;
    }
}
