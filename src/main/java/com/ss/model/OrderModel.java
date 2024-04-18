package com.ss.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ss.dto.request.OrderRequest;
import com.ss.enums.OrderStatus;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_tbl")
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "date")
    private Long date;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "actual_cost")
    private Double actualCost;

    @Column(name = "incentive")
    private Double incentive;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "received_quantity")
    private Long receivedQuantity;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "orderModel", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItemModel> items;

    public OrderModel(String code) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.date = System.currentTimeMillis();
        LocalDate now = LocalDate.now();
        this.year = now.getYear();
        this.month = now.getMonthValue();
    }

    public void update(OrderRequest request) {
        this.totalQuantity = request.getTotalQuantity();
        this.totalCost = request.getTotalCost();
        this.incentive = request.getIncentive();
        this.actualCost = request.getActualCost();
        this.status = request.getStatus();
    }
}
