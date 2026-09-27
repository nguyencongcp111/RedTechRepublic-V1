package com.example.product_service.controller;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.service.CloudinaryImageService;
import com.example.product_service.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@RestController @RequestMapping("/api/products") @RequiredArgsConstructor
public class ProductMediaController {
    private final ProductServiceImpl products;
    private final CloudinaryImageService images;
    @GetMapping("/stats")
    public Map<String, Object> stats() { return products.stats(); }
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse upload(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        products.getProductById(id);
        var image = images.upload(id, file);
        return products.setImage(id, image.url(), image.publicId());
    }
    @DeleteMapping("/{id}/image")
    public ProductResponse remove(@PathVariable UUID id) { return products.setImage(id, null, null); }
}
