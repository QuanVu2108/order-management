package com.ss.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageCriteria {
    public static final String ASC_SYMBOL = "+";
    public static final String DESC_SYMBOL = "-";
    public static final String DEFAULT_SORT_BY_LAST_MODIFIED_DATE = "updatedAt";

    @Min(value = 1, message = "page must be greater than 0")
    @Max(value = 1000000, message = "page must be less than 1000000")
    @ParamName("pageIndex")
    private Integer pageIndex = 1;
    @Min(value = 1, message = "limit must be greater than 0")
    @Max(value = 250, message = "limit must be less than or equal to 250")
    @ParamName("pageSize")
    private Integer pageSize = 250;
    @ParamName("sort")
    private List<String> sort = new ArrayList<>();

    public void setPageSize(Integer pageSize) {
        if (pageSize != null) {
            this.pageSize = pageSize;
        }
    }

    public void setPageIndex(Integer pageIndex) {
        if (pageIndex != null) {
            this.pageIndex = pageIndex;
        }
    }
}
