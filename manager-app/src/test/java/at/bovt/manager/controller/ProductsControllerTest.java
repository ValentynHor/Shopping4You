package at.bovt.manager.controller;

import at.bovt.manager.client.BadRequestException;
import at.bovt.manager.client.ProductsRestClient;
import at.bovt.manager.controller.payload.NewProductPayload;
import at.bovt.manager.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductsController")
class ProductsControllerTest {
    @Mock
    ProductsRestClient productsRestClient;
    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsList_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();
        var filter = "ware";

        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "WareNr%d".formatted(i),
                        "Beschreibung %d".formatted(i)))
                .toList();

        doReturn(products).when(this.productsRestClient).findAllProducts(filter);

        // when
        var result = this.controller.getProductsList(model, filter);

        // then
        assertEquals("catalogue/products/list", result);
        assertEquals(filter, model.getAttribute("filter"));
        assertEquals(products, model.getAttribute("products"));
    }

    @Test
    void getNewProductPage_ReturnsNewProductPage () {
        // given

        // when
        var result = this.controller.getNewProductPage();

        // then
        assertEquals("catalogue/products/new_product", result);
    }

    @Test
    void createProduct_RequestIsValid_ReturnsRedirectionToProductPage() {
        // given
        var payload = new NewProductPayload("Neue Ware", "Beschreibung");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doReturn(new Product(1, "Neue Ware", "Beschreibung"))
                .when(this.productsRestClient)
                .createProduct("Neue Ware", "Beschreibung");

        // when
        var result = this.controller.createProduct(payload, model, response);

        // then
        assertEquals("redirect:/catalogue/products/1", result);

        verify(this.productsRestClient).createProduct("Neue Ware", "Beschreibung");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors() {
        // given
        var payload = new NewProductPayload("  ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Error1", "Error1")))
                .when(this.productsRestClient)
                .createProduct("  ", null);

        // when
        var result = this.controller.createProduct(payload, model, response);

        // then
        assertEquals("catalogue/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Error1", "Error1"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        verify(this.productsRestClient).createProduct("  ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }
}