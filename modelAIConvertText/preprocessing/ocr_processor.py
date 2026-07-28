import os
import csv
import pytesseract
from PIL import Image
from pdf2image import convert_from_path
from typing import List

class ResumeOCRProcessor:
    """
    Lớp xử lý OCR tiền xử lý dữ liệu Resume (PDF, Image).
    """
    def __init__(
        self, 
        tesseract_cmd: str = r'E:\Tesseract-OCR\tesseract.exe', 
        poppler_path: str = r'C:\poppler\bin', 
        lang: str = 'vie+eng'
    ):
        self.tesseract_cmd = tesseract_cmd
        self.poppler_path = poppler_path
        self.lang = lang
        
        # Cấu hình đường dẫn Tesseract
        pytesseract.pytesseract.tesseract_cmd = self.tesseract_cmd

    def extract_from_image(self, image_path: str) -> str:
        """Trích xuất text từ một file ảnh."""
        try:
            img = Image.open(image_path)
            text = pytesseract.image_to_string(img, lang=self.lang)
            return text.strip()
        except Exception as e:
            print(f"[OCR ERROR] Lỗi khi xử lý ảnh {image_path}: {e}")
            return ""

    def extract_from_pdf(self, pdf_path: str) -> str:
        """Chuyển đổi PDF thành danh sách ảnh và trích xuất text."""
        full_text = []
        try:
            pages = convert_from_path(pdf_path, poppler_path=self.poppler_path)
            for i, page in enumerate(pages):
                text = pytesseract.image_to_string(page, lang=self.lang)
                full_text.append(f"--- Page {i + 1} ---\n{text}")
            return "\n".join(full_text)
        except Exception as e:
            print(f"[OCR ERROR] Lỗi khi xử lý PDF {pdf_path}: {e}")
            return ""

    def process_file(self, file_path: str) -> str:
        """
        Nhận diện định dạng và trích xuất text thô từ file.
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"Không tìm thấy file: {file_path}")
            
        ext = file_path.lower().split('.')[-1]
        
        if ext == 'pdf':
            return self.extract_from_pdf(file_path)
        elif ext in ['png', 'jpg', 'jpeg']:
            return self.extract_from_image(file_path)
        else:
            raise ValueError(f"Định dạng file .{ext} không được hỗ trợ.")

    def process_batch_to_csv(self, file_paths: List[str], output_csv: str = 'ocr_results.csv'):
        """
        Xử lý một danh sách các file và lưu kết quả văn bản trích xuất được vào file CSV.
        """
        file_exists = os.path.isfile(output_csv)
        
        # Mở file CSV chế độ append, đảm bảo hỗ trợ tiếng Việt (utf-8)
        with open(output_csv, mode='a', encoding='utf-8-sig', newline='') as csvfile:
            fieldnames = ['file_path', 'extracted_text', 'status']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            
            # Ghi header nếu file mới được tạo
            if not file_exists:
                writer.writeheader()
                
            for file_path in file_paths:
                print(f"Đang xử lý: {file_path}...")
                try:
                    text = self.process_file(file_path)
                    status = "Success" if text.strip() else "Empty Result"
                    
                    writer.writerow({
                        'file_path': file_path, 
                        'extracted_text': text, 
                        'status': status
                    })
                    print(f"-> Hoàn tất xử lý và lưu: {file_path}")
                except Exception as e:
                    writer.writerow({
                        'file_path': file_path, 
                        'extracted_text': "", 
                        'status': f"Error: {e}"
                    })
                    print(f"-> Lỗi khi xử lý {file_path}: {e}")
