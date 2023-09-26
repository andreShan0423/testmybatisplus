package com.example.mybatisplus.Until;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static sun.net.www.protocol.http.HttpURLConnection.userAgent;

/**
 * @PackageName: com.wind.util.exportExcel
 * @Author: bird
 * @Description:
 */

@Component
public class ExportMultiExcelUtils implements Serializable {

    private static final long serialVersionUID = -3238218142023285526L;

    private static final Logger logger = LoggerFactory.getLogger(ExportMultiExcelUtils.class);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");

    public static final String EXCEL_SUFFIX = ".xlsx";

    public static final Integer CURRENT_MAX_EXCEL_DATA = 500000;

    public static String EXCEL_NAME = "";

    public  Integer EXCEL_NUMBER =  0; //默认生成excel个数

   

   /**
     *@Description : 主逻辑入口
     *@Param: [headerMap, tempDataList, tableName, response]
     *@return: void
     *@Author: bird
     */
   public void ExportExcel(Map<String, Object> headerMap, List<Map<String, Object>> tempDataList, String tableName, HttpServletResponse response) throws Exception {
        logger.info("进入Excel工具类");
        long start = System.currentTimeMillis();
        EXCEL_NAME=tableName;
        List<List<Object>> excelDataResult = new CopyOnWriteArrayList<>(new ArrayList<>()); //Excel对应行的数据
        List<Object> headExcelKey = new CopyOnWriteArrayList<>(new ArrayList<>());   //获取导出表头key容器  目的为了动态获取对应数据
        List<Object> headExcelValue = new CopyOnWriteArrayList<>(new ArrayList<>()); //获取Excel表头value容器导出   目的作为表头
        getHeaderKeyAndValue(headerMap, headExcelValue, headExcelKey); //获取excel的表头key与value
        processingInputData(tempDataList, excelDataResult, headExcelKey);//处理数据
        Integer excelTotal = excelDataResult.size();
        if (excelTotal <= CURRENT_MAX_EXCEL_DATA) {
            generateOneExcel(tableName, response, excelDataResult, headExcelValue, excelTotal); //导出单Excel
        } else {
            generateMultiExcel(tableName, response, excelDataResult, headExcelValue, excelTotal);//导出多Excel
        }
       long end = System.currentTimeMillis();
       logger.info("导出Excel:{},{}",excelTotal+"条",formatDuring(end-start));
    }

    private void generateOneExcel(String tableName, HttpServletResponse response, List<List<Object>> excelDataResult, List<Object> headExcelValue, Integer excelTotal) {
        List<List<Object>> excelTempDataResult = new CopyOnWriteArrayList<>(new ArrayList<>());
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
        Sheet sheet = sxssfWorkbook.createSheet(tableName);
        excelTempDataResult.add(headExcelValue);
        for (int j = 0; j < excelTotal; j++) {
            excelTempDataResult.add(excelDataResult.get(j));
        }
        insertDataToExcel(excelTempDataResult, excelTempDataResult.size(), sheet);
        logger.info("查询数据小于"+CURRENT_MAX_EXCEL_DATA+"条");
        exportRelationExcel(EXCEL_NAME, sxssfWorkbook,response); //导出excel
    }

