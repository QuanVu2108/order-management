package com.ss.dto.response;

import com.ss.enums.OrderStatus;
import com.ss.model.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;

    private String code;

    private Long date;

    private Long totalQuantity;

    private Double totalCost;

    private Double actualCost;

    private Double incentive;

    private String note;

    private Long receivedQuantity;

    private OrderStatus status;

    private BasicModelResponse createdBy;

    private Instant updatedAt;

    private List<OrderItemRes> items;

    @Data
    public static class OrderItemRes {

        private UUID id;

        private String code;

        private StoreModel store;

        public ProductRes product;

        @Data
        public static class ProductRes {

            public long id;

            public String code;

            public String productNumber;

            public String name;

            private ProductRes(ProductModel model) {
                this.id = model.getId();
                this.code = model.getCode();
                this.productNumber = model.getProductNumber();
                this.name = model.getName();
            }
        }

        private OrderItemRes(OrderItemModel orderItem, ProductModel product, StoreModel store) {
            this.id = orderItem.getId();
            this.code = orderItem.getCode();
            this.store = store;
            this.product = new ProductRes(product);
        }
    }

    public OrderResponse(OrderModel order, UserModel user, List<OrderItemModel> orderItems, List<ProductModel> products, List<StoreModel> stores) {
        this.id = order.getId();
        this.code = order.getCode();
        this.date = order.getDate();
        this.totalQuantity = order.getTotalQuantity();
        this.totalCost = order.getTotalCost();
        this.actualCost = order.getActualCost();
        this.incentive = order.getIncentive();
        this.note = order.getNote();
        this.receivedQuantity = order.getReceivedQuantity();
        this.status = order.getStatus();
        this.updatedAt = order.getUpdatedAt();
        if (user != null)
            this.createdBy = new BasicModelResponse(user.getId(), user.getUsername(), user.getFullName(), null);
        if (orderItems != null && !orderItems.isEmpty()) {
            this.items = new ArrayList<>();
            orderItems.forEach(orderItem -> {
                ProductModel product = products.stream()
                        .filter(item -> item.getId() == orderItem.getProduct().getId())
                        .findFirst().orElse(null);
                StoreModel store = stores.stream()
                        .filter(item -> item.getId().equals(orderItem.getStore().getId()))
                        .findFirst().orElse(null);
                this.items.add(new OrderItemRes(orderItem, product, store));
            });
        }
    }
}
