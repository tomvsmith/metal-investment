package com.investment.metal;

import com.investment.metal.database.Currency;
import com.investment.metal.database.MetalPrice;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.CurrencyType;
import com.investment.metal.service.ExternalMetalPriceService;
import com.investment.metal.service.impl.CurrencyService;
import com.investment.metal.service.impl.ExceptionService;
import com.investment.metal.service.impl.MetalPricesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Timestamp;

@Configuration
@EnableScheduling
public class Scheduler {

    @Autowired
    private ExternalMetalPriceService externalPriceService;

    @Autowired
    private MetalPricesService metalPricesService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    protected ExceptionService exceptionService;

    private final RSSFeedParser rssFeedParser = new RSSFeedParser();

    private MetalType metalType = MetalType.GOLD;

    @PostConstruct
    public void init() {
        for (MetalType type : MetalType.values()) {
            this.metalType = type;
            this.fetchMetalPrices();
        }
        this.fetchCurrencyValues();
    }

    @Transactional
    @Scheduled(fixedDelay = 3600 * 1000)
    public void fetchMetalPrices() {
        final double metalPrice = this.externalPriceService.fetchPrice(metalType);

        MetalPrice price = new MetalPrice();
        price.setMetalSymbol(metalType.getSymbol());
        price.setPrice(metalPrice);
        price.setTime(new Timestamp(System.currentTimeMillis()));
        this.metalPricesService.save(price);

        int ord = (this.metalType.ordinal() + 1) % MetalType.values().length;
        this.metalType = MetalType.values()[ord];
    }

    @Transactional
    @Scheduled(fixedDelay = 12 * 3600 * 1000)
    public void fetchCurrencyValues() {
        for (CurrencyType currency : CurrencyType.values()) {
            if (currency.getFeelURL() != null) {
                this.fetchCurrency(currency);
            }
        }
    }

    private void fetchCurrency(CurrencyType currency) {
        double ron;
        try {
            ron = this.rssFeedParser.readFeed(currency.getFeelURL());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Currency curr;
        try {
            curr = this.currencyService.findBySymbol(currency);
        } catch (BusinessException e) {
            curr = new Currency();
        }
        curr.setRon(ron);
        curr.setSymbol(currency.name());
        curr.setTime(new Timestamp(System.currentTimeMillis()));
        this.currencyService.save(curr);
    }

}