import os
import sys
import csv
import json
import random
import re
import argparse
import spacy

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

# Helper function to extract email
def extract_email(text):
    match = re.search(r'[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+', text)
    return match.group(0) if match else ""

# Helper function to extract phone
def extract_phone(text):
    phone_pattern = r'\(?\b\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b|\b\d{10,11}\b|\b\+?\d{1,2}[-.\s]\d{3}[-.\s]\d{3}[-.\s]\d{4}\b'
    matches = re.findall(phone_pattern, text)
    for m in matches:
        clean = re.sub(r'\D', '', m)
        if 10 <= len(clean) <= 12:
            return m.strip()
    return ""

# Helper function to extract links
def extract_links(text):
    link_pattern = r'\b(?:https?://)?(?:www\.)?(?:github\.com|linkedin\.com|behance\.net|dribbble\.com|twitter\.com|facebook\.com|instagram\.com)/[a-zA-Z0-9_.-]+|\b(?:https?://)?(?:www\.)?[a-zA-Z0-9_.-]+\.[a-zA-Z]{2,4}\b'
    matches = re.findall(link_pattern, text)
    links = []
    seen = set()
    for m in matches:
        m_lower = m.lower()
        if '@' in m_lower or 'gmail' in m_lower or 'email' in m_lower:
            continue
        if any(domain in m_lower for domain in ['github.com', 'linkedin.com', 'behance', 'dribbble', 'portfolio', 'resume']) or len(m) > 10:
            if m not in seen:
                seen.add(m)
                links.append(m.strip())
    return links

# Helper function to extract candidate name
def extract_name(text, email, phone, doc):
    # Try to find PERSON entities in the first 150 characters
    first_part = text[:200]
    person_ents = [ent.text.strip() for ent in doc.ents if ent.label_ == "PERSON" and ent.start_char < 200]
    
    headers = {'summary', 'experience', 'education', 'skills', 'projects', 'certifications', 'profile', 'work history', 'details', 'employment history'}
    
    # Filter valid person names
    for name in person_ents:
        name_clean = re.sub(r'[^a-zA-Z\s]', '', name).strip()
        words = name_clean.split()
        if 2 <= len(words) <= 4 and not any(w.lower() in headers for w in words):
            return name_clean

    # Fallback to line-based matching
    lines = [line.strip() for line in text.split('\n') if line.strip()]
    for line in lines[:15]:
        if email in line or (phone and phone in line) or len(line) < 3 or len(line) > 50:
            continue
        if line.lower() in headers or any(h in line.lower() for h in ['resume', 'curriculum vitae', 'cv', 'page']):
            continue
        words = line.split()
        if 2 <= len(words) <= 4 and all(w[0].isupper() for w in words if w.isalpha()):
            return line
            
    if lines:
        first_line = lines[0]
        if len(first_line) < 40 and not any(char in first_line for char in ['@', ':', '/']):
            return first_line
    return "N/A"

# Date range extraction helper
date_range_regex = re.compile(
    r'\b(?:\d{1,2}/\d{4}|\d{4}|[a-zA-Z]{3,9}\s+\d{4})\s*(?:-|to|—|Present)\s*(?:\d{1,2}/\d{4}|\d{4}|[a-zA-Z]{3,9}\s+\d{4}|Present)\b', 
    re.IGNORECASE
)

def clean_bullet(line):
    return re.sub(r'^[\s•*«»e\-o*+]+', '', line).strip()

