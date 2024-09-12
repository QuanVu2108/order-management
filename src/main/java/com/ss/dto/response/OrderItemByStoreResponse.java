
package com.ss.dto.response;

import com.ss.model.OrderModel;
import com.ss.model.StoreModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class OrderItemByStoreResponse {
    private StoreModel store;
    private Set<UUID> orderIds;
    private long itemCnt;

    public OrderItemByStoreResponse(StoreModel store) {
        this.store = store;
        this.orderIds = new HashSet<>();
        this.itemCnt = 0;
    }

    public void addOrder(OrderModel order) {
        if (order != null)
            this.orderIds.add(order.getId());
    }

    public void updateProductCnt(Long itemQuantity) {
        this.itemCnt = this.itemCnt + itemQuantity;
    }
}
