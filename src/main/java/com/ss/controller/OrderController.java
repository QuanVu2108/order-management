package com.ss.controller;

import com.ss.dto.response.ServiceResponse;
import com.ss.model.Order;
import com.ss.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/areas")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/test")
    ServiceResponse<Order> test() {
        return ServiceResponse.succeed(HttpStatus.OK, orderService.test());
    }

}
