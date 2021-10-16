package com.umar.apps.rule.rest.api;

import com.umar.apps.rule.dao.api.RuleDao;
import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.rest.exceptions.ElementAlreadyExistException;
import com.umar.apps.rule.rest.exceptions.NoSuchElementFoundException;
import com.umar.apps.rule.service.api.BusinessRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@Controller
public class BusinessRulesController {

    @Autowired
    private BusinessRuleService businessRuleService;
    @Autowired
    private RuleDao ruleDao;

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
        createRule(ruleName, ruleType, priority);

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
        ruleDao.save(businessRule);
        return "redirect:/index";
    }

    @ExceptionHandler(ElementAlreadyExistException.class)
    public ModelAndView handleElementAlreadyExistException(HttpServletRequest request, Exception exception) {
        var mnv = new ModelAndView();
        mnv.addObject("exception", exception);
        mnv.addObject("url", request.getRequestURL());
        mnv.setViewName("already-exist-exception");
        return mnv;
    }

    @ExceptionHandler(NoSuchElementFoundException.class)
    public ModelAndView handleNoSuchElementFoundException(HttpServletRequest request, Exception exception) {
        var mnv = new ModelAndView();
        mnv.addObject("exception", exception);
        mnv.addObject("url", request.getRequestURL());
        mnv.setViewName("no-element-found-exception");
        return mnv;
    }

    private void createRule(String ruleName, String ruleType, int priority) {
        businessRuleService.createRule(ruleName, ruleType, priority);
    }

}
