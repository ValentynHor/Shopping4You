package at.bovt.catalogue.controller;

import at.bovt.catalogue.controller.payload.NewProductPayload;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsRestControllerTest {
    @Mock
    ProductService productService;
    @InjectMocks
    ProductsRestController controller;

    @Test
    void findProduct_ReturnsProductsList() {
        // given
        var filter = "ware";

        doReturn(List.of(new Product(1, "Ware1", "Beschreibung1"),
                new Product(2, "Ware2", "Beschreibung2")))
                .when(this.productService).findAllProducts("ware");

        // when
        var result = this.controller.findProducts(filter);

        // then
        assertEquals(List.of(new Product(1, "Ware1", "Beschreibung1"),
                new Product(2, "Ware2", "Beschreibung2")), result);
    }

    @Test
    void createProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        // given
        var payload = new NewProductPayload("Neue Ware", "Neue Beschreibung");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        doReturn(new Product(1, "Neue Ware", "Neue Beschreibung"))
                .when(this.productService).createProduct("Neue Ware", "Neue Beschreibung");

        // when
        var result = this.controller.createProduct(payload, bindingResult, uriComponentsBuilder);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("http://localhost/catalogue-api/products/1"), result.getHeaders().getLocation());
        assertEquals(new Product(1, "Neue Ware", "Neue Beschreibung"), result.getBody());

        verify(this.productService).createProduct("Neue Ware", "Neue Beschreibung");
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsBadRequest() {
        // given
        var payload = new NewProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        // given
        var payload = new NewProductPayload("   ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }
}
