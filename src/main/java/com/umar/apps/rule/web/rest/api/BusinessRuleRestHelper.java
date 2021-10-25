package com.umar.apps.rule.web.rest.api;

import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.domain.RuleAttributeValue;
import com.umar.apps.rule.domain.RuleValue;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.web.rest.BusinessRuleDTO;
import com.umar.apps.rule.web.rest.RuleAttributeDTO;
import com.umar.apps.rule.web.rest.RuleValueDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BusinessRuleRestHelper {

    @Autowired
    private BusinessRuleService businessRuleService;

    public List<BusinessRuleDTO> findAll() {
        var businessRules = businessRuleService.findAll();
        var businessRuleDTOs = new ArrayList<BusinessRuleDTO>(businessRules.size());
        businessRules.forEach(br -> {
            var ruleId = br.getId();
            var ruleName = br.getRuleName();
            var ruleType = br.getRuleType();
            var description = br.getDescription();
            var priority = br.getPriority();
            var active = br.isActive();
            var created = br.getCreated();
            var updated = br.getUpdated();
            var version = br.getVersion();
            var ruleAttrs = br.getRuleAttributes();
            var ruleAttrDTOs = toRuleAttrDTO(ruleAttrs);
            var instance = new BusinessRuleDTO(
                    ruleId, ruleName, ruleType, description, priority,
                    active, created, updated, version, ruleAttrDTOs
            );
            businessRuleDTOs.add(instance);
        });
        return businessRuleDTOs;
    }

    public BusinessRuleDTO findRuleById(long id) {
        return null;
    }

    public Long createRule(BusinessRuleDTO resource) {
        return 0L;
    }

    public void updateRule(BusinessRuleDTO resource) {
    }

    public void deleteRuleById(long id) {
        businessRuleService.deleteRuleById(id);
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
            var instance = new RuleAttributeDTO(id, attributeName, ruleType, displayName, created, updated, version, ruleValueDTOs);
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
