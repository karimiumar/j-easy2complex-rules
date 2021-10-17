package com.umar.apps.rule.dao.api;

import com.umar.apps.rule.dao.api.core.RuleAttributeDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleDaoImpl;
import com.umar.apps.rule.dao.api.core.RuleValueDaoImpl;
import com.umar.apps.rule.domain.RuleAttribute;
import com.umar.apps.rule.service.api.BusinessRuleService;
import com.umar.apps.rule.service.api.core.BusinessRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class CashflowRuleServiceTest {
    private final static EntityManagerFactory emf = Persistence.createEntityManagerFactory("testPU");
    private final RuleDao ruleDao = new RuleDaoImpl(emf);
    private final RuleAttributeDao ruleAttributeDao = new RuleAttributeDaoImpl(emf);
    private final RuleValueDao ruleValueDao = new RuleValueDaoImpl(emf);
    private final BusinessRuleService ruleService = new BusinessRuleServiceImpl(ruleDao, ruleAttributeDao, ruleValueDao);

    @BeforeEach
    public void before() {
        try {
            createSomeRules();
        }catch (Exception ex) {
            //eat ElementAlreadyExistException
        }
    }

    void createSomeRules() {
        CashflowRulesTestProvider.createSomeRulesAndAttributes(ruleDao, ruleService);
    }

    @Test
    @Order(1)
    public void whenGivenDataThenCounterPartySTPRuleIsCreated() {
        var ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "Historic Defaulter Party X");
        assertThat(ruleAttribute.getAttributeName()).isEqualTo("counterParty");
        var ruleValue = ruleValueDao.findByOperand("Historic Defaulter Party X").orElseThrow();
        assertThat(ruleValue.getOperand()).isEqualTo("Historic Defaulter Party X");
    }

    @Test @Order(2)
    public void whenGivenDataThenCounterPartySTPRuleIsAmendedAndNewOperandIsAdded() {
        var ruleAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NON-STP").orElseThrow();
        assertThat(ruleAttribute.getAttributeName()).isEqualTo("counterParty");
        var ruleValue = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertThat(ruleValue.getOperand()).isEqualTo("Lehman Brothers PLC");
    }

    @Test @Order(3)
    public void whenGivenDataThenCurrencySTPRuleIsCreated() {
        var ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "KOD");
        assertThat(ruleAttribute.getAttributeName()).isEqualTo("currency");
        var ruleValue = ruleValueDao.findByOperand("KOD").orElseThrow();
        assertThat(ruleValue.getOperand()).isEqualTo("KOD");
    }

    @Test @Order(4)
    public void whenGivenDataThenCurrencySTPRuleIsAmendedNewOperandsAreAdded() {
        var ruleAttribute = ruleAttributeDao.findRuleAttribute("currency", "NON-STP").orElseThrow();
        createValue(ruleAttribute, "YUAN");
        createValue(ruleAttribute, "YEN");
        assertThat(ruleAttribute.getAttributeName()).isEqualTo("currency");
        var yuan = ruleValueDao.findByOperand("YUAN").orElseThrow();
        assertThat(yuan.getOperand()).isEqualTo("YUAN");
        var yen = ruleValueDao.findByOperand("YEN").orElseThrow();
        assertThat(yen.getOperand()).isEqualTo("YEN");
    }

    @Test @Order(5)
    public void whenGivenDataThenAmountSTPRuleIsCreated() {
        var ruleAttribute = ruleAttributeDao.findRuleAttribute("amount", "NON-STP");
        ruleAttribute.ifPresentOrElse(ra -> {
            createValue(ra, "2300000.00");
            assertThat(ra.getAttributeName()).isEqualTo("amount");
            var amount = ruleValueDao.findByOperand("2300000.00");
            amount.ifPresent(amt -> {
                assertThat(amt.getOperand()).isEqualTo("2300000.00");
            });
        }, RuntimeException::new);
    }

    @Test @Order(7)
    public void whenGivenDataThenNettingRuleIsCreated() {
        var cptyAttribute = ruleAttributeDao.findRuleAttribute("counterParty", "NETTING").orElseThrow();
        var currencyAttribute = ruleAttributeDao.findRuleAttribute("currency", "NETTING").orElseThrow();
        var settlementDtAttribute = ruleAttributeDao.findRuleAttribute("settlementDate", "NETTING").orElseThrow();
        createValue(cptyAttribute, "Lehman Brothers PLC");
        createValue(currencyAttribute, "USD");
        createValue(settlementDtAttribute, LocalDate.now().plusDays(10).toString());
        assertThat(ruleAttributeDao.findRuleAttribute("amount",  "NETTING")).isEmpty();
        var currency = ruleValueDao.findByOperand("USD").orElseThrow();
        assertThat(currency.getOperand()).isEqualTo("USD");
        var settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(10).toString()).orElseThrow();
        assertThat(LocalDate.parse(settlementDate.getOperand())).isEqualTo(LocalDate.now().plusDays(10));
        var cpty = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertThat(cpty.getOperand()).isEqualTo("Lehman Brothers PLC");
    }

    @Test @Order(8)
    public void whenGivenDataThenNettingRuleIsAdded() {
        var currencyAttribute = ruleAttributeDao.findRuleAttribute("currency", "NETTING").orElseThrow();
        var settlementDtAttribute = ruleAttributeDao.findRuleAttribute("settlementDate", "NETTING").orElseThrow();
        createValue(currencyAttribute, "EUR");
        createValue(settlementDtAttribute, LocalDate.now().plusDays(15).toString());
        var cpty = ruleValueDao.findByOperand("Lehman Brothers PLC").orElseThrow();
        assertThat(cpty.getOperand()).isEqualTo("Lehman Brothers PLC");
        var settlementDate = ruleValueDao.findByOperand(LocalDate.now().plusDays(15).toString()).orElseThrow();
        assertThat(LocalDate.parse(settlementDate.getOperand())).isEqualTo(LocalDate.now().plusDays(15));
        var currency = ruleValueDao.findByOperand("EUR").orElseThrow();
        assertThat(currency.getOperand()).isEqualTo("EUR");
    }

    @Test
    public void whenGivenRuleValueDao_if_non_existent_operand_is_searched_then_returns_empty() {
        assertThat(ruleValueDao.findByOperand("Throws Exception")).isEmpty();
    }

    private void createValue(RuleAttribute ruleAttribute, String operand) {
        ruleService.createValue(ruleAttribute, operand);
    }
}
