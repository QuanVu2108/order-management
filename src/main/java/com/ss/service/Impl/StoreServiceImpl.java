package com.ss.service.Impl;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageCriteriaPageableMapper;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.pagination.Paging;
import com.ss.dto.Store;
import com.ss.exception.ExceptionResponse;
import com.ss.model.StoreModel;
import com.ss.repository.StoreRepository;
import com.ss.service.StoreService;
import com.ss.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    @Override
    public StoreModel create(Store request) {
        List<StoreModel> stores = storeRepository.findByName(request.getName());
        if (!stores.isEmpty())
            throw new ExceptionResponse("store is existed!!!");
        StoreModel storeModel = new StoreModel();
        storeModel.update(request);
        storeModel = storeRepository.save(storeModel);
        return storeModel;
    }

    @Override
    public StoreModel update(UUID id, Store request) {
        Optional<StoreModel> storeOptional = storeRepository.findById(id);
        if (storeOptional.isEmpty())
            throw new ExceptionResponse("store is existed!!!");
        StoreModel store = storeOptional.get();
        store.update(request);
        store = storeRepository.save(store);
        return store;
    }

    @Override
    public void delete(UUID id) {
        Optional<StoreModel> storeOptional = storeRepository.findById(id);
        if (storeOptional.isEmpty())
            throw new ExceptionResponse("store is existed!!!");
        StoreModel store = storeOptional.get();
        store.setDeleted(true);
        storeRepository.save(store);
    }

    @Override
    @Transactional
    public PageResponse<StoreModel> search(String keyword, PageCriteria pageCriteria) {
        keyword = CommonUtil.convertSqlSearchText(keyword);
        Page<StoreModel> storePage = storeRepository.search(keyword, pageCriteriaPageableMapper.toPageable(pageCriteria));

        return PageResponse.<StoreModel>builder()
                .paging(Paging.builder().totalCount(storePage.getTotalElements())
                        .pageIndex(pageCriteria.getPageIndex())
                        .pageSize(pageCriteria.getPageSize())
                        .build())
                .data(storePage.getContent())
                .build();
    }

    @Override
    public StoreModel findById(UUID id) {
        if (id == null)
            return null;
        Optional<StoreModel> storeOptional = storeRepository.findById(id);
        if (storeOptional.isEmpty())
            return null;
        return storeOptional.get();
    }

    @Override
    public Set<StoreModel> findByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return new HashSet<>();
        return new HashSet<>(storeRepository.findAllById(ids));
    }

    @Override
    public List<StoreModel> findByNameIn(Collection<String> storeNames) {
        if (storeNames == null || storeNames.isEmpty())
            return new ArrayList<>();
        return storeRepository.findByNameIn(new ArrayList<>(storeNames));
    }

    @Override
    public List<StoreModel> findAll() {
        return storeRepository.findAll();
    }
}
