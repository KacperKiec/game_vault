package io.kk.userinsightsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {

    @Bean
    public MongoMappingContext mongoMappingContext(MongoCustomConversions conversions) throws ClassNotFoundException {
        MongoMappingContext context = new MongoMappingContext();
        context.setFieldNamingStrategy(new SnakeCaseFieldNamingStrategy());
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        return context;
    }
}