def parse_experience(exp_lines, doc_exp):
    jobs = []
    current_job = None
    
    job_keywords = ['accountant', 'specialist', 'manager', 'developer', 'engineer', 'lead', 'consultant', 
                    'intern', 'assistant', 'officer', 'analyst', 'director', 'supervisor', 'administrator', 'volunteer']
    
    company_suffixes = ['inc', 'corp', 'ltd', 'co.', 'company', 'group', 'solutions', 'services', 'llc', 'llp', 'hospital', 'university', 'school']

    # Sub-parsing of ORGs inside experience section using spaCy
    org_ents = [ent.text.strip() for ent in doc_exp.ents if ent.label_ == "ORG"]

    for line in exp_lines:
        line_clean = line.strip()
        if not line_clean:
            continue
            
        is_new_job = False
        date_match = date_range_regex.search(line_clean)
        
        if date_match and len(line_clean) < 100:
            is_new_job = True
        elif any(kw in line_clean.lower() for kw in job_keywords) and not line_clean.startswith(('•', '*', '-', '«', '»', 'e ')):
            if len(line_clean) < 80:
                is_new_job = True
                
        if is_new_job:
            if current_job:
                jobs.append(current_job)
            
            duration = ""
            if date_match:
                duration = date_match.group(0)
                title_company = line_clean.replace(duration, "").strip(" ,-|—")
            else:
                title_company = line_clean
                
            parts = re.split(r'[,|–\-—]', title_company)
            position = parts[0].strip()
            
            # Find company from line or ORG entities
            company = "Unknown"
            if len(parts) > 1:
                company = parts[1].strip()
            else:
                # Try to match spaCy ORGs
                for org in org_ents:
                    if org in title_company:
                        company = org
                        break
                if company == "Unknown":
                    # Look for company suffixes
                    words = title_company.split()
                    for i, w in enumerate(words):
                        if w.lower().strip(',.()') in company_suffixes and i > 0:
                            company = " ".join(words[max(0, i-2):i+1])
                            break
            
            current_job = {
                "company": company if company else "Tên công ty",
                "position": position if position else "Vị trí công việc",
                "duration": duration,
                "responsibilities": []
            }
        else:
            if current_job:
                cleaned_line = clean_bullet(line_clean)
                if cleaned_line:
                    current_job["responsibilities"].append(cleaned_line)
            else:
                current_job = {
                    "company": "Tên công ty",
                    "position": "Vị trí công việc",
                    "duration": "",
                    "responsibilities": [clean_bullet(line_clean)]
                }
                
    if current_job:
        jobs.append(current_job)
        
    return jobs

def parse_education(edu_lines, doc_edu):
    edu_list = []
    current_edu = None
    
    degree_keywords = ['b.s', 'b.a', 'm.s', 'm.b.a', 'ph.d', 'bachelor', 'master', 'associate', 
                       'doctor', 'phd', 'diploma', 'cử nhân', 'thạc sĩ', 'kỹ sư', 'tiến sĩ', 'high school']
    
    edu_keywords = ['university', 'college', 'school', 'institute', 'academy', 'trường', 'đại học']

    org_ents = [ent.text.strip() for ent in doc_edu.ents if ent.label_ in ["ORG", "PERSON"]]

    for line in edu_lines:
        line_clean = line.strip()
        if not line_clean:
            continue
            
        is_new_edu = False
        date_match = date_range_regex.search(line_clean)
        
        if any(dk in line_clean.lower() for dk in degree_keywords) and len(line_clean) < 120:
            is_new_edu = True
        elif date_match and len(line_clean) < 100:
            is_new_edu = True
            
        if is_new_edu:
            if current_edu:
                edu_list.append(current_edu)
                
            duration = ""
            if date_match:
                duration = date_match.group(0)
                edu_text = line_clean.replace(duration, "").strip(" ,-|—")
            else:
                edu_text = line_clean
                
            parts = re.split(r'[,|–\-—]', edu_text)
            degree_major = parts[0].strip()
            
            # Identify institution
            institution = "Unknown Institution"
            if len(parts) > 1:
                institution = parts[1].strip()
            else:
                for org in org_ents:
                    if org in edu_text:
                        institution = org
                        break
                if institution == "Unknown Institution":
                    for kw in edu_keywords:
                        if kw in edu_text.lower():
                            words = edu_text.split()
                            for i, w in enumerate(words):
                                if kw in w.lower():
                                    institution = " ".join(words[max(0, i-2):i+2])
                                    break
                            break
            
            degree = degree_major
            major = ""
            for dk in degree_keywords:
                if dk in degree_major.lower():
                    degree = dk.upper()
                    major = degree_major.replace(dk, "").strip(" :,.-")
                    break
                    
            current_edu = {
                "institution": institution,
                "degree": degree,
                "major": major if major else degree_major,
                "duration": duration
            }
        else:
            if current_edu:
                if "gpa" in line_clean.lower():
                    current_edu["degree"] += f" ({line_clean})"
            else:
                current_edu = {
                    "institution": "Tên trường học/cơ sở đào tạo",
                    "degree": line_clean,
                    "major": "",
                    "duration": ""
                }
                
    if current_edu:
        edu_list.append(current_edu)
        
    return edu_list

