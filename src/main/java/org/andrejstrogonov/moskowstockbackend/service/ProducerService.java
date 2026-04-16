package org.andrejstrogonov.moskowstockbackend.service;

import org.andrejstrogonov.moskowstockbackend.dto.GoalRequest;
import org.andrejstrogonov.moskowstockbackend.model.Instrument;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.market-data}")
    private String marketDataExchange;

    @Value("${rabbitmq.exchange.portfolio}")
    private String portfolioExchange;

    @Value("${rabbitmq.routing-key.market-data}")
    private String marketDataRoutingKey;

    public ProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Send market data updates (e.g., stock price changes)
    public void sendMarketDataUpdate(Instrument instrument) {
        rabbitTemplate.convertAndSend(marketDataExchange, "stock.update." + instrument.getType().toString().toLowerCase(), instrument);
    }

    // Send portfolio generation request
    public void sendPortfolioGenerationRequest(GoalRequest goalRequest) {
        rabbitTemplate.convertAndSend(portfolioExchange, "", goalRequest);
    }
}
