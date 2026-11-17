-- Bloqueo optimista de préstamos (@Version en LoanEntity). Los existentes parten de la versión 0.
ALTER TABLE prestamos ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
