ALTER TABLE medical_reports
ALTER COLUMN file_data TYPE BYTEA
USING file_data::BYTEA;
