package com.ss.exception.http;

import com.ss.exception.ResponseError;
import lombok.Getter;

@Getter
public enum InvalidInputError implements ResponseError {

    PERMISSION_GROUP_INVALID(4010000, "permission group is not existed"),
    USER_PASSWORD_INVALID(4010001, "Invalid username/password supplied"),
    TOKEN_EXPIRED(4010002, "Token was expired. Please make a new signin request"),
    PRODUCT_NUMBER(4010100, "product number can not be null"),
    PRODUCT_CODE(4010101, "product code can not be null"),
    PRODUCT_NAME(4010102, "product name can not be null"),
    PRODUCT_CATEGORY(4010103, "category can not be null"),
    PRODUCT_BRAND(4010104, "brand can not be null"),
    PRODUCT_COLOR(4010105, "color can not be null"),
    PRODUCT_SIZE(4010106, "size can not be null"),
    EXPORT_FILE_FAILED(4010110,"export file failed!!!"),
    GENERATE_QR_FAILED(4010115,"QR Code was generated failed!!!"),

    STORE_INVALID(4010120,"store is invalid!!!"),
    TARGET_STORE_INVALID(4010121,"target store is invalid!!!"),
    PRODUCT_INVALID(4010122,"product is invalid!!!"),
    EXPORT_QUANTITY_INVALID(4010123,"export quantity is invalid!!!"),
    STORE_ITEM_INVALID(4010124,"store item is invalid!!!"),
    DELETE_EXPORT_STORE_ITEM_INVALID(4010125,"can not delete item because of quantity invalid!!!"),
    ORDER_INVALID(4010126,"order is invalid!!!"),

    ;

    private Integer code;
    private String message;

    InvalidInputError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return 400;
    }

    public Integer getCode() {
        return code;
    }

    public String getCodeString() {
        return code.toString();
    }
}
