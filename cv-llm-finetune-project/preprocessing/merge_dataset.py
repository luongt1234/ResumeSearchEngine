import os
import sys
import csv
import json

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

# Input paths
parsed_json_path = r"E:\project\ResumeSearchEngine\modelAIConvertText\parsed_resumes_1000.json"
csv_path = r"E:\project\ResumeSearchEngine\modelAIConvertText\cleaned_ocr_results.csv"

# Output paths
output_simple_jsonl = r"E:\project\ResumeSearchEngine\modelAIConvertText\dataset_input_output.jsonl"
output_alpaca_jsonl = r"E:\project\ResumeSearchEngine\modelAIConvertText\dataset_alpaca.jsonl"
output_sharegpt_jsonl = r"E:\project\ResumeSearchEngine\modelAIConvertText\dataset_sharegpt.jsonl"

def merge_data():
    if not os.path.exists(parsed_json_path):
        print(f"[LỖI] Không tìm thấy file {parsed_json_path}")
        return
    if not os.path.exists(csv_path):
        print(f"[LỖI] Không tìm thấy file {csv_path}")
        return

    # 1. Load parsed resumes
    print("Đang tải dữ liệu JSON đã parse...")
    with open(parsed_json_path, 'r', encoding='utf-8') as f:
        parsed_resumes = json.load(f)
    print(f"Đã tải {len(parsed_resumes)} bản ghi.")

    # 2. Build map of file_path -> extracted_text from CSV
    print("Đang tải dữ liệu CSV và đối chiếu...")
    csv_lookup = {}
    with open(csv_path, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            fp = row.get('file_path', '')
            text = row.get('extracted_text', '')
            if fp and text:
                csv_lookup[fp] = text

    # 3. Merge data
    simple_dataset = []
    alpaca_dataset = []
    sharegpt_dataset = []
    
    missing_count = 0
    
    instruction = "Hãy đọc đoạn text CV (OCR) dưới đây và trích xuất thông tin thành đúng định dạng JSON theo schema mẫu của cv-template."

    for resume in parsed_resumes:
        fp = resume.get('file_path', '')
        if not fp:
            continue
            
        # Get raw OCR text
        raw_text = csv_lookup.get(fp)
        if not raw_text:
            # Try matching by base name in case paths differ slightly
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

        # Clone and remove file_path from output JSON to clean it up for training/fine-tuning
        output_data = resume.copy()
        if 'file_path' in output_data:
            del output_data['file_path']
            
        output_str = json.dumps(output_data, ensure_ascii=False, indent=2)

        # Format 1: Simple Input-Output JSONL
        simple_dataset.append({
            "input": raw_text,
            "output": output_str
        })

        # Format 2: Alpaca format (Instruction - Input - Output)
        alpaca_dataset.append({
            "instruction": instruction,
            "input": raw_text,
            "output": output_str
        })

        # Format 3: ShareGPT / ChatML format (Messages list)
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

    print(f"Đối chiếu thành công: {len(simple_dataset)}/{len(parsed_resumes)} bản ghi.")
    if missing_count > 0:
        print(f"[CẢNH BÁO] Không thể tìm thấy dữ liệu thô cho {missing_count} bản ghi.")

    # Write files
    print(f"Đang ghi file: {output_simple_jsonl}")
    with open(output_simple_jsonl, 'w', encoding='utf-8') as f:
        for item in simple_dataset:
            f.write(json.dumps(item, ensure_ascii=False) + "\n")
            
    print(f"Đang ghi file: {output_alpaca_jsonl}")
    with open(output_alpaca_jsonl, 'w', encoding='utf-8') as f:
        for item in alpaca_dataset:
            f.write(json.dumps(item, ensure_ascii=False) + "\n")

    print(f"Đang ghi file: {output_sharegpt_jsonl}")
    with open(output_sharegpt_jsonl, 'w', encoding='utf-8') as f:
        for item in sharegpt_dataset:
            f.write(json.dumps(item, ensure_ascii=False) + "\n")

    print("[THÀNH CÔNG] Đã hoàn thành ghép dữ liệu và lưu 3 định dạng dataset!")

if __name__ == "__main__":
    merge_data()
