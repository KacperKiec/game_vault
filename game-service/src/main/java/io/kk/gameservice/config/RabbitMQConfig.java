package io.kk.gameservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public final String QUEUE_NAME;
    public final String EXCHANGE_NAME;
    public final String ROUTING_KEY;

    public RabbitMQConfig(@Value("${app.rabbit.queue}") String queueName,
                          @Value("${app.rabbit.exchange}") String exchangeName,
                          @Value("${app.rabbit.routing-key}") String routingKey) {
        QUEUE_NAME = queueName;
        EXCHANGE_NAME = exchangeName;
        ROUTING_KEY = routingKey;
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public String getQueue() {
        return QUEUE_NAME;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
