package com.luontd.etlworkerservice.infrastructure.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {

    // Lấy đường dẫn thư mục đã cấu hình từ file yml
    @Value("${app.ocr.tessdata-path}")
    private String tessdataPath;

    @Bean
    public ITesseract tesseract() {
        ITesseract tesseract = new Tesseract();

        // 1. Gắn đường dẫn thư mục chứa các file .traineddata vào engine
        tesseract.setDatapath(tessdataPath);

        // 2. Thiết lập quét song song cả tiếng Việt và tiếng Anh
        tesseract.setLanguage("vie+eng");

        return tesseract;
    }
}
