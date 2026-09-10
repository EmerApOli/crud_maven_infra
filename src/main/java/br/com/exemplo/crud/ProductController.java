package br.com.exemplo.crud;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import static org.springframework.http.HttpStatus.NOT_FOUND;
@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductRepository repository;
    public ProductController(ProductRepository repository) { this.repository = repository; }
    @GetMapping
    public Page<Product> list(@RequestParam(defaultValue = "0") @Min(0) int page,
                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("id")));
    }
    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Produto não encontrado"));
    }
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        Product product = repository.save(new Product(request));
        return ResponseEntity.created(URI.create("/api/products/" + product.getId())).body(product);
    }
    @PutMapping("/{id}") @Transactional
    public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product product = get(id);
        product.update(request);
        return repository.save(product);
    }
    @DeleteMapping("/{id}") @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.delete(get(id));
        return ResponseEntity.noContent().build();
    }
}
