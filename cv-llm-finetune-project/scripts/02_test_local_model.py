import os
import sys
import json
import argparse

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

# Get project root directory
script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(script_dir)

def test_gguf_model(model_path, cv_text):
    print(f"Đang nạp GGUF model từ: {model_path}...")
    try:
        from llama_cpp import Llama
    except ImportError:
        print("[LỖI] Chưa cài đặt thư viện 'llama-cpp-python'. Vui lòng chạy lệnh:")
        print("  pip install llama-cpp-python")
        return

    # Load model
    llm = Llama(
        model_path=model_path,
        n_ctx=4096,      # Context window
        n_threads=4,     # Adjust according to your CPU cores
        n_gpu_layers=0   # Set to >0 if you want to offload layers to GTX 1650
    )

    instruction = "Hãy đọc đoạn text CV (OCR) dưới đây và trích xuất thông tin thành đúng định dạng JSON theo schema mẫu của cv-template."
    prompt = f"<|im_start|>system\nBạn là một chuyên gia bóc tách dữ liệu nhân sự (Resume Parser).<|im_end|>\n<|im_start|>user\n{instruction}\n\nTEXT CV:\n{cv_text}<|im_end|>\n<|im_start|>assistant\n"

    print("Đang chạy sinh dữ liệu cấu trúc (Inference)...")
    response = llm(
        prompt,
        max_tokens=2048,
        temperature=0.1,
        stop=["<|im_end|>", "<|im_start|>"]
    )

    output_text = response['choices'][0]['text']
    print("\n=== KẾT QUẢ INFERENCE (GGUF) ===")
    print(output_text)
    return output_text

def test_lora_model(base_model_name, lora_path, cv_text):
    print(f"Đang nạp base model '{base_model_name}' với LoRA adapters từ: {lora_path}...")
    try:
        import torch
        from transformers import AutoModelForCausalLM, AutoTokenizer
        from peft import PeftModel
    except ImportError:
        print("[LỖI] Chưa cài đặt các thư viện cần thiết. Vui lòng chạy lệnh:")
        print("  pip install torch transformers peft accelerate")
        return

    # Load tokenizer and base model
    tokenizer = AutoTokenizer.from_pretrained(base_model_name)
    base_model = AutoModelForCausalLM.from_pretrained(
        base_model_name,
        torch_dtype=torch.float16,
        device_map="auto"
    )

    # Merge LoRA weights
    model = PeftModel.from_pretrained(base_model, lora_path)
    model = model.merge_and_unload()

    instruction = "Hãy đọc đoạn text CV (OCR) dưới đây và trích xuất thông tin thành đúng định dạng JSON theo schema mẫu của cv-template."
    prompt = f"<|im_start|>system\nBạn là một chuyên gia bóc tách dữ liệu nhân sự (Resume Parser).<|im_end|>\n<|im_start|>user\n{instruction}\n\nTEXT CV:\n{cv_text}<|im_end|>\n<|im_start|>assistant\n"

    inputs = tokenizer(prompt, return_tensors="pt").to("cuda" if torch.cuda.is_available() else "cpu")
    
    print("Đang chạy sinh dữ liệu cấu trúc (Inference)...")
    with torch.no_grad():
        outputs = model.generate(
            **inputs,
            max_new_tokens=2048,
            temperature=0.1,
            do_sample=False
        )

    output_text = tokenizer.decode(outputs[0][inputs.input_ids.shape[1]:], skip_special_tokens=True)
    print("\n=== KẾT QUẢ INFERENCE (LoRA) ===")
    print(output_text)
    return output_text

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Chạy kiểm thử mô hình đã fine-tune offline")
    parser.add_argument("--mode", type=str, choices=["gguf", "lora"], default="gguf", help="Chế độ test: gguf hoặc lora")
    parser.add_argument("--model-path", type=str, help="Đường dẫn đến file .gguf hoặc folder lora adapter")
    parser.add_argument("--base-model", type=str, default="Qwen/Qwen2.5-3B-Instruct", help="Tên base model (chỉ dùng cho lora)")
    
    args = parser.parse_args()

    # Sample CV text for testing
    sample_cv = """
Julie A. McFederal
(555) 333-4555 | julie.mcfederal@jobsnet.com
11111 Shaw Avenue; Fresno, CA 93740

EDUCATION:
California State University, Fresno (Fresno State), Fresno, CA 93740
B.S. in Business Administration; Option: Accountancy. GPA: 3.75/4.0. Graduating May 2010.

WORK EXPERIENCE:
Accounting Intern, Westley Accountants, White Plains
- Handled daily input of ledger activities
- Prepared monthly balance sheets
- Assisted senior accountants in forecast analysis
"""

    if args.mode == "gguf":
        model_path = args.model_path if args.model_path else os.path.join(project_root, "exports", "gguf_model", "model.gguf")
        if not os.path.exists(model_path):
            print(f"[CẢNH BÁO] Không tìm thấy file GGUF tại {model_path}.")
            print("Bạn cần train model trên Colab/Kaggle, xuất file GGUF rồi copy vào thư mục này trước khi test.")
        else:
            test_gguf_model(model_path, sample_cv)
            
    elif args.mode == "lora":
        lora_path = args.model_path if args.model_path else os.path.join(project_root, "exports", "lora_adapters")
        if not os.path.exists(lora_path):
            print(f"[CẢNH BÁO] Không tìm thấy folder LoRA adapter tại {lora_path}.")
            print("Bạn cần train model trên Colab/Kaggle, tải lora adapters về và copy vào thư mục này trước khi test.")
        else:
            test_lora_model(args.base_model, lora_path, sample_cv)
