package org.andrejstrogonov.moskowstockbackend.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.rabbitmq.host", havingValue = "localhost", matchIfMissing = true)
public class ProducerService {

    @Value("${rabbitmq.exchange.market-data:market.data.exchange}")
    private String marketDataExchange;

    @Value("${rabbitmq.exchange.portfolio:portfolio.exchange}")
    private String portfolioExchange;

    @Value("${rabbitmq.routing-key.market-data:stock.update.*}")
    private String marketDataRoutingKey;
}

