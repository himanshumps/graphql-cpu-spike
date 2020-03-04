package com.test.graphql.verticle;

import com.test.graphql.scalar.ScalarDataTypes;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.ext.web.handler.graphql.VertxPropertyDataFetcher;
import lombok.extern.slf4j.Slf4j;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@Slf4j
public class ServerVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        GraphQLHandler graphQLHandler = GraphQLHandler.create(createGraphQL(), new GraphQLHandlerOptions().setRequestBatchingEnabled(true));
        Router router = Router.router(vertx);
        //router.route().handler(LoggerHandler.create(LoggerFormat.SHORT));
        router.route("/graphql").handler(graphQLHandler);
        vertx.createHttpServer().requestHandler(router).listen(8080, handler -> {
            if (handler.succeeded()) {
                log.info("Server started on port 8080");
            } else {
                log.error("Server failed to start on port 8080", handler.cause());
            }
        });
    }


    private GraphQL createGraphQL() {
        log.info("Loading the graphQL schema");
        String schema = vertx.fileSystem().readFileBlocking("tetris.graphqls").toString();
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .wiringFactory(new WiringFactory() {
                    @Override
                    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
                        return new VertxPropertyDataFetcher(environment.getFieldDefinition().getName());
                    }
                })
                .scalar(ScalarDataTypes.instantScalar)
                .scalar(ExtendedScalars.Json)

                .type("Query", builder -> {
                    VertxDataFetcher<JsonObject> getData = new VertxDataFetcher<>(this::getData);
                    return builder
                            .dataFetcher("getData", getData);

                })
                .build();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private void getData(DataFetchingEnvironment dataFetchingEnvironment, Promise<JsonObject> tPromise) {
        final String ia = dataFetchingEnvironment.getArgument("ia");
        if (ia == null) {
            tPromise.fail(new Exception("ia is mandatory field"));
            return;
        } else {
            String s = "{\"effective_timestamp\":\"2020-01-22T00:00:00Z\",\"end_timestamp\":\"9999-12-31T00:00:00Z\",\"jurisdiction\":{\"category\":\"COUNTRY\",\"name\":\"US\"},\"product\":null,\"experiences\":[{\"available_to\":\"OWNER\",\"values\":[{\"digital_assets\":[{\"category\":\"CARD-ART\",\"default\":true,\"identifier\":\"NUS000000174\"}],\"identifier\":\"d60b7e12-b5df-4d27-a102-02e1fd95d14f\",\"legal_names\":null,\"name\":null,\"features\":[{\"terms\":[{\"name\":\"test-fee\",\"identifier\":\"220ca59a-6c66-4845-b4b7-dc7c3c6b6170\",\"legal_names\":[{\"language\":\"en\",\"value\":\"test-fee(fixed-finance-charge)\"}],\"values\":[{\"calculation_method\":\"PERCENTAGE\",\"percentage\":{\"derived_from\":\"CAPPED-APR\",\"value\":1.33,\"of\":\"TEST-AMOUNT\"}}]}],\"configurations\":[{\"identifier\":\"1db0b4c2-0d61-4790-867d-6242a07ba29e\",\"category\":\"feature\",\"applicable_to\":\"fd78b551-bf03-4dff-8542-97c250cddbac\",\"name\":\"test-it-market-offer-id\",\"values\":[{\"value\":\"03LF\",\"jurisdiction\":{\"name\":\"US\",\"category\":\"COUNTRY\"}}]}],\"embedded\":false,\"identifier\":\"fd78b551-bf03-4dff-8542-97c250cddbac\",\"legal_names\":[{\"language\":\"en\",\"value\":\"Test It\"}],\"name\":\"test-it\",\"related_features\":[{\"context\":\"CHILD\"}]},{\"terms\":[{\"name\":\"minimum-test-amount\",\"identifier\":\"8f88e34d-5eb9-41ae-ae2f-00e5897b9b9d\",\"legal_names\":[{\"language\":\"en\",\"value\":\"minimum-test-amount\"}],\"values\":[{\"maximum\":{},\"minimum\":{\"calculation_method\":\"FIXED-AMOUNT\",\"fixed_amount\":{\"amount\":100,\"currency\":\"USD\"}}}]},{\"name\":\"percentage-of-statement-balance\",\"identifier\":\"9ab5459b-f20c-40f8-b99d-908c9537a520\",\"legal_names\":[{\"language\":\"en\",\"value\":\"percentage-of-statement-balance\"}],\"values\":[{\"maximum\":{\"calculation_method\":\"PERCENTAGE\",\"percentage\":{\"value\":85,\"of\":\"statement-balance\"}},\"minimum\":{}}]}],\"configurations\":null,\"embedded\":false,\"identifier\":\"d861cab4-2ccb-4497-9de6-623b8f06c148\",\"legal_names\":[{\"language\":\"en\",\"value\":\"Test It\"}],\"name\":\"test-it-amount\",\"related_features\":[{\"context\":\"PARENT\"}]},{\"terms\":null,\"configurations\":null,\"embedded\":false,\"identifier\":\"e81e079d-785e-4d4d-9b48-ace1126a2cda\",\"legal_names\":[{\"language\":\"en\",\"value\":\"Test It\"}],\"name\":\"test-it-transaction\",\"related_features\":[{\"context\":\"PARENT\"}]}]}]}]}";
            tPromise.complete(new JsonObject(s));
            return;
        }

    }
}
