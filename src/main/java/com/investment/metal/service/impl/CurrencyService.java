package com.investment.metal.service.impl;

import com.investment.metal.MessageKey;
import com.investment.metal.database.Currency;
import com.investment.metal.database.CurrencyRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.CurrencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService extends AbstractService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public Currency findBySymbol(CurrencyType type) throws BusinessException {
        return this.currencyRepository
                .findBySymbol(type.name())
                .orElseThrow(() -> exceptionService
                        .createBuilder(MessageKey.INEXISTING_CURRENCY)
                        .setArguments(type.name())
                        .build());
    }

    public void save(Currency currency) {
        this.currencyRepository.save(currency);
    }

}
