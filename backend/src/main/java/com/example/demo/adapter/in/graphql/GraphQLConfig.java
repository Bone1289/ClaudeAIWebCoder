package com.example.demo.adapter.in.graphql;

import graphql.language.StringValue;
import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GraphQL configuration for custom scalars and runtime wiring.
 */
@Configuration
public class GraphQLConfig {

    /**
     * Configure custom scalars for GraphQL.
     * Adds support for Date, DateTime, and Decimal types.
     * Using @Order to ensure our custom scalars override any auto-configured ones.
     */
    @Bean
    @Order(0) // Highest precedence to override auto-configuration
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.Date)
                .scalar(createLocalDateTimeScalar())
                // Register BigDecimal scalar with the name "Decimal" to match our schema
                .scalar(GraphQLScalarType.newScalar()
                        .name("Decimal")
                        .description("A custom scalar that handles arbitrary precision decimal numbers")
                        .coercing(ExtendedScalars.GraphQLBigDecimal.getCoercing())
                        .build());
    }

    /**
     * Create a custom DateTime scalar that handles LocalDateTime instead of OffsetDateTime
     */
    private GraphQLScalarType createLocalDateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("A custom scalar that handles LocalDateTime")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        if (dataFetcherResult == null) {
                            return null;
                        }
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        throw new CoercingSerializeException(
                            "Expected a LocalDateTime object but got: " + dataFetcherResult.getClass().getName()
                        );
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                        if (input == null) {
                            return null;
                        }
                        try {
                            if (input instanceof String) {
                                return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            }
                            throw new CoercingParseValueException("Expected a String but got: " + input.getClass().getName());
                        } catch (CoercingParseValueException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new CoercingParseValueException("Unable to parse value to LocalDateTime: " + input, e);
                        }
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input == null) {
                            return null;
                        }
                        if (input instanceof StringValue) {
                            try {
                                String value = ((StringValue) input).getValue();
                                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            } catch (Exception e) {
                                throw new CoercingParseLiteralException("Unable to parse literal to LocalDateTime: " + input, e);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected a StringValue but got: " + input.getClass().getName());
                    }
                })
                .build();
    }
}
