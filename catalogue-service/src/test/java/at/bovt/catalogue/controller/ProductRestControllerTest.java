package at.bovt.catalogue.controller;

import at.bovt.catalogue.controller.payload.UpdateProductPayload;
import at.bovt.catalogue.entity.Product;
import at.bovt.catalogue.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {
    @Mock
    ProductService productService;
    @InjectMocks
    ProductRestController controller;

    @Test
    void getProduct_ProductExists_ReturnsProduct() {
        // given
        var product = new Product(1, "Ware", "Beschreibung");
        doReturn(Optional.of(product)).when(this.productService).findProduct(1);

        // when
        var result = this.controller.getProduct(1);

        // then
        assertEquals(product, result);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given

        // when
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.getProduct(1));

        // then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());
    }

    @Test
    void findProduct_ReturnsProduct() {
        // given
        var product = new Product(1, "Ware", "Beschreibung");

        // when
        var result = this.controller.findProduct(product);

        // then
        assertEquals(product, result);
    }

    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        // given
        var payload = new UpdateProductPayload("Ware", "Beschreibung");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        // when
        var result = this.controller.updateProduct(1, payload, bindingResult);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).updateProduct(1, "Ware", "Beschreibung");
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.updateProduct(1, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.updateProduct(1, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void deleteProduct_ReturnsNoContent() {
        // given

        // when
        var result = this.controller.deleteProduct(1);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).deleteProduct(1);
    }


}
