package com.example.demo.adapter.in.graphql;

import graphql.language.StringValue;
import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
     */
    @Bean
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
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        throw new CoercingSerializeException("Expected a LocalDateTime object.");
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                        try {
                            if (input instanceof String) {
                                return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            }
                            throw new CoercingParseValueException("Expected a String");
                        } catch (Exception e) {
                            throw new CoercingParseValueException("Unable to parse value to LocalDateTime: " + input, e);
                        }
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof StringValue) {
                            try {
                                return LocalDateTime.parse(((StringValue) input).getValue(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            } catch (Exception e) {
                                throw new CoercingParseLiteralException("Unable to parse literal to LocalDateTime: " + input, e);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected a StringValue.");
                    }
                })
                .build();
    }
}
