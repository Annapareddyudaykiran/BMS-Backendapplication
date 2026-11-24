CREATE TABLE safety_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_no BIGSERIAL UNIQUE NOT NULL,
    report_type VARCHAR(32) NOT NULL,
    title VARCHAR(240) NOT NULL,
    location VARCHAR(240) NOT NULL,
    severity VARCHAR(24) NOT NULL,
    details TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'Open',
    corrective_work_order VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_safety_reports_status ON safety_reports(status, created_at DESC);

CREATE TABLE safety_inspections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inspection_name VARCHAR(180) NOT NULL,
    location VARCHAR(240) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'Scheduled',
    result TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_safety_inspections_due ON safety_inspections(due_date);
