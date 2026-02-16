package org.ecommerce.v1.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorType errorType = ErrorType.INTERNAL_ERROR;
        if (ex instanceof NotFoundException) {
            errorType = ErrorType.NOT_FOUND;
        } else if (ex instanceof IllegalArgumentException) {
            errorType = ErrorType.BAD_REQUEST;
        }
        return GraphqlErrorBuilder.newError(env)
                .errorType(errorType)
                .message(ex.getMessage())
                .build();
    }
}
