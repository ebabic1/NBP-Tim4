-- V6: Enforce "a discount can be used at most once"
-- Note: Oracle UNIQUE constraint allows multiple NULLs, so payments without a discount still work.
--
-- Existing data may already contain invalid duplicates. We normalize them by keeping the earliest payment
-- and clearing discount info from the rest.
UPDATE NBP_PAYMENT p
SET p.DISCOUNT_ID = NULL,
    p.DISCOUNT_AMOUNT = 0,
    p.FINAL_AMOUNT = p.AMOUNT
WHERE p.DISCOUNT_ID IS NOT NULL
  AND p.ID IN (
      SELECT id FROM (
          SELECT ID,
                 ROW_NUMBER() OVER (PARTITION BY DISCOUNT_ID ORDER BY ID) AS rn
          FROM NBP_PAYMENT
          WHERE DISCOUNT_ID IS NOT NULL
      )
      WHERE rn > 1
  );

ALTER TABLE NBP_PAYMENT
    ADD CONSTRAINT UK_PAYMENT_DISCOUNT_ID UNIQUE (DISCOUNT_ID);

