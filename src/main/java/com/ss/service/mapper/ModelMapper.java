package com.ss.service.mapper;

import java.util.List;

public interface ModelMapper<S, T> {
    S toSource(T target);

    T toTarget(S source);

    List<S> toSource(List<T> listTarget);

    List<T> toTarget(List<S> ListSource);
}
