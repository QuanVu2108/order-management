
package com.ss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderStatisticResponse {
    private int allCnt;
    private int newCnt;
    private int pendingCnt;
    private int doneCnt;
}
