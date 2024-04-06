
package com.ss.dto.response;

import com.ss.model.StoreModel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemByStoreResponse {
    private StoreModel store;
    private long orderCnt;
    private long itemCnt;

    public OrderItemByStoreResponse(StoreModel store) {
        this.store = store;
        this.orderCnt = 0;
        this.itemCnt = 0;
    }

    public void updateOrderCnt() {
        this.orderCnt++;
    }

    public void updateProductCnt(Long itemQuantity) {
        this.itemCnt = this.itemCnt + itemQuantity;
    }
}