    private void generateMultiExcel(String tableName, HttpServletResponse response, List<List<Object>> excelDataResult, List<Object> headExcelValue, Integer excelTotal) {
        logger.info("查询到的数据大于"+CURRENT_MAX_EXCEL_DATA+"条");
        int flagNum = excelTotal % CURRENT_MAX_EXCEL_DATA; // 取模看是否需要多分Excel
        if (flagNum == 0) {
            EXCEL_NUMBER = excelTotal / CURRENT_MAX_EXCEL_DATA;
        } else {
            EXCEL_NUMBER = excelTotal / CURRENT_MAX_EXCEL_DATA + 1;
        }
        for (int i = 1; i <= EXCEL_NUMBER; i++) {
            //构造当前Excel数据
            List<List<Object>> excelTempDataResult = new CopyOnWriteArrayList<>(new ArrayList<>()); ;
            SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(); //在循环内部
            Sheet sheet = sxssfWorkbook.createSheet(tableName);
            if (i == 1) {
                //ToDo: 取50万整数据，单独加表头
                excelTempDataResult.add(headExcelValue);
                for (int j = 0; j < CURRENT_MAX_EXCEL_DATA; j++) {
                    excelTempDataResult.add(excelDataResult.get(j));
                }
            } else {
                excelTempDataResult.add(headExcelValue); //添加每个Excel的表头
                for (int j = CURRENT_MAX_EXCEL_DATA * (i - 1); j < CURRENT_MAX_EXCEL_DATA * i; j++) {
                    if (j > excelTotal) {
                        break;
                    } else if (j < excelTotal) {
                        excelTempDataResult.add(excelDataResult.get(j));
                    }
                }
            }
            insertDataToExcel(excelTempDataResult, excelTempDataResult.size(), sheet);
            //导出Excel
            exportRelationExcel(EXCEL_NAME, sxssfWorkbook,response); //导出excel
        }
    }

//导出一个Excel,数据分Sheet
private void generateMultiSheet(String tableName, HttpServletResponse response, List<List<Object>> excelDataResult, List<Object> headExcelValue, Integer excelTotal) {
        logger.info("查询到的数据大于"+CURRENT_MAX_EXCEL_DATA+"条");
        int flagNum = excelTotal % CURRENT_MAX_EXCEL_DATA; // 取模看是否需要多分Excel
        if (flagNum == 0) {
            EXCEL_NUMBER = excelTotal / CURRENT_MAX_EXCEL_DATA;
        } else {
            EXCEL_NUMBER = excelTotal / CURRENT_MAX_EXCEL_DATA + 1;
        }
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();  //在循环外
        for (int i = 1; i <= EXCEL_NUMBER; i++) {
            //构造当前Excel数据
            List<List<Object>> excelTempDataResult = new ArrayList<>();
            Sheet sheet = sxssfWorkbook.createSheet(tableName+i);
            if (i == 1) {
                //ToDo: 取50万整数据，单独加表头
                excelTempDataResult.add(headExcelValue);
                for (int j = 0; j < CURRENT_MAX_EXCEL_DATA; j++) {
                    excelTempDataResult.add(excelDataResult.get(j));
                }
            } else {
                excelTempDataResult.add(headExcelValue); //添加每个Excel的表头
                for (int j = CURRENT_MAX_EXCEL_DATA * (i - 1); j < CURRENT_MAX_EXCEL_DATA * i; j++) {
                    if (j > excelTotal) {
                        break;
                    } else if (j < excelTotal) {
                        excelTempDataResult.add(excelDataResult.get(j));
                    }
                }
            }
            insertDataToExcel(excelTempDataResult, excelTempDataResult.size(), sheet);
        }
        excelDataResult.clear();
        exportRelationExcel(EXCEL_NAME, sxssfWorkbook,response); //导出excel
    }
    /**
     *@Description: 给Excel赋值
     *@Param: [excelTempDataResult, size, sheet]
     *@return: void
     */
    private void insertDataToExcel(List<List<Object>> excelTempDataResult, int size, Sheet sheet) {
        for (int i = 0; i < size; i++) {
            Row row = sheet.createRow(i);
            List<Object> cellValue = excelTempDataResult.get(i);
            for (int j = 0; j < cellValue.size(); j++) {
                row.createCell(j).setCellValue(String.valueOf(null==cellValue.get(j)?"":cellValue.get(j)));
            }
        }
    }

    /**
     *@Description :
     *@Param: [tempDataList, excelDataResult, headExcelKey]
     *@return: void
     */
    private void processingInputData(List<Map<String, Object>> tempDataList, List<List<Object>> excelDataResult, List<Object> headExcelKey) {
        if(!tempDataList.isEmpty()&&tempDataList.size()>0){
            tempDataList.forEach(tempData->{
                List<Object> headExcelData = new ArrayList<>();
                headExcelKey.forEach(key->{
                    headExcelData.add(tempData.get(key));
                });
                excelDataResult.add(headExcelData);
            });
        }
    }