def parse_projects(proj_lines):
    projects = []
    current_proj = None
    
    for line in proj_lines:
        line_clean = line.strip()
        if not line_clean:
            continue
            
        is_new_proj = False
        date_match = date_range_regex.search(line_clean)
        if (line_clean.lower().startswith('project') or len(line_clean) < 40) and not line_clean.startswith(('•', '*', '-')):
            is_new_proj = True
            
        if is_new_proj:
            if current_proj:
                projects.append(current_proj)
                
            duration = date_match.group(0) if date_match else ""
            name = line_clean.replace(duration, "").strip(" ,-|—")
            
            current_proj = {
                "project_name": name if name else "Tên dự án",
                "role": "Developer",
                "duration": duration,
                "technologies": [],
                "description": "",
                "responsibilities": []
            }
        else:
            if current_proj:
                cleaned = clean_bullet(line_clean)
                if cleaned:
                    if "technolog" in cleaned.lower() or "tech stack" in cleaned.lower():
                        techs = re.split(r'[,|;]', cleaned.replace("Technologies:", "").replace("Tech Stack:", "").strip())
                        current_proj["technologies"].extend([t.strip() for t in techs if t.strip()])
                    elif not current_proj["description"]:
                        current_proj["description"] = cleaned
                    else:
                        current_proj["responsibilities"].append(cleaned)
            else:
                current_proj = {
                    "project_name": "Tên dự án",
                    "role": "Developer",
                    "duration": "",
                    "technologies": [],
                    "description": clean_bullet(line_clean),
                    "responsibilities": []
                }
                
    if current_proj:
        projects.append(current_proj)
        
    return projects

def parse_skills(skills_lines):
    skills = []
    for line in skills_lines:
        line_clean = line.strip()
        if not line_clean:
            continue
        parts = re.split(r'[,|;•\-\*]', line_clean)
        for p in parts:
            p_clean = p.strip(" :,.-")
            if p_clean and len(p_clean) < 30:
                skills.append(p_clean)
    return list(dict.fromkeys(skills))

