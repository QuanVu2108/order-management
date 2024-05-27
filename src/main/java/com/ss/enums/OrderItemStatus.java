package com.ss.enums;

import java.util.Arrays;
import java.util.List;

public enum OrderItemStatus {
    PENDING,
    DELAY,
    UPDATING,
    CANCEL,
    SENT,
    DONE;

    public static List<OrderItemStatus> getPendingStatus() {
        return Arrays.asList(PENDING, DELAY, UPDATING, SENT);
    }
}
