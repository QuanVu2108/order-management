package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.ProductRequest;
import com.ss.dto.response.ProductCheckImportResponse;
import com.ss.dto.response.ProductResponse;
import com.ss.dto.response.ProductSaleResponse;
import com.ss.model.ProductModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);

    ProductResponse update(long id, ProductRequest request);

    void delete(long id);

    void importFile(MultipartFile file);

    PageResponse<ProductResponse> search(String code, String number, String name, String category, String brand, String color, String size, PageCriteria pageCriteria);

    List<ProductResponse> getList(String code, String number, String name, String category, String brand, String color, String size);

    Resource export(String code, String number, String name, String category, String brand, String color, String size);

    ProductResponse uploadImage(long id, MultipartFile[] fileRequests);

    List<ProductModel> findByIds(List<Long> productIds);


    List<ProductCheckImportResponse> checkImportFile(MultipartFile file);

    List<ProductCheckImportResponse> checkImportFileKiotviet(MultipartFile file);

    ProductResponse getByNumber(String number);

    void generateQRCode();

    ProductSaleResponse getProductInfoBySale(long id);
}
