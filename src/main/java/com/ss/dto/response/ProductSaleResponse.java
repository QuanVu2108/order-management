
package com.ss.dto.response;

import com.ss.model.StoreItemModel;
import com.ss.model.StoreModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSaleResponse {
    private ProductResponse product;

    private List<StoreInfo> stores;

    public void setStoreInfo(Set<StoreModel> ownerStores, List<StoreItemModel> storeItems) {
        List<StoreInfo> stores = new ArrayList<>();
        storeItems.forEach(storeItem -> {
            StoreInfo storeInfo = new StoreInfo(storeItem);
            storeInfo.setOwn(ownerStores.stream().anyMatch(item -> item.getId().equals(storeItem.getStore().getId())));
            stores.add(storeInfo);
        });
        this.stores = stores;
    }

    @Data
    private static class StoreInfo {
        private StoreModel store;
        private Long quantity;
        private boolean isOwn;

        private StoreInfo(StoreItemModel storeItem) {
            this.store = storeItem.getStore();
            this.quantity = storeItem.getQuantity();
        }
    }
}
