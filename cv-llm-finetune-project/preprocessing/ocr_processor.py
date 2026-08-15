import os
import csv
import pytesseract
from PIL import Image
from pdf2image import convert_from_path
from typing import List
import concurrent.futures
import multiprocessing

def worker_process(file_path: str, tesseract_cmd: str, poppler_path: str, lang: str) -> str:
    """Hàm worker độc lập để tránh lỗi pickling khi chạy đa luồng trên Windows."""
    processor = ResumeOCRProcessor(tesseract_cmd=tesseract_cmd, poppler_path=poppler_path, lang=lang)
    return processor.process_file(file_path)


class ResumeOCRProcessor:
    """
    Lớp xử lý OCR tiền xử lý dữ liệu Resume (PDF, Image).
    """
    def __init__(
        self, 
        tesseract_cmd: str = r'E:\Tesseract-OCR\tesseract.exe', 
        poppler_path: str = r'E:\poppler-26.02.0\Library\bin', 
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

    def process_batch_to_csv(self, file_paths: List[str], output_csv: str = 'ocr_results.csv', max_workers: int = None):
        """
        Xử lý một danh sách các file và lưu kết quả văn bản trích xuất được vào file CSV (Hỗ trợ đa luồng).
        """
        if max_workers is None:
            max_workers = max(1, multiprocessing.cpu_count() - 1)
            
        file_exists = os.path.isfile(output_csv)
        processed_files = set()
        
        if file_exists:
            try:
                with open(output_csv, mode='r', encoding='utf-8-sig') as f:
                    reader = csv.DictReader(f)
                    for row in reader:
                        if 'file_path' in row:
                            processed_files.add(row['file_path'])
            except Exception as e:
                print(f"Không thể đọc file {output_csv} để tiếp tục: {e}")
                
        pending_files = [fp for fp in file_paths if fp not in processed_files]
        
        if not pending_files:
            print("Tất cả các file đã được xử lý xong từ trước. Bỏ qua chạy.")
            return
            
        if processed_files:
            print(f"Đã bỏ qua {len(processed_files)} files đã xử lý trước đó. Còn lại {len(pending_files)} files.")
        
        # Mở file CSV chế độ append, đảm bảo hỗ trợ tiếng Việt (utf-8)
        with open(output_csv, mode='a', encoding='utf-8-sig', newline='') as csvfile:
            fieldnames = ['file_path', 'extracted_text', 'status']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            
            # Ghi header nếu file mới được tạo
            if not file_exists:
                writer.writeheader()
                
            print(f"Bắt đầu xử lý {len(pending_files)} files với {max_workers} tiến trình (processes)...")
            
            with concurrent.futures.ProcessPoolExecutor(max_workers=max_workers) as executor:
                # Gửi tất cả các task vào executor
                future_to_file = {
                    executor.submit(worker_process, fp, self.tesseract_cmd, self.poppler_path, self.lang): fp 
                    for fp in pending_files
                }
                
                completed = 0
                for future in concurrent.futures.as_completed(future_to_file):
                    file_path = future_to_file[future]
                    completed += 1
                    try:
                        text = future.result()
                        status = "Success" if text.strip() else "Empty Result"
                        
                        writer.writerow({
                            'file_path': file_path, 
                            'extracted_text': text, 
                            'status': status
                        })
                        csvfile.flush() # Lưu ngay xuống đĩa để tránh mất dữ liệu nếu script bị dừng đột ngột
                        print(f"[{completed}/{len(pending_files)}] Hoàn tất: {file_path}")
                    except Exception as e:
                        writer.writerow({
                            'file_path': file_path, 
                            'extracted_text': "", 
                            'status': f"Error: {e}"
                        })
                        csvfile.flush()
                        print(f"[{completed}/{len(pending_files)}] Lỗi khi xử lý {file_path}: {e}")


if __name__ == "__main__":
    import argparse
    import sys
    
    # Khắc phục lỗi hiển thị tiếng Việt trên Windows Console nếu cần
    if sys.platform == "win32":
        sys.stdout.reconfigure(encoding='utf-8')
        
    parser = argparse.ArgumentParser(description="Xử lý OCR cho Resume (PDF/Image)")
    parser.add_argument("-f", "--file", type=str, help="Đường dẫn đến 1 file cần xử lý (VD: cv.pdf)")
    parser.add_argument("-b", "--batch", nargs="+", help="Danh sách các file cần xử lý batch")
    parser.add_argument("-d", "--dir", type=str, help="Thư mục chứa các file cần xử lý (sẽ quét đệ quy tìm PDF/Image)")
    parser.add_argument("-o", "--output", type=str, default="ocr_results.csv", help="File CSV đầu ra (dùng cho batch/dir)")
    parser.add_argument("-w", "--workers", type=int, default=None, help="Số lượng worker processes (mặc định: số nhân CPU - 1)")
    
    args = parser.parse_args()
    
    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(1)
        
    processor = ResumeOCRProcessor()
    
    if args.file:
        print(f"Đang xử lý file: {args.file}")
        try:
            text = processor.process_file(args.file)
            print("\n" + "="*20 + " KẾT QUẢ TRÍCH XUẤT " + "="*20)
            print(text)
            print("="*60)
        except Exception as e:
            print(f"Lỗi: {e}")
            
    batch_files = []
    if args.batch:
        batch_files.extend(args.batch)
        
    if args.dir:
        print(f"Đang quét thư mục: {args.dir}...")
        for root, _, files in os.walk(args.dir):
            for file in files:
                if file.lower().endswith(('.pdf', '.png', '.jpg', '.jpeg')):
                    batch_files.append(os.path.join(root, file))
        print(f"Tìm thấy {len(batch_files)} file(s) hợp lệ.")
        
    if batch_files:
        processor.process_batch_to_csv(batch_files, output_csv=args.output, max_workers=args.workers)

