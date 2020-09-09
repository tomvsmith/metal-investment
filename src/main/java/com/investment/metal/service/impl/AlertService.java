package com.investment.metal.service.impl;

import com.investment.metal.MetalType;
import com.investment.metal.database.Alert;
import com.investment.metal.database.AlertRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.AlertFrequency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService extends AbstractService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MetalPricesService metalPricesService;

    private final ScriptEngine engine;

    public AlertService() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
    }

    public void addAlert(Long userId, String expression, AlertFrequency frequency, MetalType metalType) throws BusinessException {
        Alert alert = new Alert();
        alert.setUserId(userId);
        alert.setMetalSymbol(metalType.getSymbol());
        alert.setExpression(expression);
        alert.setFrequency(frequency.name());
        alert.setLastTimeChecked(new Timestamp(System.currentTimeMillis()));
        this.alertRepository.save(alert);
    }

    public List<Alert> findAllByMetalSymbol(String metalSymbol) {
        return this.alertRepository.findByMetalSymbol(metalSymbol).orElse(new ArrayList<>());
    }

    public boolean evaluateExpression(String expression, double profit) throws ScriptException {
        final String filledExpression = new FilledExpressionBuilder(expression)
                .setProfit(profit)
                .build();
        return (Boolean) engine.eval(filledExpression);
    }

    public void saveAll(List<Alert> allAlerts) {
        this.alertRepository.saveAll(allAlerts);
    }
}
