package com.ss.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ss.dto.request.OrderItemReceivedRequest;
import com.ss.dto.request.OrderItemRequest;
import com.ss.dto.request.OrderItemToolRequest;
import com.ss.dto.request.OrderItemUpdatedDetailRequest;
import com.ss.enums.OrderItemStatus;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

import static com.ss.util.CommonUtil.nullToDefault;

@Entity
@Table(name = "order_item")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "code")
    private String code;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductModel product;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "quantity_order")
    private Long quantityOrder;

    @Column(name = "quantity_reality")
    private Long quantityReality;

    @Column(name = "quantity_received")
    private Long quantityReceived;

    @Column(name = "quantity_sent")
    private Long quantitySent;

    @Column(name = "quantity_incart")
    private Long quantityInCart;

    @Column(name = "quantity_checked")
    private Long quantityChecked;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "cost_reality")
    private Double costReality;

    @Column(name = "cost_total")
    private Double costTotal;

    @Column(name = "incentive")
    private Double incentive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private StoreModel store;

    @Column(name = "delay_day")
    private Long delayDay;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private OrderModel orderModel;

    public OrderItemModel(OrderModel order, int index) {
        this.id = UUID.randomUUID();
        this.code = order.getCode() + "." + index;
        this.status = OrderItemStatus.PENDING;
        this.orderModel = order;
        this.quantityReceived = Long.valueOf(0);
        this.quantitySent = Long.valueOf(0);
        this.quantityInCart = Long.valueOf(0);
        this.quantityChecked = Long.valueOf(0);
    }

    public void update(OrderItemRequest itemRequest, StoreModel store, ProductModel product) {
        this.note = itemRequest.getNote();
        this.quantityOrder = nullToDefault(itemRequest.getQuantity(), this.quantityOrder);
        this.quantityReality = nullToDefault(itemRequest.getQuantity(), this.quantityReality);
        this.cost = nullToDefault(itemRequest.getCost(), this.cost);
        this.costReality = nullToDefault(itemRequest.getCost(), this.costReality);
        this.costTotal = nullToDefault(itemRequest.getCostTotal(), this.costTotal);
        this.incentive = nullToDefault(itemRequest.getIncentive(), this.incentive);
        this.store = nullToDefault(store, this.store);
        this.product = nullToDefault(product, this.product);
    }

    public void updateByTool(OrderItemToolRequest request) {
        this.quantityReality = request.getQuantityReality();
        this.costReality = request.getCostReality();
        this.delayDay = request.getDelayDay();
        this.quantitySent = request.getQuantitySent();
        this.quantityInCart = request.getQuantityInCart();
        this.quantityChecked = request.getQuantityChecked();
        this.status = request.getStatus();
    }

    public void updateByReceive(OrderItemReceivedRequest request) {
        this.quantityReceived = (this.quantityReceived == null ? 0 : this.quantityReceived) + request.getReceivedQuantity();
        if (this.quantitySent <= request.getReceivedQuantity())
            this.quantitySent = Long.valueOf(0);
        else
            this.quantitySent = this.quantitySent - request.getReceivedQuantity();
        if (this.quantityReceived >= this.quantityOrder)
            this.status = OrderItemStatus.DONE;
        this.note = request.getNote();
    }

    public void submitByTool() {
        this.status = OrderItemStatus.SENT;
        this.quantitySent = this.quantityInCart;
        this.quantityInCart = Long.valueOf(0);
    }

    public void updateItemByUpdating(OrderItemUpdatedDetailRequest itemRequest) {
        this.quantityOrder = this.quantityReceived + itemRequest.getQuantity();
        this.quantityReality = this.quantityOrder;
        this.cost = itemRequest.getCost();
        this.costReality = this.cost;
        this.status = OrderItemStatus.PENDING;
    }
}
