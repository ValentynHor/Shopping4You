package at.bovt.manager.controller;

import at.bovt.manager.controller.payload.NewProductPayload;
import at.bovt.manager.entity.Product;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 12345)
class ProductsControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    void getProductList_ReturnsProductsListPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/list")
                .queryParam("filter", "ware")
                .with(user("v.hor").roles("MANAGER"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("ware"))
                .willReturn(WireMock.ok("""
                        [
                            {"id": 1, "title": "Ware1", "details": "Beschreibung1"},
                            {"id": 2, "title": "Ware2", "details": "Beschreibung2"}
                        ]""").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/list"),
                        model().attribute("filter", "ware"),
                        model().attribute("products", List.of(
                                new Product(1, "Ware1", "Beschreibung1"),
                                new Product(2, "Ware2", "Beschreibung2")
                        ))
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("ware")));
    }

    @Test
    void getProductList_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/list")
                .queryParam("filter", "ware")
                .with(user("v.hor"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void getNewProductPage_ReturnsProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/create")
                .with(user("v.hor").roles("MANAGER"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/new_product")
                );
    }

    @Test
    void getNewProductPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/create")
                .with(user("v.hor"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void createProduct_RequestIsValid_RedirectsToProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "Neue Ware")
                .param("details", "Beschreibung")
                .with(user("v.hor").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                             "title": "Neue Ware",
                              "details": "Beschreibung"
                        }"""))
                .willReturn(WireMock.created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": 1,
                                    "title": "Neue Ware",
                                    "details": "Beschreibung"
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        header().string(HttpHeaders.LOCATION, "/catalogue/products/1")
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                             "title": "Neue Ware",
                             "details": "Beschreibung"
                        }""")));
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsNewProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "   ")
                .with(user("v.hor").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "   ",
                            "details": null
                        }"""))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Error1", "Error2"]
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("catalogue/products/new_product"),
                        model().attribute("payload", new NewProductPayload("   ", null)),
                        model().attribute("errors", List.of("Error1", "Error2"))
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "   ",
                            "details": null
                        }""")));
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "Neue Ware")
                .param("details", "Beschreibung")
                .with(user("j.daniels"))
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}
