package com.ss.model;


import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_tbl", indexes = {
        @Index(name = "idx_url_original", columnList = "url_original"),
        @Index(name = "idx_product_id", columnList = "product_id")
})
@Where(clause = "deleted = false")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileModel extends AuditModel {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "url", columnDefinition = "text")
    private String url;

    @Column(name = "url_original", columnDefinition = "text")
    private String urlOriginal;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductModel product;

}
