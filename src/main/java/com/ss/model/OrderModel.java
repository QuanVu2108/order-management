package com.ss.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ss.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_tbl")
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "title")
    public String title;

    @Column(name = "code")
    public String code;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "orderModel", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItemModel> items;

}
