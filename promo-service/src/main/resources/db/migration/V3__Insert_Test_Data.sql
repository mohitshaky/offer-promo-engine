
-- V3__Insert_Test_Data.sql
INSERT INTO vendors (vendor_code, vendor_name, email) VALUES
  ('V001','Acme Mobility','ops@acme.example'),
  ('V002','Globex Wireless','ops@globex.example')
  ON CONFLICT DO NOTHING;

INSERT INTO categories (name, description) VALUES
  ('Smartphones','All smartphone devices'),
  ('Accessories','Mobile accessories')
  ON CONFLICT DO NOTHING;
