package at.bovt.feedback.controller;

import at.bovt.feedback.entity.FavouriteProduct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@Slf4j
@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
class FavouriteProductsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        this.reactiveMongoTemplate.insertAll(List.of(
                new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1,
                        "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"),
                new FavouriteProduct(UUID.fromString("37b79df0-cbda-11ee-b5d0-17231cdeab05"), 2,
                        "3c467d3c-cbda-11ee-aa43-1782cd18c42f"),
                new FavouriteProduct(UUID.fromString("23ff1d58-cbd8-11ee-9f4f-ef497a4e4799"), 3,
                        "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.reactiveMongoTemplate.remove(FavouriteProduct.class).all().block();
    }

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        [
                            {
                                "id": "fe87eef6-cbd7-11ee-aeb6-275dac91de02",
                                "productId": 1,
                                "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                            },
                            {
                                "id": "23ff1d58-cbd8-11ee-9f4f-ef497a4e4799",
                                "productId": 3,
                                "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                            }
                        ]""")
                .consumeWith(document("feedback/favourite_products/findFavouriteProducts",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.STRING)
                                        .description("The ID of the favourite product"),
                                fieldWithPath("[].productId").type(JsonFieldType.NUMBER)
                                        .description("The ID of the product"),
                                fieldWithPath("[].userId").type(JsonFieldType.STRING)
                                        .description("The ID of the user who favourited the product")
                        )
                ));

    }

    @Test
    void findFavouriteProducts_UserIsNotAuthenticated_ReturnsUnauthorized() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/3")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "id": "23ff1d58-cbd8-11ee-9f4f-ef497a4e4799",
                            "productId": 3,
                            "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                        }""")
                .consumeWith(document("feedback/favourite_products_by_product_id",
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.STRING).description("The ID of the favourite product"),
                                fieldWithPath("productId").type(JsonFieldType.NUMBER).description("The ID of the product"),
                                fieldWithPath("userId").type(JsonFieldType.STRING).description("The ID of the user who favourited the product")
                        )
                ));
    }

    @Test
    void findFavouriteProductByProductId_UserIsNotAuthenticated_ReturnsUnauthorized() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/3")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void addFavouriteProduct_RequestIsValid_ReturnsCreatedFavouriteProduct() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 4
                        }""")
                .exchange()
                // then
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "productId": 4,
                            "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                        }""").jsonPath("$.id").exists()
                .consumeWith(document("feedback/add-favourite-product",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("productId").type(JsonFieldType.NUMBER).description("The ID of the product")
                        ),
                        responseFields(
                                fieldWithPath("productId").type(JsonFieldType.NUMBER).description("The ID of the product"),
                                fieldWithPath("userId").type(JsonFieldType.STRING).description("The ID of the user who favourited the product"),
                                fieldWithPath("id").type(JsonFieldType.STRING).description("The ID of the favourite product")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Link to the created favourite product")
                        )
                ));
    }

    @Test
    void addFavouriteProduct_RequestIsInvalid_ReturnsBadRequest() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null
                        }""")
                .exchange()
                // then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().json("""
                        {
                            "errors": ["Ware ist nicht gefunden."]
                        }""");
    }

    @Test
    void addFavouriteProduct_UserIsNotAuthenticated_ReturnsUnauthorized() {
        // given

        // when
        this.webTestClient
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null
                        }""")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void removeProductFromFavourites_ReturnsNoContent() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isNoContent();
    }

    @Test
    void removeProductFromFavourites_UserIsNotAuthenticated_ReturnsUnauthorized() {
        // given

        // when
        this.webTestClient
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }
}