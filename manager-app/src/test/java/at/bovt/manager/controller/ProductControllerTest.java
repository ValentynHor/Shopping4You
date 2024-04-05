package at.bovt.manager.controller;

import at.bovt.manager.client.BadRequestException;
import at.bovt.manager.client.ProductsRestClient;
import at.bovt.manager.controller.payload.UpdateProductPayload;
import at.bovt.manager.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductsRestClient productsRestClient;
    @InjectMocks
    ProductController controller;

    @Test
    void product_ProductExists_ReturnsProduct() {
        // given
        var product = new Product(1, "Ware1", "Beschreibung1");

        doReturn(Optional.of(product)).when(this.productsRestClient).findProduct(1);

        // when
        var result = this.controller.product(1);

        // then
        assertEquals(product, result);

        verify(this.productsRestClient).findProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void product_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given

        // when
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.product(1));

        // then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());

        verify(this.productsRestClient).findProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProduct_ReturnsProductPage() {
        // given

        // when
        var result = this.controller.getProduct();

        // then
        assertEquals("catalogue/products/product", result);

        verifyNoInteractions(this.productsRestClient);
    }

    @Test
    void getProductEditPage_ReturnsProductEditPage() {
        // given

        // when
        var result = this.controller.getProductEditPage();

        // then
        assertEquals("catalogue/products/edit", result);

        verifyNoInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsValid_RedirectsToProductPage() {
        // given
        var product = new Product(1, "Ware1", "Beschreibung1");
        var payload = new UpdateProductPayload("WareUpdated", "BeschreibungUpdated");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        // when
        var result = this.controller.updateProduct(product, payload, model, response);

        // then
        assertEquals("redirect:/catalogue/products/1", result);

        verify(this.productsRestClient).updateProduct(1, "WareUpdated", "BeschreibungUpdated");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage() {
        // given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        var payload = new UpdateProductPayload("   ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient).updateProduct(1, "   ", null);

        // when
        var result = this.controller.updateProduct(product, payload, model, response);

        // then
        assertEquals("catalogue/products/edit", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        verify(this.productsRestClient).updateProduct(1, "   ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void deleteProduct_RedirectsToProductsListPage() {
        // given
        var product = new Product(1, "Ware", "Beschreibung");

        // when
        var result = this.controller.deleteProduct(product);

        // then
        assertEquals("redirect:/catalogue/products/list", result);

        verify(this.productsRestClient).deleteProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

}