package com.ss.service.Impl;

import com.ss.enums.OrderItemStatus;
import com.ss.enums.OrderStatus;
import com.ss.model.FileModel;
import com.ss.model.OrderItemModel;
import com.ss.model.OrderModel;
import com.ss.model.ProductModel;
import com.ss.repository.FileRepository;
import com.ss.repository.OrderItemRepository;
import com.ss.repository.OrderRepository;
import com.ss.repository.ProductRepository;
import com.ss.service.AsyncService;
import com.ss.util.StorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.ss.util.QRCodeUtil.generateQRCodeImage;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncServiceImpl implements AsyncService {

    private final OrderItemRepository orderItemRepository;

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final FileRepository fileRepository;

    private final StorageUtil storageUtil;

    @Override
    @Async
    public void updateStatusOrders(OrderModel order, List<UUID> submittedOrderItemIds) {
        List<OrderItemModel> allOrderItems = orderItemRepository.findByOrderModel(order);
        List<OrderItemModel> unsubmittedOrderItems = allOrderItems.stream()
                .filter(item -> !submittedOrderItemIds.contains(item.getId()) && OrderItemStatus.PENDING.equals(item.getStatus()))
                .collect(Collectors.toList());
        if (unsubmittedOrderItems.isEmpty()) {
            order.setStatus(OrderStatus.DONE);
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);
        }
    }

    @Override
    @Async
    public void generateQRCodeProduct(List<ProductModel> products) {
        if (products != null && !products.isEmpty()) {
            products.forEach(product -> {
                product.setQrCode(generateQRCodeImage(String.valueOf(product.getId())));
            });
            productRepository.saveAll(products);
        }
    }

    @Override
    @Async
    @Transactional
    public void createImageProduct(List<ProductModel> updatedProducts, Map<String, List<String>> productImageUrlOriginal) {
        List<String> fileUrlOriginals = new ArrayList<>();
        Map<String, Set<String>> imageUrlOriginalProducts = new HashMap<>();
        productImageUrlOriginal.forEach((key, urlOriginals) -> {
            if (urlOriginals != null && !urlOriginals.isEmpty()) {
                fileUrlOriginals.addAll(urlOriginals);
                urlOriginals.forEach(urlOriginal -> {
                    Set<String> productNumbers = imageUrlOriginalProducts.get(urlOriginal);
                    if (productNumbers == null)
                        productNumbers = Set.of(key);
                    else {
                        productNumbers.add(key);
                    }
                    imageUrlOriginalProducts.put(urlOriginal, productNumbers);
                });
            }
        });
        List<FileModel> existedFiles = fileRepository.findByUrlOriginalIn(fileUrlOriginals);

        List<FileModel> newFiles = new ArrayList<>();
        imageUrlOriginalProducts.forEach((imageUrlOriginal, productNumbers) -> {
            List<FileModel> files = existedFiles.stream()
                    .filter(item -> item.getProduct() != null && item.getUrlOriginal() != null && item.getUrlOriginal().equals(imageUrlOriginal))
                    .collect(Collectors.toList());
            if (!files.isEmpty()) {
                String imageUrl = files.get(0).getUrl();
                List<String> existedProductNumbers = files.stream().map(item -> item.getProduct().getProductNumber()).collect(Collectors.toList());
                productNumbers.forEach(productNumber -> {
                    if (!existedProductNumbers.contains(productNumber)) {
                        ProductModel product = updatedProducts.stream()
                                .filter(item -> item.getProductNumber().equals(productNumber))
                                .findFirst().orElse(null);
                        if (product != null) {
                            FileModel image = FileModel.builder()
                                    .id(UUID.randomUUID())
                                    .name(product.getName())
                                    .product(product)
                                    .url(imageUrl)
                                    .urlOriginal(imageUrlOriginal)
                                    .build();
                            newFiles.add(image);
                        }
                    }
                });
            } else {
                String fileName = productNumbers.stream().findFirst().orElse(null);
                if (fileName != null) {
                    String fileUrl = storageUtil.uploadFileByUrl(fileName, imageUrlOriginal);
                    updatedProducts.forEach(updatedProduct -> {
                        if (productNumbers.contains(updatedProduct.getProductNumber())) {
                            FileModel newFile = FileModel.builder()
                                    .id(UUID.randomUUID())
                                    .name(updatedProduct.getName())
                                    .url(fileUrl)
                                    .urlOriginal(imageUrlOriginal)
                                    .product(updatedProduct)
                                    .build();
                            newFiles.add(newFile);
                        }
                    });
                }
            }
        });
        fileRepository.saveAll(newFiles);
    }

}
