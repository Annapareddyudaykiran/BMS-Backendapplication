ALTER TABLE iot_devices ADD COLUMN owner_email VARCHAR(254);
UPDATE iot_devices SET owner_email='udaykiran@gmail.com';
ALTER TABLE iot_devices ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_iot_devices_owner ON iot_devices(owner_email, device_no);

ALTER TABLE iot_alerts ADD COLUMN owner_email VARCHAR(254);
UPDATE iot_alerts a SET owner_email=d.owner_email FROM iot_devices d WHERE d.id=a.device_id;
ALTER TABLE iot_alerts ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_iot_alerts_owner_created ON iot_alerts(owner_email, created_at DESC);

ALTER TABLE iot_readings ADD COLUMN owner_email VARCHAR(254);
UPDATE iot_readings r SET owner_email=d.owner_email FROM iot_devices d WHERE d.id=r.device_id;
ALTER TABLE iot_readings ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_iot_readings_owner_device_time ON iot_readings(owner_email, device_id, reading_at DESC);
