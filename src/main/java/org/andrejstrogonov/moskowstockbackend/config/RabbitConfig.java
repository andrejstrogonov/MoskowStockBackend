package org.andrejstrogonov.moskowstockbackend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "spring.rabbitmq.host", havingValue = "localhost", matchIfMissing = true)
public class RabbitConfig {

    @Value("${rabbitmq.exchange.market-data:market.data.exchange}")
    private String marketDataExchange;

    @Value("${rabbitmq.exchange.portfolio:portfolio.exchange}")
    private String portfolioExchange;

    @Value("${rabbitmq.queue.market-data-updates:market.data.updates}")
    private String marketDataUpdatesQueue;

    @Value("${rabbitmq.queue.portfolio-generation:portfolio.generation}")
    private String portfolioGenerationQueue;

    @Value("${rabbitmq.queue.dead-letter:dead.letter.queue}")
    private String deadLetterQueue;

    @Value("${rabbitmq.routing-key.market-data:stock.update.*}")
    private String marketDataRoutingKey;

    // Topic Exchange for market data (flexible routing)
    @Bean
    public TopicExchange marketDataExchange() {
        return new TopicExchange(marketDataExchange, true, false);
    }

    // Fanout Exchange for portfolio notifications
    @Bean
    public FanoutExchange portfolioExchange() {
        return new FanoutExchange(portfolioExchange, true, false);
    }

    // Dead Letter Exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dead.letter.exchange", true, false);
    }

    // Quorum Queue for market data updates (high availability)
    @Bean
    public Queue marketDataUpdatesQueue() {
        return QueueBuilder.durable(marketDataUpdatesQueue)
                .quorum()
                .deadLetterExchange("dead.letter.exchange")
                .deadLetterRoutingKey("dead")
                .build();
    }

    // Quorum Queue for portfolio generation
    @Bean
    public Queue portfolioGenerationQueue() {
        return QueueBuilder.durable(portfolioGenerationQueue)
                .quorum()
                .deadLetterExchange("dead.letter.exchange")
                .deadLetterRoutingKey("dead")
                .build();
    }

    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    // Bindings
    @Bean
    public Binding marketDataBinding() {
        return BindingBuilder.bind(marketDataUpdatesQueue()).to(marketDataExchange()).with(marketDataRoutingKey);
    }

    @Bean
    public Binding portfolioBinding() {
        return BindingBuilder.bind(portfolioGenerationQueue()).to(portfolioExchange());
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("dead");
    }

    // Configure listener container for prefetch and acknowledgements
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(10); // Limit messages per consumer
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // Manual acknowledgements for reliability
        return factory;
    }
}
