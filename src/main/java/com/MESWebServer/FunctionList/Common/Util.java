package com.MESWebServer.FunctionList.Common;
import java.math.BigDecimal;

public class Util {
    private Util() {}  // 생성자 private (객체 생성 방지)
    public static class ResultMapper {
        public static Double toDouble(Object obj) {
            if (obj == null) return null;
            if (obj instanceof BigDecimal bigDecimal) {
                return bigDecimal.doubleValue();
            }
            if (obj instanceof Number number) {
                return number.doubleValue();
            }
            return null;
        }
    }
}