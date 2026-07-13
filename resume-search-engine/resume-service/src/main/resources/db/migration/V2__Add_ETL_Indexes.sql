-- V2: Thêm index tối ưu performance cho các truy vấn thường gặp sau ETL

-- Index tìm resume theo etl_status (phục vụ monitoring & retry logic)
CREATE INDEX idx_resumes_etl_status ON resumes (etl_status);

-- Index tìm resume theo user_id (phục vụ API "lấy CV của tôi")
CREATE INDEX idx_resumes_user_id ON resumes (user_id);

-- Index tìm resume theo batch (phục vụ API "CV trong batch này")
CREATE INDEX idx_resumes_batch_id ON resumes (batch_id);

-- Index tìm profile theo email (phục vụ dedup check)
CREATE INDEX idx_resume_profiles_email ON resume_profiles (email);

-- Index tìm skill theo tên (phục vụ skill-based filter)
CREATE INDEX idx_resume_skills_name ON resume_skills (skill_name);

-- Index tìm experience theo resume (phục vụ JOIN query)
CREATE INDEX idx_resume_experiences_resume_id ON resume_experiences (resume_id);

-- Index tìm education theo resume
CREATE INDEX idx_resume_educations_resume_id ON resume_educations (resume_id);
