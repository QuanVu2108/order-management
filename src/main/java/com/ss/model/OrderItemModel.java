package com.ss.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ss.dto.request.OrderItemRequest;
import com.ss.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Where(clause = "deleted = false")
@Data
@Builder
@AllArgsConstructor
public class OrderItemModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "name")
    public String name;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "quantity_order")
    public Long quantityOrder;

    @Column(name = "quantity_reality")
    public Long quantityReality;

    @Column(name = "price_order")
    public Double priceOrder;

    @Column(name = "price_reality")
    public Double priceReality;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<FileModel> files;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private OrderModel orderModel;

    public OrderItemModel() {
        this.id = UUID.randomUUID();
        this.status = OrderItemStatus.NEW;
    }

    public void update(OrderItemRequest request) {
        this.name = request.getName();
        this.content = request.getContent();
        this.quantityOrder = request.getQuantityOrder();
        this.quantityReality = request.getQuantityReality();
        this.priceOrder = request.getPriceOrder();
        this.priceReality = request.getPriceReality();
        this.status = request.getStatus();
        setUpdatedAt(Instant.now());
    }

    public void updateByTool(OrderItemRequest request) {
        this.quantityOrder = request.getQuantityOrder();
        this.quantityReality = request.getQuantityReality();
        this.priceOrder = request.getPriceOrder();
        this.priceReality = request.getPriceReality();
        this.status = request.getStatus();
        setUpdatedAt(Instant.now());
    }
}
