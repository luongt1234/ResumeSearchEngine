CREATE TABLE resume_batches (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    batch_name VARCHAR(150) NOT NULL,
    target_position VARCHAR(100),
    min_yoe DECIMAL(4,1),
    description TEXT
);

CREATE TABLE batch_skills (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    batch_id VARCHAR(36) NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    weight INT NOT NULL DEFAULT 1,
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_batch_skills_batch FOREIGN KEY (batch_id) REFERENCES resume_batches(id) ON DELETE CASCADE
);

CREATE TABLE resumes (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    etl_status VARCHAR(20) NOT NULL,
    error_message TEXT,
    batch_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_resumes_batch FOREIGN KEY (batch_id) REFERENCES resume_batches(id) ON DELETE CASCADE
);

CREATE TABLE resume_profiles (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resume_id VARCHAR(36) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    summary TEXT,
    yoe DECIMAL(4,1),
    CONSTRAINT fk_resume_profiles_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE TABLE resume_experiences (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resume_id VARCHAR(36) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    CONSTRAINT fk_resume_experiences_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE TABLE resume_educations (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resume_id VARCHAR(36) NOT NULL,
    institution VARCHAR(150) NOT NULL,
    degree VARCHAR(100),
    major VARCHAR(100),
    start_date DATE,
    end_date DATE,
    CONSTRAINT fk_resume_educations_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE TABLE resume_skills (
    id VARCHAR(36) PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resume_id VARCHAR(36) NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    CONSTRAINT fk_resume_skills_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);
