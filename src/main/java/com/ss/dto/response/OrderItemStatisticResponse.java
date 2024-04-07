
package com.ss.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemStatisticResponse {
    private int allCnt;
    private int pendingCnt;
    private int checkedCnt;
    private int delayCnt;
    private int updateCnt;
    private int sentCnt;
    private int inCartCnt;
    private int cancelCnt;
    private int doneCnt;
}