def parse_cv_to_template(text, nlp):
    # Initial NLP processing of full text
    doc = nlp(text)
    
    email = extract_email(text)
    phone = extract_phone(text)
    links = extract_links(text)
    full_name = extract_name(text, email, phone, doc)
    
    # Section Extraction
    lines = [line.strip() for line in text.split('\n')]
    
    header_patterns = {
        "summary": ['summary', 'profile', 'professional summary', 'objective', 'about me', 'details', 'professional profile'],
        "skills": ['skills', 'key skills', 'expertise', 'core competencies', 'technical skills', 'core skills', 'expertise portfolio'],
        "experience": ['experience', 'work history', 'employment history', 'professional experience', 'career summary', 'employment record'],
        "education": ['education', 'academic history', 'qualifications', 'education and training', 'education portfolio'],
        "projects": ['projects', 'personal projects', 'academic projects', 'key achievements/projects', 'selected projects'],
        "certifications": ['certifications', 'awards', 'affiliations', 'licenses', 'your awward', 'certification']
    }
    
    sections = {k: [] for k in header_patterns.keys()}
    current_section = None
    
    for line in lines:
        line_clean = line.strip()
        if not line_clean:
            continue
            
        line_test = re.sub(r'[^a-zA-Z\s]', '', line_clean).strip().lower()
        
        matched_header = None
        for sec, keywords in header_patterns.items():
            if line_test in keywords or any(line_test == kw for kw in keywords):
                matched_header = sec
                break
                
        if matched_header:
            current_section = matched_header
            continue
            
        if current_section:
            sections[current_section].append(line_clean)
            
    # Process each section with spaCy context if needed
    summary = " ".join(sections["summary"])
    if not summary:
        non_empty = [l for l in lines[:10] if l.strip() and not any(kw in l.lower() for kw in ['summary', 'experience', 'skills', 'education', '@', 'http'])]
        if len(non_empty) > 1:
            summary = " ".join(non_empty[1:4])
            
    skills = parse_skills(sections["skills"])
    if not skills:
        common_skills = ['Java', 'Python', 'SQL', 'Excel', 'C++', 'JavaScript', 'HTML', 'CSS', 'Accounting', 'GAAP', 
                         'Finance', 'Tax', 'Audit', 'Communication', 'Management', 'Leader', 'QuickBooks', 'SAP']
        for cs in common_skills:
            if re.search(r'\b' + re.escape(cs) + r'\b', text, re.IGNORECASE):
                skills.append(cs)
                
    doc_exp = nlp(" ".join(sections["experience"])) if sections["experience"] else doc
    doc_edu = nlp(" ".join(sections["education"])) if sections["education"] else doc
    
    experience = parse_experience(sections["experience"], doc_exp)
    education = parse_education(sections["education"], doc_edu)
    projects = parse_projects(sections["projects"])
    
    certifications = []
    for line in sections["certifications"]:
        cleaned = clean_bullet(line)
        if cleaned and len(cleaned) < 80:
            certifications.append(cleaned)
            
    return {
        "personal_info": {
            "full_name": full_name,
            "email": email,
            "phone": phone,
            "links": links
        },
        "summary": summary if summary else "Đoạn giới thiệu bản thân.",
        "skills": skills,
        "experience": experience,
        "projects": projects,
        "education": education,
        "certifications": certifications
    }

