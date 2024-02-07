package com.ss.service.Impl;

import com.ss.model.Order;
import com.ss.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Override
    public Order test() {
        return new Order();
    }
}
