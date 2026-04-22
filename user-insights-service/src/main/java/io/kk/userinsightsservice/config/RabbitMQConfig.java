package io.kk.userinsightsservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public final String DASHBOARD_QUEUE_NAME;
    public final String DASHBOARD_EXCHANGE_NAME;
    public final String DASHBOARD_ROUTING_KEY;

    public RabbitMQConfig(
            @Value("${app.rabbit.dashboard.queue}") String dashboardQueueName,
            @Value("${app.rabbit.dashboard.exchange}") String dashboardExchangeName,
            @Value("${app.rabbit.dashboard.routing-key}") String dashboardRoutingKey) {
        DASHBOARD_QUEUE_NAME = dashboardQueueName;
        DASHBOARD_EXCHANGE_NAME = dashboardExchangeName;
        DASHBOARD_ROUTING_KEY = dashboardRoutingKey;
    }

    @Bean
    public Queue dashboardQueue() {
        return new Queue(DASHBOARD_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange dashboardExchange() {
        return new DirectExchange(DASHBOARD_EXCHANGE_NAME);
    }

    @Bean
    public Binding dashboardBinding(Queue dashboardQueue, DirectExchange dashboardExchange) {
        return BindingBuilder.bind(dashboardQueue).to(dashboardExchange).with(DASHBOARD_ROUTING_KEY);
    }

    @Bean
    public String getDashboardQueue() {
        return DASHBOARD_QUEUE_NAME;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxRetries(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}