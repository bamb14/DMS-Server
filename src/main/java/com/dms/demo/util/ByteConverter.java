package com.dms.demo.util;
import java.text.DecimalFormat;

public class ByteConverter {
    public static String format(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        // 1024의 n승으로 나눔 (예: 1048576 -> 1.0)
        // #,##0.# : 3자리마다 콤마, 소수점 첫째자리까지 표시 (0이면 정수만)
        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}