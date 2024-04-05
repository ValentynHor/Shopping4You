package at.bovt.manager.controller;

import at.bovt.manager.controller.payload.UpdateProductPayload;
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
class ProductControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getProduct_ProductExists_ReturnsProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("v.hor").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Ware",
                            "details": "Beschreibung"
                        }
                        """)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/product"),
                        model().attribute("product", new Product(1, "Ware", "Beschreibung"))
                );
    }

    @Test
    void getProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("v.hor").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Ware ist nicht gefunden")
                );
    }

    @Test
    void getProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
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
    void getProductEditPage_ProductExists_ReturnsProductEditPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("v.hor").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Ware",
                            "details": "Beschreibung"
                        }
                        """)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "Ware", "Beschreibung"))
                );
    }

    @Test
    void getProductEditPage_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("v.hor").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Ware ist nicht gefunden")
                );
    }

    @Test
    void getProductEditPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
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
    void updateProduct_RequestIsValid_RedirectsToProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "neue Ware")
                .param("details", "Beschreibung")
                .with(user("v.hor").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "neue Ware",
                            "details": "Beschreibung"
                        }
                        """)));

        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "neue Ware",
                            "details": "Beschreibung"
                        }"""))
                .willReturn(WireMock.noContent()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/1")
                );

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                           "title": "neue Ware",
                            "details": "Beschreibung"
                        }""")));
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "   ")
                .with(user("v.hor").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "neue Ware",
                            "details": "Beschreibung"
                        }
                        """)));

        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
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
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "neue Ware", "Beschreibung")),
                        model().attribute("errors", List.of("Error1", "Error2")),
                        model().attribute("payload", new UpdateProductPayload("   ", null))
                );

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "   ",
                            "details": null
                        }""")));
    }

    @Test
    void updateProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Name")
                .param("details", "Beschreibung")
                .with(user("v.hor").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Ware ist nicht gefunden")
                );
    }

    @Test
    void updateProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Name")
                .param("details", "Beschreibung")
                .with(user("v.hor"))
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void deleteProduct_ProductExists_RedirectsToProductsListPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .with(user("v.hor").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Ware",
                            "details": "Beschreibung"
                        }
                        """)));

        WireMock.stubFor(WireMock.delete("/catalogue-api/products/1")
                .willReturn(WireMock.noContent()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/list")
                );

        WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1")));
    }

    @Test
    void deleteProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .with(user("v.hor").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Ware ist nicht gefunden")
                );
    }

    @Test
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .with(user("v.hor"))
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