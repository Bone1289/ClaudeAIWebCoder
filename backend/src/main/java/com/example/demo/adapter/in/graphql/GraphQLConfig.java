package com.example.demo.adapter.in.graphql;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * GraphQL configuration for custom scalars and runtime wiring.
 */
@Configuration
public class GraphQLConfig {

    /**
     * Configure custom scalars for GraphQL.
     * Adds support for Date, DateTime, and Decimal types.
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.DateTime)
                // Register BigDecimal scalar with the name "Decimal" to match our schema
                .scalar(GraphQLScalarType.newScalar()
                        .name("Decimal")
                        .description("A custom scalar that handles arbitrary precision decimal numbers")
                        .coercing(ExtendedScalars.GraphQLBigDecimal.getCoercing())
                        .build());
    }
}
