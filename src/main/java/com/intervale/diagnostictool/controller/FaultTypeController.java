package com.intervale.diagnostictool.controller;

import com.intervale.diagnostictool.dto.FaultTypeDto;
import com.intervale.diagnostictool.dto.request.FaultTypeRequest;
import com.intervale.diagnostictool.service.FaultTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fault-types")
@RequiredArgsConstructor
@Tag(name = "Fault Type Management", description = "APIs for managing fault types")
public class FaultTypeController {

    private final FaultTypeService faultTypeService;

    @GetMapping
    @Operation(summary = "Get all fault types")
    public ResponseEntity<List<FaultTypeDto>> getAllFaultTypes() {
        return ResponseEntity.ok(faultTypeService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a fault type by ID")
    public ResponseEntity<FaultTypeDto> getFaultTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(faultTypeService.findById(id));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get fault types by device category ID")
    public ResponseEntity<List<FaultTypeDto>> getFaultTypesByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(faultTypeService.findByDeviceCategoryId(categoryId));
    }

    @PostMapping
    @Operation(summary = "Create a new fault type")
    public ResponseEntity<FaultTypeDto> createFaultType(@Valid @RequestBody FaultTypeRequest request) {
        return new ResponseEntity<>(faultTypeService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing fault type")
    public ResponseEntity<FaultTypeDto> updateFaultType(
            @PathVariable Long id, 
            @Valid @RequestBody FaultTypeRequest request) {
        return ResponseEntity.ok(faultTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a fault type")
    public void deleteFaultType(@PathVariable Long id) {
        faultTypeService.delete(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search fault types by name or code")
    public ResponseEntity<List<FaultTypeDto>> searchFaultTypes(@RequestParam String query) {
        return ResponseEntity.ok(faultTypeService.search(query));
    }
}
