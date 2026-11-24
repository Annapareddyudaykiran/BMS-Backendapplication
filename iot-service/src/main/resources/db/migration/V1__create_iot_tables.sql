CREATE TABLE iot_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_no BIGSERIAL UNIQUE NOT NULL,
    device_name VARCHAR(180) NOT NULL,
    linked_asset VARCHAR(180),
    location VARCHAR(240) NOT NULL,
    reading_unit VARCHAR(24) NOT NULL,
    current_reading NUMERIC(16,4) NOT NULL DEFAULT 0,
    alert_threshold NUMERIC(16,4) NOT NULL,
    threshold_direction VARCHAR(12) NOT NULL DEFAULT 'ABOVE',
    status VARCHAR(24) NOT NULL DEFAULT 'Active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE iot_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL REFERENCES iot_devices(id) ON DELETE CASCADE,
    reading_value NUMERIC(16,4) NOT NULL,
    threshold_value NUMERIC(16,4) NOT NULL,
    alert_details VARCHAR(500) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'Open',
    work_order_reference VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);
CREATE INDEX idx_iot_alerts_status ON iot_alerts(status, created_at DESC);
CREATE TABLE iot_readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL REFERENCES iot_devices(id) ON DELETE CASCADE,
    reading_value NUMERIC(16,4) NOT NULL,
    reading_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_iot_readings_device_time ON iot_readings(device_id, reading_at DESC);
