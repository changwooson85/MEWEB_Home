package com.MESWebServer.service;


import com.MESWebServer.FunctionList.Common.DateFormatUtil;
import com.MESWebServer.entity.Real.CrasJobLst;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public ByteArrayOutputStream generateExcel(List<CrasJobLst> crasJobLstList) throws IOException {
        // 새로운 엑셀 워크북 생성
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("CrasJobLst");

        // 헤더 행 추가
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("RES_ID");
        headerRow.createCell(1).setCellValue("DEVICE");
        headerRow.createCell(2).setCellValue("LAYER");
        headerRow.createCell(3).setCellValue("마지막 파일 수정 시간");
        headerRow.createCell(4).setCellValue("UPDATE_TIME");

        // 데이터 행 추가
        int rowNum = 1;
        for (CrasJobLst crasJobLst : crasJobLstList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(crasJobLst.getId().getResId());
            row.createCell(1).setCellValue(crasJobLst.getId().getDevice());
            row.createCell(2).setCellValue(crasJobLst.getId().getLayer());
            row.createCell(3).setCellValue(DateFormatUtil.format14(crasJobLst.getLastModifyTime()));
            row.createCell(4).setCellValue(DateFormatUtil.format14(crasJobLst.getUpdateTime()));
            //row.createCell(3).setCellValue(crasJobLst.getUpdateTime());
        }

        // 엑셀 파일을 바이트 배열로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }
}
