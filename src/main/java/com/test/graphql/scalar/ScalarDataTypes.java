package com.test.graphql.scalar;

import graphql.schema.GraphQLScalarType;

public class ScalarDataTypes {
    public static final GraphQLScalarType instantScalar = GraphQLScalarType.newScalar()
            .name("Instant")
            .description("JDK8 Instant GraphQLType")
            .coercing(new InstantCoercing())
            .build();
}
