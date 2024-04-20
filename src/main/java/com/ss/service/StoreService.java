package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.Store;
import com.ss.model.StoreModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface StoreService {
    StoreModel create(Store request);

    StoreModel update(UUID id, Store request);

    void delete(UUID id);

    PageResponse<StoreModel> search(String keyword, PageCriteria pageCriteria);

    StoreModel findById(UUID id);

    Set<StoreModel> findByIds(List<UUID> ids);

    List<StoreModel> findByNameIn(Collection<String> storeNames);
}
