-- V10: Indexes for frequent booking access patterns
--
-- IDX_NBP_BOOKING_USER_ID_ID supports:
-- - GET /api/bookings/my
-- - GET /api/payments/my through the NBP_PAYMENT -> NBP_BOOKING join by USER_ID
--
-- IDX_NBP_BOOKING_PACKAGE_STATUS supports:
-- - capacity checks before creating a TRAVEL_PACKAGE booking
-- - review lookup by travel package through NBP_REVIEW -> NBP_BOOKING

CREATE INDEX IDX_NBP_BOOKING_USER_ID_ID
    ON NBP_BOOKING (USER_ID, ID);

CREATE INDEX IDX_NBP_BOOKING_PACKAGE_STATUS
    ON NBP_BOOKING (TRAVEL_PACKAGE_ID, STATUS);
