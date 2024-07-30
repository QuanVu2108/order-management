package com.ss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CommonUtil<T> {

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
        if (keyword == null)
            return null;
        return "%" +  keyword.toUpperCase() + "%";
    }

    public static <T> String convertToString(T value) {
        if (value == null)
            return "";
        return value.toString();
    }

    public static int findFirstLetterIndex(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isLetter(ch)) {
                return i;
            }
        }
        return str.length();
    }

    public static <T> T nullToDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
