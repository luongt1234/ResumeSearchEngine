package com.luontd.etlworkerservice.infrastructure.ocr;

import com.luontd.etlworkerservice.application.port.out.IOcrPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TesseractOcrAdapter implements IOcrPort {

    // Inject bean ITesseract đã được cấu hình sẵn (load tessdata)
    private final ITesseract tesseract;

    @Override
    public String extractText(byte[] fileBytes, String fileName) {
        File tempFile = null;

        try {
            // Bước 1: Tạo File Tạm (Temp File) để Tesseract có thể đọc trực tiếp từ ổ cứng
            String extension = getExtension(fileName);
            tempFile = File.createTempFile("cv_temp_" + UUID.randomUUID(), extension);

            // Ghi mảng byte vào file tạm
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }

            log.info("Bắt đầu tiến hành OCR trên file tạm: {}", tempFile.getName());

            // Bước 2: Quét và Trích xuất (DoOCR)
            String rawText = tesseract.doOCR(tempFile);

            if (rawText == null || rawText.trim().isEmpty()) {
                log.warn("Tesseract không trích xuất được chữ nào từ file.");
                throw new RuntimeException("OCR không nhận diện được văn bản (Text rỗng)");
            }

            return rawText.trim();

        } catch (IOException e) {
            log.error("Lỗi IO khi tạo/ghi file tạm phục vụ OCR: {}", e.getMessage());
            throw new RuntimeException("Lỗi xử lý file nội bộ", e);
        } catch (TesseractException e) {
            log.error("Lỗi lõi Tesseract OCR trong quá trình nhận diện: {}", e.getMessage());
            throw new RuntimeException("Lỗi engine Tesseract", e);
        } finally {
            // Bước 3: Dọn dẹp (Cleanup) - Xóa file tạm ngay lập tức để tránh đầy ổ cứng
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("Cảnh báo: Không thể xóa file tạm sau khi OCR: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Hàm hỗ trợ lấy đuôi mở rộng của file
     */
    private String getExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return ".png"; // Mặc định đưa về dạng ảnh nếu không rõ
    }
}
