package com.ss.model;

import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.StoreItemDetailRequest;
import com.ss.enums.StoreItemType;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "store_item")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreItemModel extends AuditModel {
    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private StoreModel store;

    @OneToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductModel product;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private StoreItemType type;

    @Column(name = "time")
    private Long time;

    @OneToOne
    @JoinColumn(name = "target_store_id", referencedColumnName = "id")
    private StoreModel targetStore;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private OrderModel order;

    public StoreItemModel(StoreModel store, ProductModel product, StoreItemType type) {
        this.id = UUID.randomUUID();
        this.time = System.currentTimeMillis();
        this.store = store;
        this.product = product;
        this.type = type;
        this.quantity = Long.valueOf(0);
    }

    public void update(StoreItemDetailRequest itemRequest, StoreModel targetStore, OrderModel order) {
        if (targetStore != null)
            this.targetStore = targetStore;
        if (order != null)
            this.order = order;
        if (itemRequest != null) {
            this.quantity = itemRequest.getQuantity();
            this.cost = itemRequest.getCost();
            this.note = itemRequest.getNote();
        }
    }

    public void updateInventory(StoreItemType type, Long quantity) {
        if (type.equals(StoreItemType.EXPORT))
            this.quantity = this.quantity - quantity;
        else
            this.quantity = this.quantity + quantity;
    }
}
