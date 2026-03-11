-- Add table for storing device configuration within a profile (S and lambda values)

CREATE TABLE profile_device_config
(
    profile_id   BIGINT       NOT NULL,
    device_id    BIGINT       NOT NULL,
    s_value      NUMERIC(3, 1) NOT NULL DEFAULT 0.5,
    lambda_value NUMERIC(10, 1) NOT NULL DEFAULT 10.0,
    PRIMARY KEY (profile_id, device_id),
    CONSTRAINT fk_pdc_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_pdc_device
        FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE CASCADE
);

-- Add index for faster lookups
CREATE INDEX idx_profile_device_config_profile ON profile_device_config (profile_id);
