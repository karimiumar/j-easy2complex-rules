package com.umar.apps.rule.web.rest.api;

import com.umar.apps.rule.web.rest.BusinessRuleDTO;
import com.umar.apps.rule.web.rest.events.SingleResourceRetrievedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/rest/api")
public class BusinessRulesRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessRulesRestController.class);

    @Autowired
    BusinessRuleRestFacade businessRuleRestFacade;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/rules")
    List<BusinessRuleDTO> findAllRules() {
        return businessRuleRestFacade.findAll();
    }

    @GetMapping("/rules/{id}")
    BusinessRuleDTO findById(@PathVariable("id")long id, final HttpServletResponse response) {
        var businessRule = businessRuleRestFacade.findRuleById(id);
        eventPublisher.publishEvent(new SingleResourceRetrievedEvent(this, response));
        return businessRule;
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    Long create(@RequestBody BusinessRuleDTO resource, final HttpServletResponse response) {
        Objects.requireNonNull(resource, "Incoming BusinessRule resource is null");
        return businessRuleRestFacade.createRule(resource);
        /*
        var optRule = businessRuleService.findByNameAndType(ruleName, ruleType, true);

        return optRule.map(businessRule -> {
            eventPublisher.publishEvent(new ResourceCreatedEvent(this, response, businessRule.getId()));
            return businessRule;
        }).orElseThrow(() -> new ResourceNotFoundException("No BusinessRule Found with: " + ruleName + " " + ruleType));
        */
    }

    @PutMapping("/rules/{id}")
    @ResponseStatus(HttpStatus.OK)
    void update(@PathVariable("id") long id, @RequestBody BusinessRuleDTO resource) {
        Objects.requireNonNull(resource, "Incoming BusinessRule resource is null");
        businessRuleRestFacade.updateRule(resource);
    }

    @DeleteMapping("/rules/id")
    @ResponseStatus(HttpStatus.OK)
    void delete(@PathVariable("id") long id) {
        businessRuleRestFacade.deleteRuleById(id);
    }
}
