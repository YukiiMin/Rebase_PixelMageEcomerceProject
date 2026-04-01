-- Fix schema
ALTER TABLE payments RENAME COLUMN stripe_payment_intent_id TO gateway_transaction_id;


ALTER TABLE payments
ADD COLUMN payment_gateway VARCHAR(50);

-- Backfill data
UPDATE payments
SET payment_gateway = 'STRIPE'
WHERE gateway_transaction_id LIKE 'pi_%';

UPDATE payments
SET payment_gateway = 'VNPAY'
WHERE gateway_transaction_id LIKE 'VNPAY_%';
