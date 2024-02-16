package com.ss.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paging {
    private long totalCount;
    private int pageIndex;
    private int pageSize;
    private int totalPages;

    public long getTotalPages() {
        long mod = totalCount % (long) pageSize;
        long totalPages = totalCount / pageSize;
        totalPages += mod > 0 ? 1 : 0;
        return totalPages;
    }
}
