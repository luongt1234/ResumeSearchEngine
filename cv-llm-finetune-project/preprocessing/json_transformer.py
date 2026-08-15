import os
import csv
import json
import time
import argparse
import google.generativeai as genai
from google.generativeai.types import HarmCategory, HarmBlockThreshold

class ResumeJSONTransformer:
    def __init__(self, api_key: str, template_path: str, input_csv: str, output_jsonl: str):
        self.api_key = api_key
        self.template_path = template_path
        self.input_csv = input_csv
        self.output_jsonl = output_jsonl
        
        # Cấu hình Gemini
        genai.configure(api_key=self.api_key)
        self.model = genai.GenerativeModel('gemini-3.5-flash')
        
        self.template_content = self._load_template()
        self.processed_files = self._load_processed_files()

    def _load_template(self) -> str:
        try:
            with open(self.template_path, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            print(f"[LỖI] Không thể đọc file template JSON: {e}")
            return ""

    def _load_processed_files(self) -> set:
        processed = set()
        if os.path.exists(self.output_jsonl):
            try:
                with open(self.output_jsonl, 'r', encoding='utf-8') as f:
                    for line in f:
                        if line.strip():
                            data = json.loads(line)
                            if 'file_path' in data:
                                processed.add(data['file_path'])
            except Exception as e:
                print(f"[CẢNH BÁO] Lỗi khi đọc file kết quả cũ: {e}")
        return processed

    def parse_with_gemini(self, text: str, retries: int = 3) -> dict:
        prompt = f"""
Bạn là một chuyên gia bóc tách dữ liệu nhân sự (Resume Parser).
Hãy đọc đoạn text CV dưới đây và trích xuất thông tin thành ĐÚNG định dạng JSON theo schema mẫu.
Tuyệt đối KHÔNG trả về bất kỳ text nào khác ngoài chuỗi JSON hợp lệ. Bỏ qua các markdown formatting như ```json.

MẪU SCHEMA (Chỉ trả về các key này, nếu không có thông tin thì để trống string "" hoặc mảng rỗng []):
{self.template_content}

TEXT CV CẦN BÓC TÁCH:
{text}
"""
        # Cấu hình độ an toàn để tránh bị chặn khi parse thông tin cá nhân
        safety_settings = {
            HarmCategory.HARM_CATEGORY_HARASSMENT: HarmBlockThreshold.BLOCK_NONE,
            HarmCategory.HARM_CATEGORY_HATE_SPEECH: HarmBlockThreshold.BLOCK_NONE,
            HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT: HarmBlockThreshold.BLOCK_NONE,
            HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT: HarmBlockThreshold.BLOCK_NONE,
        }

        for attempt in range(retries):
            try:
                response = self.model.generate_content(
                    prompt,
                    safety_settings=safety_settings,
                    generation_config=genai.types.GenerationConfig(
                        response_mime_type="application/json", # Ép trả về JSON
                        temperature=0.1 # Nhiệt độ thấp để chính xác
                    )
                )
                
                result_text = response.text
                
                # Làm sạch markdown nếu Gemini vô tình trả về
                if result_text.startswith("```json"):
                    result_text = result_text.replace("```json", "").replace("```", "").strip()
                
                parsed_json = json.loads(result_text)
                return parsed_json
                
            except json.JSONDecodeError as e:
                print(f"[CẢNH BÁO] Trả về không phải JSON hợp lệ. Thử lại {attempt + 1}/{retries}")
                time.sleep(2)
            except Exception as e:
                error_msg = str(e)
                print(f"[CẢNH BÁO] Lỗi gọi API: {error_msg}")
                if "429" in error_msg or "Quota exceeded" in error_msg:
                    print("=> Bị giới hạn tốc độ (Rate Limit) từ Google. Đang tự động tạm dừng 60 giây...")
                    time.sleep(60) # Đợi hẳn 1 phút cho chắc chắn để qua giới hạn RPM
                else:
                    time.sleep(5)
                    
        return None

    def run(self):
        if not self.template_content:
            print("[LỖI] Template rỗng, không thể chạy.")
            return

        if not os.path.exists(self.input_csv):
            print(f"[LỖI] Không tìm thấy file {self.input_csv}")
            return

        print("Bắt đầu tiến trình bóc tách bằng Gemini API...")
        print("Tốc độ giới hạn: ~15 request / phút để dùng Free Tier.")
        
        with open(self.input_csv, mode='r', encoding='utf-8-sig') as infile:
            reader = csv.DictReader(infile)
            rows = list(reader)
            total = len(rows)

        print(f"Tổng số file cần xử lý: {total}")
        print(f"Đã xử lý trước đó: {len(self.processed_files)}")
        
        # Mở file jsonl chế độ append để ghi thêm (không ghi đè)
        with open(self.output_jsonl, mode='a', encoding='utf-8') as outfile:
            for idx, row in enumerate(rows, 1):
                file_path = row.get('file_path', '')
                text = row.get('extracted_text', '')
                
                if file_path in self.processed_files:
                    continue
                    
                if not text.strip():
                    print(f"[{idx}/{total}] Bỏ qua file rỗng: {file_path}")
                    continue

                print(f"[{idx}/{total}] Đang xử lý: {file_path}")
                
                parsed_data = self.parse_with_gemini(text)
                
                if parsed_data:
                    # Chèn thêm filepath vào JSON để dễ theo dõi
                    parsed_data['file_path'] = file_path
                    
                    # Ghi từng dòng JSON (JSONLines)
                    json_str = json.dumps(parsed_data, ensure_ascii=False)
                    outfile.write(json_str + "\n")
                    outfile.flush()
                    print(f"   => Thành công.")
                else:
                    print(f"   => [LỖI] Xử lý thất bại sau nhiều lần thử.")
                
                # RATE LIMIT: Ngủ khoảng 4.5 giây để đạt ~13-14 request/phút, 
                # an toàn cho giới hạn 15 RPM của Gemini Free Tier
                time.sleep(4.5) 

        print(f"Hoàn thành toàn bộ quá trình! File kết quả: {self.output_jsonl}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Chạy bóc tách CV bằng Gemini API")
    parser.add_argument("--api-key", type=str, required=True, help="Google Gemini API Key")
    parser.add_argument("-t", "--template", type=str, default=r"E:\project\ResumeSearchEngine\resume-search-engine\etl-worker-service\src\main\resources\schemas\cv-template.json", help="Đường dẫn file template JSON")
    parser.add_argument("-i", "--input", type=str, default="../cleaned_ocr_results.csv", help="File CSV đầu vào đã làm sạch")
    parser.add_argument("-o", "--output", type=str, default="../parsed_resumes.jsonl", help="File JSONL kết quả đầu ra")
    
    args = parser.parse_args()
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    input_path = args.input if os.path.isabs(args.input) else os.path.join(script_dir, args.input)
    output_path = args.output if os.path.isabs(args.output) else os.path.join(script_dir, args.output)
    
    transformer = ResumeJSONTransformer(
        api_key=args.api_key,
        template_path=args.template,
        input_csv=input_path,
        output_jsonl=output_path
    )
    transformer.run()
