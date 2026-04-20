package io.kk.userinsightsservice.config;

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

    public final String ACTIVITY_QUEUE_NAME;
    public final String ACTIVITY_EXCHANGE_NAME;
    public final String ACTIVITY_ROUTING_KEY;

    public final String DASHBOARD_QUEUE_NAME;
    public final String DASHBOARD_EXCHANGE_NAME;
    public final String DASHBOARD_ROUTING_KEY;

    public RabbitMQConfig(
            @Value("${app.rabbit.activity.queue}") String activityQueueName,
            @Value("${app.rabbit.activity.exchange}") String activityExchangeName,
            @Value("${app.rabbit.activity.routing-key}") String activityRoutingKey,
            @Value("${app.rabbit.dashboard.queue}") String dashboardQueueName,
            @Value("${app.rabbit.dashboard.exchange}") String dashboardExchangeName,
            @Value("${app.rabbit.dashboard.routing-key}") String dashboardRoutingKey) {
        ACTIVITY_QUEUE_NAME = activityQueueName;
        ACTIVITY_EXCHANGE_NAME = activityExchangeName;
        ACTIVITY_ROUTING_KEY = activityRoutingKey;
        DASHBOARD_QUEUE_NAME = dashboardQueueName;
        DASHBOARD_EXCHANGE_NAME = dashboardExchangeName;
        DASHBOARD_ROUTING_KEY = dashboardRoutingKey;
    }

    @Bean
    public Queue activityQueue() {
        return new Queue(ACTIVITY_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange activityExchange() {
        return new DirectExchange(ACTIVITY_EXCHANGE_NAME);
    }

    @Bean
    public Binding activityBinding(Queue activityQueue, DirectExchange activityExchange) {
        return BindingBuilder.bind(activityQueue).to(activityExchange).with(ACTIVITY_ROUTING_KEY);
    }

    @Bean
    public String getActivityQueue() {
        return ACTIVITY_QUEUE_NAME;
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
}
