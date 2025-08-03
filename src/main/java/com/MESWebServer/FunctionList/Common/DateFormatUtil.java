package com.MESWebServer.FunctionList.Common;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("dateUtil")
public class DateFormatUtil {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format14(String dateStr){
        if(dateStr == null || dateStr.length() != 14){
            return dateStr;
        }

        try{
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, INPUT_FORMAT);
            return dateTime.format(OUTPUT_FORMAT);
        }
        catch(Exception e){
            return dateStr;
        }
    }

}
