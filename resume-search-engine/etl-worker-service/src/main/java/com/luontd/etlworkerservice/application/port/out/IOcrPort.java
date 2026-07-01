package com.luontd.etlworkerservice.application.port.out;

public interface IOcrPort {
    /**
     * Trích xuất văn bản từ mảng byte của file ảnh/PDF
     * * @param fileBytes Mảng byte chứa nội dung file CV (lấy từ MinIO)
     * @param fileName Tên gốc của file (để xác định đuôi mở rộng .pdf, .png...)
     * @return Chuỗi văn bản thô (Raw Text)
     */
    String extractText(byte[] fileBytes, String fileName);
}
