package com.umar.apps.rule.web.api;

import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.service.api.BusinessRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    private void createRule(String ruleName, String ruleType, String description, int priority) {
        businessRuleService.createRule(ruleName, ruleType, description, priority, true);
    }

}
