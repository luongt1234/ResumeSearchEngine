import os
import sys
import csv
import json

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

# Get project root directory
script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(script_dir)

# Define paths relative to project root
parsed_json_path = os.path.join(project_root, "data", "raw", "parsed_resumes_1000.json")
csv_path = os.path.join(project_root, "data", "raw", "cleaned_ocr_results.csv")
output_json_path = os.path.join(project_root, "data", "processed", "train_dataset_sharegpt.json")

def prepare_data():
    print("=== BẮT ĐẦU CHUẨN BỊ DỮ LIỆU SHAREGPT ===")
    
    if not os.path.exists(parsed_json_path):
        print(f"[LỖI] Không tìm thấy file {parsed_json_path}")
        return
    if not os.path.exists(csv_path):
        print(f"[LỖI] Không tìm thấy file {csv_path}")
        return

    # 1. Load parsed resumes
    print(f"Đang đọc dữ liệu parsed JSON từ: {parsed_json_path}...")
    with open(parsed_json_path, 'r', encoding='utf-8') as f:
        parsed_resumes = json.load(f)
    print(f"Đã tải {len(parsed_resumes)} bản ghi.")

    # 2. Build map of file_path -> extracted_text from CSV
    print(f"Đang đọc dữ liệu OCR CSV từ: {csv_path}...")
    csv_lookup = {}
    with open(csv_path, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            fp = row.get('file_path', '')
            text = row.get('extracted_text', '')
            if fp and text:
                csv_lookup[fp] = text

    # 3. Match and convert to ShareGPT format
    sharegpt_dataset = []
    missing_count = 0
    
    instruction = "Hãy đọc đoạn text CV (OCR) dưới đây và trích xuất thông tin thành đúng định dạng JSON theo schema mẫu của cv-template."

    for resume in parsed_resumes:
        fp = resume.get('file_path', '')
        if not fp:
            continue
            
        raw_text = csv_lookup.get(fp)
        if not raw_text:
            # Fallback check for base file names
            base_name = os.path.basename(fp)
            matched = False
            for k, v in csv_lookup.items():
                if os.path.basename(k) == base_name:
                    raw_text = v
                    matched = True
                    break
            if not matched:
                missing_count += 1
                continue

        # Clean output JSON: remove file_path field before fine-tuning
        output_data = resume.copy()
        if 'file_path' in output_data:
            del output_data['file_path']
            
        output_str = json.dumps(output_data, ensure_ascii=False, indent=2)

        # Build ShareGPT message dictionary
        sharegpt_dataset.append({
            "conversations": [
                {
                    "from": "human",
                    "value": f"{instruction}\n\nTEXT CV:\n{raw_text}"
                },
                {
                    "from": "gpt",
                    "value": output_str
                }
            ]
        })

    print(f"Đối chiếu thành công: {len(sharegpt_dataset)}/{len(parsed_resumes)} bản ghi.")
    if missing_count > 0:
        print(f"[CẢNH BÁO] Không tìm thấy text thô cho {missing_count} bản ghi.")

    # 4. Save to processed folder in JSON format
    os.makedirs(os.path.dirname(output_json_path), exist_ok=True)
    print(f"Đang ghi dữ liệu đã xử lý vào: {output_json_path}...")
    with open(output_json_path, 'w', encoding='utf-8') as f:
        json.dump(sharegpt_dataset, f, ensure_ascii=False, indent=2)

    print("[THÀNH CÔNG] Đã tạo xong train_dataset_sharegpt.json!")

if __name__ == "__main__":
    prepare_data()
