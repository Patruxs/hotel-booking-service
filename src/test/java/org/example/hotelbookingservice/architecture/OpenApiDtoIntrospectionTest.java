package org.example.hotelbookingservice.architecture;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.response.RevenueStatisticResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.CurrentUserResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.PageResponse;
import org.example.hotelbookingservice.dto.response.booking.operations.BookingResponse;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiDtoIntrospectionTest {

    @Test
    void openApiDtos_whenResolved_shouldExposeSchemas() {
        List<Class<?>> dtoTypes = List.of(
                BookingCreateRequest.class,
                BookingResponse.class,
                CurrentUserResponse.class,
                PageResponse.class,
                ListResponse.class,
                RevenueStatisticResponse.class
        );

        dtoTypes.forEach(type -> {
            ResolvedSchema schema = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(type));

            assertThat(schema)
                    .as("resolved schema for %s", type.getName())
                    .isNotNull();
            assertThat(schema.schema)
                    .as("root schema for %s", type.getName())
                    .isNotNull();
        });
    }
}
