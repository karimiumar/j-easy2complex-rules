package com.umar.apps.rule.web.api;

import com.umar.apps.rule.domain.BusinessRule;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.web.exceptions.NoSuchElementFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;

@Controller
public class BusinessRulesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessRulesController.class);

    @Autowired
    private BusinessRuleService businessRuleService;

    @GetMapping("/index")
    public String showRulesList(Model model) {
        model.addAttribute("businessRules", businessRuleService.findAll());
        return "index";
    }

    @GetMapping("/rulesForm")
    public String showRulesForm(BusinessRule businessRule) {
        return "add-rules-form";
    }

    @PostMapping("/addRule")
    public String addRule(@Valid BusinessRule businessRule, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "add-rules-form";
        }
        var ruleName = businessRule.getRuleName();
        var ruleType = businessRule.getRuleType();
        var priority = businessRule.getPriority();
        var description = businessRule.getDescription();
        createRule(ruleName, ruleType, description, priority);
        return "redirect:/index";
    }

    @GetMapping("/editRule/{id}")
    public String showUpdateRuleForm(@PathVariable("id") long id, Model model) {
        var businessRule = businessRuleService.findRuleById(id);
        businessRule.ifPresentOrElse(br -> {
            LOGGER.debug("Found BusinessRule {} ", br.getId());
            composeRuleAttributes(model, br.getId(), br);
        },
            () -> {
               throw new NoSuchElementFoundException("No BusinessRule found for the id: " + id);
        });
        return "update-rule";
    }

    @PostMapping("/updateRule/{id}")
    public String updateBusinessRule(@PathVariable("id")long id, @Valid BusinessRule businessRule, BindingResult result, Model model) {
        if(result.hasErrors()) {
            businessRule.setId(id);
            return "update-rule";
        }
        businessRuleService.update(businessRule);
        return "redirect:/index";
    }

    @PostMapping(value = "/deleteRule")
    public String deleteBusinessRule(HttpServletRequest request) {
        var id = Long.parseLong(request.getParameter("id"));
        businessRuleService.deleteRuleById(id);
        return "redirect:/index";
    }

    @GetMapping("/showAttributes")
    public String showAttributes(@RequestParam("ruleId") long ruleId, Model model) {
        var businessRule = businessRuleService.findRuleById(ruleId);
        businessRule.ifPresentOrElse(br -> composeRuleAttributes(model, ruleId, br),
        () -> {
            throw new NoSuchElementFoundException("No BusinessRule found for the id: " + ruleId);
        });
        return "rules-attributes";
    }

    @GetMapping("/createAttribute")
    public String showAddAttributeForm(@RequestParam("ruleId") long ruleId, RuleAttribute ruleAttribute, Model model) {
        var businessRule = businessRuleService.findRuleById(ruleId);
        businessRule.ifPresentOrElse(br -> {
            model.addAttribute("ruleName", br.getRuleName());
            model.addAttribute("ruleId", ruleId);
        }, () -> {
            throw new NoSuchElementFoundException("No BusinessRule found for the id: " + ruleId);
        });
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
        businessRule.ifPresentOrElse(br -> {
            createAttribute(br, attributeName, attributeType, displayText);
            composeRuleAttributes(model, ruleId, br);
            model.addAttribute("ruleName", br.getRuleName());
            model.addAttribute("ruleId", ruleId);
        }, () -> {
            throw new NoSuchElementFoundException("No BusinessRule found for the id: " + ruleId);
        });
        //RedirectAttributes is responsible for adding parameter to request url
        //In the given case http://..../showAttributes?ruleId={1,2,3....}
        attributes.addAttribute("ruleId", ruleId);
        return "redirect:/showAttributes";
    }

    @GetMapping("/editAttribute/{id}")
    public String showEditAttributeForm(@PathVariable("id") long id, Model model) {
        var ra = businessRuleService.findAttributeById(id);
        ra.ifPresentOrElse(ruleAttribute -> {
            model.addAttribute("ruleAttribute", ruleAttribute);
        }
        , () -> {
              throw new NoSuchElementFoundException("No RuleAttribute exists for the given id:" + id);
           }
        );
        return "edit-attribute-form";
    }

    @PostMapping("/updateAttribute/{id}")
    public String updateAttribute(@PathVariable("id")long id
            , @Valid RuleAttribute ruleAttribute
            , BindingResult result, Model model
            ,RedirectAttributes attributes) {
        if(result.hasErrors()) {
            ruleAttribute.setId(id);
            return "edit-attribute-form";
        }
        LOGGER.debug("Received updateAttribute() request for {} with RuleAttribute {}", id, ruleAttribute);
        businessRuleService.update(ruleAttribute);
        var optionalRA = businessRuleService.findAttributeById(id);
        optionalRA.ifPresentOrElse(ra -> {
            var businessRule = ra.getBusinessRule();
            LOGGER.debug("Composing BusinessRule {}" , businessRule);
            var ruleId = businessRule.getId();
            composeRuleAttributes(model, ruleId, businessRule);
            attributes.addAttribute("ruleId", ruleId);
        }, () -> {
            throw new NoSuchElementFoundException("No RuleAttribute found for the id: " + id);
        });
        return "redirect:/showAttributes";
    }

    @PostMapping("/deleteAttribute")
    public String deleteAttribute(@Valid RuleAttribute ruleAttribute
            ,Model model
            , RedirectAttributes attributes) {
        var id = ruleAttribute.getId();
        var optionalRA = businessRuleService.findAttributeById(id);
        optionalRA.ifPresentOrElse(ra -> {
            var businessRule = ra.getBusinessRule();
            LOGGER.debug("Composing BusinessRule {}" , businessRule);
            var ruleId = businessRule.getId();
            composeRuleAttributes(model, ruleId, businessRule);
            attributes.addAttribute("ruleId", ruleId);
            LOGGER.debug("Deleting RuleAttribute with id {}", id);
            businessRuleService.deleteRuleAttributeById(id);
            LOGGER.debug("Deleted RuleAttribute with id {}", id);
        }, () -> {
            throw new NoSuchElementFoundException("No RuleAttribute found for the id: " + id);
        });
        return "redirect:/showAttributes";
    }

    private void composeRuleAttributes(Model model, @RequestParam("ruleId") long ruleId, BusinessRule businessRule) {
        Objects.requireNonNull(businessRule, "BusinessRule cannot be null");
        var ruleAttributes = businessRuleService.findAttributesOfRule(ruleId);
        model.addAttribute("ruleName", businessRule.getRuleName());
        model.addAttribute("ruleId", ruleId);
        model.addAttribute("businessRule", businessRule);
        model.addAttribute("ruleAttributes", ruleAttributes);
    }


    private void createRule(String ruleName, String ruleType, String description, int priority) {
        businessRuleService.createRule(ruleName, ruleType, description, priority, true);
    }

    private void createAttribute(BusinessRule businessRule, String attributeName, String attributeType, String displayText) {
        businessRuleService.createAttribute(businessRule, attributeName, attributeType, displayText);
    }

}
