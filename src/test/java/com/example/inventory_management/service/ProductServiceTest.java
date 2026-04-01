package com.example.inventory_management.service;

import com.example.inventory_management.dto.ProductRequestDto;
import com.example.inventory_management.dto.ProductResponseDto;
import com.example.inventory_management.exception.ResourceNotFoundException;
import com.example.inventory_management.model.Product;
import com.example.inventory_management.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_returnsSavedProductResponse() {
        ProductRequestDto request = new ProductRequestDto("Mouse", "Wireless mouse", new BigDecimal("25.50"), 30);

        Product saved = new Product("Mouse", "Wireless mouse", new BigDecimal("25.50"), 30);
        saved.setName("Mouse");
        saved.setDescription("Wireless mouse");
        saved.setPrice(new BigDecimal("25.50"));
        saved.setStockQuantity(30);

        Product persisted = new Product("Mouse", "Wireless mouse", new BigDecimal("25.50"), 30) {
            @Override
            public Long getId() {
                return 1L;
            }
        };

        when(productRepository.save(any(Product.class))).thenReturn(persisted);

        ProductResponseDto response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Mouse");
        assertThat(response.getStockQuantity()).isEqualTo(30);
    }

    @Test
    void getProductById_throwsWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteProduct_callsRepositoryDeleteById() {
        when(productRepository.existsById(7L)).thenReturn(true);

        productService.deleteProduct(7L);

        verify(productRepository).deleteById(7L);
    }
}
