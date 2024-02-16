package com.ss.controller;

import com.ss.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool")
@AllArgsConstructor
public class ToolController {

    private final OrderService orderService;

}
