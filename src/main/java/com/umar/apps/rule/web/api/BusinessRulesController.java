package com.umar.apps.rule.web.api;

import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.service.api.BusinessRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class BusinessRulesController {

    @Autowired
    private BusinessRuleService businessRuleService;

    @GetMapping("/index")
    public String showRulesList(Model model) {
        model.addAttribute("businessRules", businessRuleService.findAll());
        return "index";
    }

    @GetMapping("/rulesForm")
    public String showRulesForm(BusinessRule businessRule) {
        return "rules-form";
    }

    @PostMapping("/addRule")
    public String addRule(@Valid BusinessRule businessRule, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "rules-form";
        }
        var ruleName = businessRule.getRuleName();
        var ruleType = businessRule.getRuleType();
        var priority = businessRule.getPriority();
        var description = businessRule.getDescription();
        createRule(ruleName, ruleType, description, priority);
        return "redirect:/index";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateRuleForm(@PathVariable("id") long id, Model model) {
        var businessRule = businessRuleService.findRuleById(id);
        model.addAttribute("businessRule", businessRule);
        return "update-rule";
    }

    @PostMapping("/update/{id}")
    public String updateBusinessRule(@PathVariable("id")long id, @Valid BusinessRule businessRule, BindingResult result, Model model) {
        if(result.hasErrors()) {
            businessRule.setId(id);
            return "update-rule";
        }
        businessRuleService.updateRule(businessRule);
        return "redirect:/index";
    }

    @PostMapping(value = "/delete")
    public String deleteBusinessRule(HttpServletRequest request) {
        var id = Long.parseLong(request.getParameter("id"));
        businessRuleService.deleteRuleById(id);
        return "redirect:/index";
    }

    @GetMapping("/showAttributes")
    public String showAttributes(@RequestParam("ruleId") long ruleId, Model model) {
        var businessRule = businessRuleService.findRuleById(ruleId);
        composeRuleAttributes(model, ruleId, businessRule);
        return "rules-attributes";
    }

    @GetMapping("/createAttribute")
    public String showAddAttributeForm(@RequestParam("ruleId") long ruleId, RuleAttribute ruleAttribute, Model model) {
        var businessRule = businessRuleService.findRuleById(ruleId);
        model.addAttribute("ruleName", businessRule.getRuleName());
        model.addAttribute("ruleId", ruleId);
        return "add-attribute-form";
    }

    @PostMapping("/addAttribute")
    public String addAttribute(@Valid RuleAttribute ruleAttribute,
                               BindingResult bindingResult, Model model,
                               @RequestParam("ruleId")long ruleId,
                               RedirectAttributes attributes) {
        if(bindingResult.hasErrors()) {
            return "add-attribute-form";
        }
        var attributeName = ruleAttribute.getAttributeName();
        var attributeType = ruleAttribute.getRuleType();
        var displayText = ruleAttribute.getDisplayName();
        var businessRule = businessRuleService.findRuleById(ruleId);
        createAttribute(businessRule, attributeName, attributeType, displayText);
        composeRuleAttributes(model, ruleId, businessRule);
        //RedirectAttributes is responsible for adding parameter to request url
        //In the given case http://..../showAttributes?ruleId={1,2,3....}
        attributes.addAttribute("ruleId", ruleId);
        return "redirect:/showAttributes";
    }

    private void composeRuleAttributes(Model model, @RequestParam("ruleId") long ruleId, BusinessRule businessRule) {
        var ruleAttributes = businessRuleService.findAttributesOfRule(ruleId);
        model.addAttribute("ruleName", businessRule.getRuleName());
        model.addAttribute("ruleId", ruleId);
        model.addAttribute("ruleAttributes", ruleAttributes);
        if(ruleAttributes != null ) {
            ruleAttributes.forEach(System.out::println);
        }
    }


    private void createRule(String ruleName, String ruleType, String description, int priority) {
        businessRuleService.createRule(ruleName, ruleType, description, priority, true);
    }

    private void createAttribute(BusinessRule businessRule, String attributeName, String attributeType, String displayText) {
        businessRuleService.createAttribute(businessRule, attributeName, attributeType, displayText);
    }

}
