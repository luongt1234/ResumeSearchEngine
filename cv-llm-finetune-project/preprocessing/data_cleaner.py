import csv
import re
import os
import sys

class OCRDataCleaner:
    """
    Lớp xử lý việc làm sạch dữ liệu OCR thô từ file CSV.
    """
    def __init__(self, input_csv: str, output_csv: str):
        self.input_csv = input_csv
        self.output_csv = output_csv
        self.total_rows = 0
        
        # Xử lý đường dẫn tương đối để thành đường dẫn tuyệt đối
        script_dir = os.path.dirname(os.path.abspath(__file__))
        if not os.path.isabs(self.input_csv):
            self.input_csv = os.path.join(script_dir, self.input_csv)
        if not os.path.isabs(self.output_csv):
            self.output_csv = os.path.join(script_dir, self.output_csv)

    def clean_text(self, text: str) -> str:
        """
        Làm sạch một đoạn văn bản OCR.
        """
        if not text:
            return ""
            
        # 1. Loại bỏ các dòng đánh dấu trang (VD: --- Page 1 ---)
        text = re.sub(r'---\s*Page\s+\d+\s*---', '', text, flags=re.IGNORECASE)
        
        # 2. Xóa các khoảng trắng thừa đầu/cuối mỗi dòng
        lines = [line.strip() for line in text.split('\n')]
        
        # 3. Loại bỏ các dòng trống liên tiếp (chỉ giữ lại tối đa 1 dòng trống giữa các đoạn)
        cleaned_lines = []
        for line in lines:
            if line:
                # Xóa các khoảng trắng thừa liên tiếp giữa các từ trong cùng 1 dòng
                line = re.sub(r'[ \t]+', ' ', line)
                cleaned_lines.append(line)
            elif cleaned_lines and cleaned_lines[-1] != "":
                # Thêm dòng trống nếu dòng trước đó không phải là dòng trống
                cleaned_lines.append("")
                
        # Nối lại thành văn bản
        cleaned_text = '\n'.join(cleaned_lines)
        
        # Đảm bảo không có khoảng trắng thừa ở đầu và cuối văn bản
        return cleaned_text.strip()

    def _count_rows(self) -> int:
        """Đếm số dòng trong file input."""
        try:
            with open(self.input_csv, mode='r', encoding='utf-8-sig') as f:
                reader = csv.DictReader(f)
                return sum(1 for _ in reader)
        except Exception as e:
            print(f"Lỗi khi đọc file: {e}")
            return 0

    def process(self):
        """
        Thực thi quá trình đọc, làm sạch và lưu file.
        """
        if not os.path.exists(self.input_csv):
            print(f"Lỗi: Không tìm thấy file {self.input_csv}")
            return

        print(f"Bắt đầu đọc dữ liệu từ: {self.input_csv}")
        
        self.total_rows = self._count_rows()
        if self.total_rows == 0:
            return
            
        print(f"Tổng cộng có {self.total_rows} dòng. Đang tiến hành làm sạch...")
        
        with open(self.input_csv, mode='r', encoding='utf-8-sig') as infile, \
             open(self.output_csv, mode='w', encoding='utf-8-sig', newline='') as outfile:
             
            reader = csv.DictReader(infile)
            fieldnames = reader.fieldnames
            
            if not fieldnames:
                print("File CSV không có header hợp lệ.")
                return
                
            writer = csv.DictWriter(outfile, fieldnames=fieldnames)
            writer.writeheader()
            
            processed = 0
            for row in reader:
                if 'extracted_text' in row:
                    row['extracted_text'] = self.clean_text(row['extracted_text'])
                writer.writerow(row)
                
                processed += 1
                if processed % 100 == 0:
                    print(f"Đã xử lý {processed}/{self.total_rows} files...")

        print(f"Hoàn thành! File đã làm sạch được lưu tại: {self.output_csv}")


if __name__ == "__main__":
    import argparse
    
    if sys.platform == "win32":
        sys.stdout.reconfigure(encoding='utf-8')
        
    parser = argparse.ArgumentParser(description="Script làm sạch dữ liệu OCR (OOP)")
    parser.add_argument("-i", "--input", type=str, default="../ocr_results.csv", help="Đường dẫn file CSV thô đầu vào")
    parser.add_argument("-o", "--output", type=str, default="../cleaned_ocr_results.csv", help="Đường dẫn file CSV đầu ra")
    
    args = parser.parse_args()
    
    cleaner = OCRDataCleaner(input_csv=args.input, output_csv=args.output)
    cleaner.process()
