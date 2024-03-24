package com.ss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StringUtil<T> {

    public static List<String> uuidGetNotContainedList(List<UUID> childList, List<UUID> parentList) {
        List<String> childStrList = childList == null ? new ArrayList<>() : childList.stream().map(item -> item.toString()).collect(Collectors.toList());
        List<String> parentStrList = parentList == null ? new ArrayList<>() : parentList.stream().map(item -> item.toString()).collect(Collectors.toList());
        return getNotContainedList(childStrList, parentStrList);
    }

    public static List<String> getNotContainedList(List<String> childList, List<String> parentList) {
        List<String> notContainItemList = new ArrayList<>();
        parentList.forEach(item -> {
            if (!childList.contains(item))
                notContainItemList.add(item);
        });
        return notContainItemList;
    }

    public static String convertSqlSearchText(String keyword) {
        return "%" + (keyword == null ? "" :  keyword.toUpperCase()) + "%";
    }
}