class ResumeLocalParser:
    def __init__(self, input_csv: str, output_json: str, sample_file: str, sample_size: int, seed: int):
        self.input_csv = input_csv
        self.output_json = output_json
        self.sample_file = sample_file
        self.sample_size = sample_size
        self.seed = seed
        
        # Load spaCy model locally
        print("Đang tải thư viện spaCy NLP...")
        self.nlp = spacy.load("en_core_web_sm")
        self.sampled_paths = self._get_or_create_samples()

    def _get_or_create_samples(self) -> list:
        if os.path.exists(self.sample_file):
            try:
                with open(self.sample_file, 'r', encoding='utf-8') as f:
                    samples = json.load(f)
                    print(f"Đã tải {len(samples)} file mẫu từ {self.sample_file}")
                    return samples
            except Exception as e:
                print(f"[CẢNH BÁO] Lỗi khi đọc file mẫu cũ: {e}. Sẽ tạo lại mẫu mới.")

        if not os.path.exists(self.input_csv):
            print(f"[LỖI] Không tìm thấy file dữ liệu đầu vào: {self.input_csv}")
            return []

        print(f"Đang đọc dữ liệu đầu vào từ {self.input_csv}...")
        all_rows = []
        try:
            with open(self.input_csv, mode='r', encoding='utf-8-sig') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    file_path = row.get('file_path', '')
                    text = row.get('extracted_text', '')
                    if file_path and text and text.strip():
                        all_rows.append(file_path)
        except Exception as e:
            print(f"[LỖI] Lỗi khi đọc CSV: {e}")
            return []

        total_available = len(all_rows)
        print(f"Tổng số file CV có dữ liệu text: {total_available}")
        
        size_to_sample = min(self.sample_size, total_available)
        random.seed(self.seed)
        sampled_paths = random.sample(all_rows, size_to_sample)
        
        try:
            with open(self.sample_file, 'w', encoding='utf-8') as f:
                json.dump(sampled_paths, f, ensure_ascii=False, indent=2)
            print(f"Đã lưu danh sách {len(sampled_paths)} file mẫu được chọn vào {self.sample_file}")
        except Exception as e:
            print(f"[CẢNH BÁO] Không thể lưu danh sách file mẫu: {e}")

        return sampled_paths

    def run(self):
        if not self.sampled_paths:
            print("[LỖI] Không có file mẫu nào để xử lý, dừng tiến trình.")
            return

        # Load CSV data into a fast lookup dict
        print("Đang tải dữ liệu text từ CSV vào bộ nhớ...")
        text_lookup = {}
        try:
            with open(self.input_csv, mode='r', encoding='utf-8-sig') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    text_lookup[row.get('file_path', '')] = row.get('extracted_text', '')
        except Exception as e:
            print(f"[LỖI] Không thể đọc dữ liệu CSV vào bộ nhớ: {e}")
            return

        print(f"Bắt đầu chuyển đổi offline {len(self.sampled_paths)} file mẫu bằng spaCy NLP...")
        parsed_results = []
        
        for idx, file_path in enumerate(self.sampled_paths, 1):
            text = text_lookup.get(file_path, '')
            if not text or not text.strip():
                print(f"[{idx}/{len(self.sampled_paths)}] Bỏ qua file rỗng: {file_path}")
                continue

            parsed_data = parse_cv_to_template(text, self.nlp)
            parsed_data['file_path'] = file_path
            parsed_results.append(parsed_data)
            
            if idx % 100 == 0 or idx == len(self.sampled_paths):
                print(f"Đã chuyển đổi: {idx}/{len(self.sampled_paths)}")

        # Write final JSON output
        try:
            with open(self.output_json, 'w', encoding='utf-8') as f:
                json.dump(parsed_results, f, ensure_ascii=False, indent=2)
            print(f"[THÀNH CÔNG] Đã ghi toàn bộ {len(parsed_results)} đối tượng vào {self.output_json}")
        except Exception as e:
            print(f"[LỖI] Không thể ghi file kết quả: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Local NLP sample và convert 1000 CV sang JSON template")
    parser.add_argument("-i", "--input", type=str, default=r"E:\project\ResumeSearchEngine\modelAIConvertText\cleaned_ocr_results.csv", help="Input CSV")
    parser.add_argument("-o", "--output", type=str, default=r"E:\project\ResumeSearchEngine\modelAIConvertText\parsed_resumes_1000.json", help="File JSON kết quả")
    parser.add_argument("-s", "--sample-file", type=str, default=r"E:\project\ResumeSearchEngine\modelAIConvertText\sampled_resumes_list.json", help="File danh sách mẫu đã chọn")
    parser.add_argument("-n", "--sample-size", type=int, default=1000, help="Số lượng mẫu cần chọn")
    parser.add_argument("--seed", type=int, default=42, help="Seed ngẫu nhiên")
    parser.add_argument("--test", action="store_true", help="Chạy thử với 3 mẫu")
    
    args = parser.parse_args()
    
    sample_size = 3 if args.test else args.sample_size
    output_json = args.output.replace(".json", "_test.json") if args.test else args.output
    sample_file = args.sample_file.replace(".json", "_test.json") if args.test else args.sample_file

    transformer = ResumeLocalParser(
        input_csv=args.input,
        output_json=output_json,
        sample_file=sample_file,
        sample_size=sample_size,
        seed=args.seed
    )
    transformer.run()