    /**
     *@Description : d
     *@Param: [EXCEL_NAME, sxssfWorkbook, response]
     *@return: void
     */
    private void exportRelationExcel(String EXCEL_NAME, SXSSFWorkbook sxssfWorkbook,HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel");
        String fileName = EXCEL_NAME+"-"+dateFormat.format((new Date()))+EXCEL_SUFFIX;
        String finalFileName ;
        try {
            if (StringUtils.contains(userAgent, "MSIE")) {
                finalFileName = URLEncoder.encode(fileName, "UTF8");
            } else if (StringUtils.contains(userAgent, "Mozilla")) {
                finalFileName = new String(fileName.getBytes("GBK"), "ISO-8859-1");
            } else {
                finalFileName = URLEncoder.encode(fileName, "UTF8");
            }
            response.setHeader("Content-disposition", "attachment;filename=" + finalFileName);
            OutputStream outputStream = response.getOutputStream();
            sxssfWorkbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            sxssfWorkbook.close();
        } catch (IOException e) {
            logger.error("导出失败,失败原因:{}");e.printStackTrace();
        }
    }

    /**
     *@Description : 创建Excel并赋值给单元格
     *@Param: [excelDataResult, EXCEL_NAME, tempDataList, headExcelKey]
     *@return: org.apache.poi.xssf.streaming.SXSSFWorkbook
     */
    private SXSSFWorkbook getSheetsData(List<List<Object>> excelDataResult, String EXCEL_NAME, List<Map<String, Object>> tempDataList, List<Object> headExcelKey) {
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
        Sheet sheet = sxssfWorkbook.createSheet(EXCEL_NAME);
        if(!tempDataList.isEmpty()&&tempDataList.size()>0){
            tempDataList.forEach(tempData->{
                List<Object> headExcelData = new ArrayList<>();
                headExcelKey.forEach(key->{
                    headExcelData.add(tempData.get(key));
                });
                excelDataResult.add(headExcelData);
            });
        }
        for (int i = 0; i < excelDataResult.size(); i++) {
            Row row = sheet.createRow(i);
            List<Object> cellValue = excelDataResult.get(i);
            for (int j = 0; j < cellValue.size(); j++) {
                row.createCell(j).setCellValue(String.valueOf(null==cellValue.get(j)?"":cellValue.get(j)));
            }
        }
        return sxssfWorkbook;
    }


    /**
     *@Description :获取导出表头的key值及value值
     *@Param: [headerMap, headExcelValue, headExcelKey]
     *@return: void
     */
    private void getHeaderKeyAndValue(Map<String,Object> headerMap,List<Object> headExcelValue, List<Object> headExcelKey) {
        for(Map.Entry<String,Object> entry: headerMap.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            headExcelValue.add(value);
            headExcelKey.add(key);
        }
    }

 public static String formatDuring(long time) {
        long minutes = (time % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (time % (1000 * 60)) / 1000;
        long millisecond = time % 1000;
        return "共耗时"+time+"毫秒,转为时分秒为: "+ minutes + "分钟，" + seconds + "秒，" + millisecond + "毫秒 ";
    }

/**
     *@Description :响应二
     *@Param: [EXCEL_NAME, sxssfWorkbook, response]
     *@return: void
     */
    public ResponseEntity<InputStreamResource> exportRelationExcel2(String EXCEL_NAME, SXSSFWorkbook sxssfWorkbook, HttpServletResponse response) {
        //response.setContentType("application/vnd.ms-excel");
        String fileName = EXCEL_NAME+"-"+dateFormat.format((new Date()))+ UUID.randomUUID().toString().replaceAll("-","")+EXCEL_SUFFIX;
        String filePath = "D:"+ File.separator+"sfExport";
        File tempPath = new File(filePath);
        FileSystemResource file =null;
        if(!tempPath.exists()){
            tempPath.mkdirs();
        } 
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath+File.separator+fileName);
            sxssfWorkbook.write(fileOutputStream);
            fileOutputStream.close();
            sxssfWorkbook.close();
            file = new FileSystemResource(filePath+File.separator+fileName);
            System.out.println(file.contentLength());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment;filename="+ new String(file.getFilename().getBytes("utf-8"), "ISO8859-1"));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return  ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(file.contentLength())
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(file.getInputStream()));
        } catch (IOException e) {
            logger.error("导出失败,失败原因:{}");
            e.printStackTrace();
            return null ;
        }

    }
}    
