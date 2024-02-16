package com.ss.dto.pagination;

import org.springframework.data.domain.Pageable;

public interface PageableMapper<T> {
    Pageable toPageable(T criteria);
}
