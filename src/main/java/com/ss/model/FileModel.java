package com.ss.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_tbl")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private ProductModel product;

}
