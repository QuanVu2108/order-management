package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.ProductPropertyRequest;
import com.ss.dto.request.ProductRequest;
import com.ss.dto.response.ProductCheckImportResponse;
import com.ss.dto.response.ServiceResponse;
import com.ss.enums.ExportFormat;
import com.ss.enums.ProductPropertyType;
import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;
import com.ss.service.ProductPropertyService;
import com.ss.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductController {

    private final ProductPropertyService productPropertyService;

    private final ProductService productService;

    @PostMapping
    ServiceResponse<ProductModel> create(@RequestBody @Valid ProductRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.create(request));
    }

    @PutMapping("/{id}")
    ServiceResponse<ProductModel> update(@PathVariable @Valid long id,
                                         @RequestBody @Valid ProductRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.update(id, request));
    }

    @PostMapping("/upload-image/{id}")
    ServiceResponse<ProductModel> uploadImage(@PathVariable @Valid long id,
                                              @RequestPart(name = "fileRequests") MultipartFile[] fileRequests) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.uploadImage(id, fileRequests));
    }

    @DeleteMapping("/{id}")
    ServiceResponse<Void> delete(@PathVariable @Valid long id) {
        productService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @PostMapping("/import")
    ServiceResponse<List<ProductModel>> importFile(MultipartFile file) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.importFile(file));
    }

    @GetMapping("/number/{number}")
    ServiceResponse<ProductModel> getByNumber(@PathVariable @Valid String number) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.getByNumber(number));
    }

    @GetMapping
    PageResponse<ProductModel> search(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "number", required = false) String number,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "size", required = false) String size,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, productService.search(code, number, name, category, brand, color, size, pageCriteria));
    }

    @GetMapping("get-list")
    ServiceResponse<List<ProductModel>> getList(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "number", required = false) String number,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "size", required = false) String size) {
        return ServiceResponse.succeed(HttpStatus.OK, productService.getList(code, number, name, category, brand, color, size));
    }

    @PostMapping("/export")
    ResponseEntity<Resource> export(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "number", required = false) String number,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "size", required = false) String size) {
        ExportFormat format = ExportFormat.EXCEL;
        Resource resource = productService.export(code, number, name, category, brand, color, size);
        String fileName = "product";
        try {
            return ResponseEntity
                    .ok()
                    .contentLength(resource.contentLength())
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=" + fileName + format.getExtension())
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // property
    @PostMapping("/property")
    ServiceResponse<ProductPropertyModel> createProperty(@RequestBody @Valid ProductPropertyRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productPropertyService.create(request));
    }

    @PutMapping("/property/{id}")
    ServiceResponse<ProductPropertyModel> updateProperty(@PathVariable @Valid UUID id,
                                                         @RequestBody @Valid ProductPropertyRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, productPropertyService.update(id, request));
    }

    @DeleteMapping("/property/{id}")
    ServiceResponse<Void> deleteProperty(@PathVariable @Valid UUID id) {
        productPropertyService.delete(id);
        return ServiceResponse.succeed(HttpStatus.OK, null);
    }

    @GetMapping("/property")
    PageResponse<ProductPropertyModel> searchProperty(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "type", required = false) ProductPropertyType type,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, productPropertyService.search(code, name, type, pageCriteria));
    }

    @PostMapping("/check-import-file")
    ServiceResponse<List<ProductCheckImportResponse>> checkImportFile(MultipartFile file) throws Exception {
        return ServiceResponse.succeed(HttpStatus.OK, productService.checkImportFile(file));
    };

    @PostMapping("/check-import-file-kiotviet")
    ServiceResponse<List<ProductCheckImportResponse>> checkImportFileKiotviet(MultipartFile file) throws Exception {
        return ServiceResponse.succeed(HttpStatus.OK, productService.checkImportFileKiotviet(file));
    };

    @PostMapping("/generate-qr-code")
    ServiceResponse<Void> generateQRCode() {
        productService.generateQRCode();
        return ServiceResponse.succeed(HttpStatus.OK, null);
    };
}
