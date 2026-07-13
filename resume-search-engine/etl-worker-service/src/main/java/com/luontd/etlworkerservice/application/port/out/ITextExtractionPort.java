package com.luontd.etlworkerservice.application.port.out;

public interface ITextExtractionPort {
    /**
     * Trích xuất văn bản từ mảng byte của file.
     *
     * @param fileBytes Mảng byte của file tải về từ MinIO
     * @param fileExtension Đuôi file (ví dụ: "pdf", "png", "jpg") để phân loại nhánh
     * @return Chuỗi văn bản đã trích xuất (Raw Text)
     */
    String extractText(byte[] fileBytes, String fileName, String fileExtension);
}
