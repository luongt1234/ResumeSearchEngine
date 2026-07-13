package com.luontd.etlworkerservice.infrastructure.extraction;

import org.apache.pdfbox.Loader;
import com.luontd.etlworkerservice.application.port.out.ITextExtractionPort;
import com.luontd.etlworkerservice.infrastructure.ocr.TesseractOcrAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmartPdfExtractionAdapter implements ITextExtractionPort {
    private final TesseractOcrAdapter _ocrPort;

    private static final int MIN_TEXT_LENGTH = 50;

    @Override
    public String extractText(byte[] fileBytes, String fileName, String fileExtension) {
        if (!"pdf".equalsIgnoreCase(fileExtension)) {
            log.info("File là ảnh ({}). Chuyển thẳng sang luồng Tesseract OCR.", fileExtension);
            return _ocrPort.extractText(fileBytes, fileName);
        }

        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            if (extractedText != null && extractedText.trim().length() > MIN_TEXT_LENGTH) {
                log.info("Đọc trực tiếp PDF thành công. Độ dài text: {} ký tự. Bỏ qua OCR.", extractedText.length());
                return extractedText.trim();
            } else {
                log.warn("Đọc trực tiếp PDF thất bại hoặc text rác (< 50 ký tự). Bắt đầu rẽ nhánh sang OCR...");
                return _ocrPort.extractText(fileBytes, fileName);
            }
        } catch (Exception e) {
            log.error("Lỗi khi đọc PDF bằng PDFBox. Fallback sang OCR.", e);
            return _ocrPort.extractText(fileBytes, fileName);
        }
    }
}
