package com.ss.enums;

import java.util.Arrays;
import java.util.List;

public enum OrderItemStatus {
    PENDING,
    CHECKED,
    DELAY,
    UPDATE,
    SENT,
    IN_CART,
    CANCEL,
    DONE;

    public static List<OrderItemStatus> getPendingStatus() {
        return Arrays.asList(PENDING, CHECKED, DELAY, UPDATE, SENT, IN_CART);
    }
}
