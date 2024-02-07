package com.ss.model;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "order_tbl")
@Data
public class Order {
    @Id
    private UUID id;

    @Column(name = "title")
    public String title;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private Boolean isDeleted;

}
