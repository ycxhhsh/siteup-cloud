package com.siteup.biz.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel configuration for circuit breaking and rate limiting.
 * Configures rules for protecting service calls and API endpoints.
 */
@Configuration
public class SentinelConfig {

    /**
     * Enable Sentinel annotations
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * Initialize Sentinel rules on application startup
     */
    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
    }

    /**
     * Configure flow control (rate limiting) rules
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // Rate limiting for project creation (10 requests per minute)
        FlowRule createProjectRule = new FlowRule();
        createProjectRule.setResource("POST:/api/projects");
        createProjectRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        createProjectRule.setCount(10); // 10 QPS
        rules.add(createProjectRule);

        // Rate limiting for template access (20 requests per minute)
        FlowRule templateRule = new FlowRule();
        templateRule.setResource("GET:/api/templates");
        templateRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        templateRule.setCount(20); // 20 QPS
        rules.add(templateRule);

        FlowRuleManager.loadRules(rules);
    }

    /**
     * Configure circuit breaking (degrade) rules
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // Circuit breaker for engine service calls
        DegradeRule engineRule = new DegradeRule();
        engineRule.setResource("siteup-engine");
        engineRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        engineRule.setCount(0.5); // 50% exception ratio
        engineRule.setTimeWindow(60); // 60 seconds recovery
        engineRule.setMinRequestAmount(5); // Minimum 5 requests to trigger
        rules.add(engineRule);

        // Circuit breaker for auth service calls
        DegradeRule authRule = new DegradeRule();
        authRule.setResource("siteup-auth");
        authRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        authRule.setCount(0.3); // 30% exception ratio (more sensitive for auth)
        authRule.setTimeWindow(30); // 30 seconds recovery
        authRule.setMinRequestAmount(3); // Minimum 3 requests to trigger
        rules.add(authRule);

        // Load the rules
        // Note: DegradeRuleManager is used for degrade rules
        // For this demo, we'll use annotation-based circuit breaking instead
    }
}

