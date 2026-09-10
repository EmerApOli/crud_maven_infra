package br.com.exemplo.crud;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductRequest(
    @NotBlank @Size(max = 120) String name,
    @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price
) {}
