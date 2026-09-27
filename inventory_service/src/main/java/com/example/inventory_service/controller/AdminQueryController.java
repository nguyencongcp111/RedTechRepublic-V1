package com.example.inventory_service.controller;
import com.example.inventory_service.service.AdminQueryService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/inventory") @RequiredArgsConstructor @Validated
public class AdminQueryController {
    private final AdminQueryService queries;
    @GetMapping
    public Map<String,Object> list(@RequestParam(defaultValue="0") @Min(0) int page,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return queries.list(page,size); }
    @GetMapping("/stats")
    public Map<String,Object> stats() { return queries.stats(); }
}
