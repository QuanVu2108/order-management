package com.ss.dto.response;

import com.ss.enums.StoreItemType;
import com.ss.model.OrderModel;
import com.ss.model.ProductModel;
import com.ss.model.StoreItemModel;
import com.ss.model.StoreModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemResponse {
    @Id
    private UUID id;

    private StoreModel store;

    private ProductModel product;

    private Long quantity;

    private Double cost;

    private String note;

    private StoreItemType type;

    private Long time;

    private StoreModel targetStore;

    private BasicModelResponse order;

    public StoreItemResponse(StoreItemModel storeItem, List<ProductModel> products) {
        this.id = storeItem.getId();
        this.store = storeItem.getStore();
        if (storeItem.getProductId() != null)
            this.product = products.stream()
                    .filter(item -> item.getId() == storeItem.getProductId())
                    .findFirst().orElse(null);
        this.quantity = storeItem.getQuantity();
        this.cost = storeItem.getCost();
        this.note = storeItem.getNote();
        this.type = storeItem.getType();
        this.time = storeItem.getTime();
        this.targetStore = storeItem.getTargetStore();
        OrderModel order = storeItem.getOrder();
        if (order != null)
            this.order = new BasicModelResponse(order.getId(), order.getCode(), null, order.getDate());
    }
}
