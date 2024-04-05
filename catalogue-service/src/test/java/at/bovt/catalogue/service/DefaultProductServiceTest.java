package at.bovt.catalogue.service;

import at.bovt.catalogue.entity.Product;
import at.bovt.catalogue.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    DefaultProductService service;

    @Test
    void findAllProducts_FilterIsNotSet_ReturnsProductsList() {
        // given
        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Ware%d".formatted(i), "Beschreibung%d".formatted(i)))
                .toList();

        doReturn(products).when(this.productRepository).findAll();

        // when
        var result = this.service.findAllProducts(null);

        // then
        assertEquals(products, result);

        verify(this.productRepository).findAll();
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findAllProducts_FilterIsSet_ReturnsFilteredProductsList() {
        // given
        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Ware%d".formatted(i), "Beschreibung%d".formatted(i)))
                .toList();

        doReturn(products).when(this.productRepository).findAllByTitleLikeIgnoreCase("%ware%");

        // when
        var result = this.service.findAllProducts("ware");

        // then
        assertEquals(products, result);

        verify(this.productRepository).findAllByTitleLikeIgnoreCase("%ware%");
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findProduct_ProductExists_ReturnsNotEmptyOptional() {
        // given
        var product = new Product(1, "Ware", "Beschreibung");

        doReturn(Optional.of(product)).when(this.productRepository).findById(1);

        // when
        var result = this.service.findProduct(1);

        // then
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(product, result.orElseThrow());

        verify(this.productRepository).findById(1);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findProduct_ProductDoesNotExist_ReturnsEmptyOptional() {
        // given
        var product = new Product(1, "Ware", "Beschreibung");

        // when
        var result = this.service.findProduct(1);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(this.productRepository).findById(1);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void createProduct_ReturnsCreatedProduct() {
        // given
        var title = "Ware";
        var details = "Beschreibung";

        doReturn(new Product(1, "Ware", "Beschreibung"))
                .when(this.productRepository).save(new Product(null, "Ware", "Beschreibung"));

        // when
        var result = this.service.createProduct(title, details);

        // then
        Assertions.assertEquals(new Product(1, "Ware", "Beschreibung"), result);

        verify(this.productRepository).save(new Product(null, "Ware", "Beschreibung"));
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void updateProduct_ProductExists_UpdatesProduct() {
        // given
        var productId = 1;
        var product = new Product(1, "Ware", "Beschreibung");
        var title = "neue Ware";
        var details = "neue Beschreibung";

        doReturn(Optional.of(product))
                .when(this.productRepository).findById(1);

        // when
        this.service.updateProduct(productId, title, details);

        // then
        verify(this.productRepository).findById(productId);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void updateProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given
        var productId = 1;
        var title = "neue Ware";
        var details = "Neue Beschreibung";

        // when
        assertThrows(NoSuchElementException.class, () -> this.service
                .updateProduct(productId, title, details));

        // then
        verify(this.productRepository).findById(productId);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void deleteProduct_DeletesProduct() {
        // given
        var productId = 1;

        // when
        this.service.deleteProduct(productId);

        // then
        verify(this.productRepository).deleteById(productId);
        verifyNoMoreInteractions(this.productRepository);
    }
}