package com.ss.repository;

import com.ss.model.FileModel;
import com.ss.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileModel, UUID> {
    List<FileModel> findByUrlOriginalIn(List<String> urlOriginals);

    List<FileModel> findByProduct(ProductModel product);

    List<FileModel> findByProductIn(List<ProductModel> products);
}
