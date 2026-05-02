
-- V2__Add_Indexes.sql
CREATE INDEX IF NOT EXISTS idx_promo_codes_status_dates ON promo_codes(status, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promo_usage_vendor_date ON promo_usage(vendor_id, used_at);
CREATE INDEX IF NOT EXISTS idx_promo_codes_code ON promo_codes(code);
