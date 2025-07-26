package com.project.selenium;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    public static List<String> getLoanNumbers(String excelPath) {
        List<String> loanNumbers = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    if (cell.getCellType() == CellType.STRING) {
                        loanNumbers.add(cell.getStringCellValue().trim());
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        loanNumbers.add(String.valueOf((long) cell.getNumericCellValue()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error reading Excel: " + e.getMessage());
        }
        return loanNumbers;
    }
}
