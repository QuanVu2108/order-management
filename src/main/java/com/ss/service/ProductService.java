package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.ProductRequest;
import com.ss.dto.response.ProductCheckImportResponse;
import com.ss.dto.response.ProductSaleResponse;
import com.ss.model.ProductModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductModel create(ProductRequest request);

    ProductModel update(long id, ProductRequest request);

    void delete(long id);

    List<ProductModel> importFile(MultipartFile file);

    PageResponse<ProductModel> search(String code, String number, String name, String category, String brand, String color, String size, PageCriteria pageCriteria);

    List<ProductModel> getList(String code, String number, String name, String category, String brand, String color, String size);

    Resource export(String code, String number, String name, String category, String brand, String color, String size);

    ProductModel uploadImage(long id, MultipartFile[] fileRequests);

    List<ProductModel> findByIds(List<Long> productIds);


    List<ProductCheckImportResponse> checkImportFile(MultipartFile file);

    List<ProductCheckImportResponse> checkImportFileKiotviet(MultipartFile file);

    ProductModel getByNumber(String number);

    void generateQRCode();

    ProductSaleResponse getProductInfoBySale(long id);
}
