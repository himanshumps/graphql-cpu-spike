package com.test.graphql.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Function;


public class InstantCoercing implements Coercing<Instant, String> {

    private static Instant parseInstant(String input, Function<String, RuntimeException> exceptionMaker) {
        try {
            return Instant.parse(input);
        } catch (DateTimeParseException e) {
            throw exceptionMaker.apply(e.getMessage());
        }
    }

    private static Optional<Instant> toInstant(Object input, Function<String, RuntimeException> exceptionMaker) {
        try {
            if (input instanceof TemporalAccessor) {
                return Optional.of(Instant.from((TemporalAccessor) input));
            }
        } catch (DateTimeException e) {
            throw exceptionMaker.apply(e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public String serialize(Object input) {
        Optional<Instant> instant;

        if (input instanceof String) {
            instant = Optional.of(parseInstant(input.toString(), CoercingSerializeException::new));
        } else {
            instant = toInstant(input, CoercingSerializeException::new);
        }

        if (instant.isPresent()) {
            return DateTimeFormatter.ISO_INSTANT.format(instant.get());
        }

        throw new CoercingSerializeException("Expected a 'String' or 'TemporalAccessor'.");
    }

    @Override
    public Instant parseValue(Object input) {
        if (input instanceof String) {
            return parseInstant(String.valueOf(input), CoercingParseValueException::new);
        }

        Optional<Instant> instant = toInstant(input, CoercingParseValueException::new);
        if (!instant.isPresent()) {
            throw new CoercingParseValueException("Expected a 'Instant' like object.");
        }

        return instant.get();
    }

    @Override
    public Instant parseLiteral(Object input) {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException("Expected AST type 'StringValue'.");
        }
        return parseInstant(((StringValue) input).getValue(), CoercingParseLiteralException::new);
    }
}