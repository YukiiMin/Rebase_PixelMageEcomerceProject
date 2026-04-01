-- V20260319203500__add_guest_reading_used_at.sql
ALTER TABLE Accounts
ADD COLUMN guest_reading_used_at TIMESTAMP NULL DEFAULT NULL;
