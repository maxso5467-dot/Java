package com.xyzy.utils;

import java.util.UUID;

public class PathUtils {
    public static String generateFilePath(String fileName) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid + "_" + fileName;
    }
}
