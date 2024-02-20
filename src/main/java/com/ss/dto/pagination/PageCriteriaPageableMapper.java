package com.ss.dto.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class PageCriteriaPageableMapper implements PageableMapper<PageCriteria> {
    @Override
    public Pageable toPageable(PageCriteria criteria) {
        List<Sort.Order> orders = new ArrayList<>();
        if (!CollectionUtils.isEmpty(criteria.getSort())) {
            String sortStr = criteria.getSort().get(criteria.getSort().size() - 1);
            if (StringUtils.hasLength(sortStr)) {
                if (sortStr.startsWith(PageCriteria.DESC_SYMBOL) && sortStr.length() > 1) {
                    orders.add(Sort.Order.desc(sortStr.substring(1)));
                } else if (sortStr.startsWith(PageCriteria.ASC_SYMBOL) && sortStr.length() > 1) {
                    orders.add(Sort.Order.asc(sortStr.substring(1)));
                } else {
                    orders.add(Sort.Order.asc(sortStr));
                }
            }
        }else{
            orders.add(Sort.Order.desc(PageCriteria.DEFAULT_SORT_BY_LAST_MODIFIED_DATE));
        }
        return PageRequest.of(criteria.getPageIndex() - 1, criteria.getPageSize(), Sort.by(orders));
    }
}
