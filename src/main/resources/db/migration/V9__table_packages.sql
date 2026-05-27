-- V9: Table-specific PL/SQL packages
--
-- Requirement coverage:
-- - NBP_BOOKING_PKG is tied to NBP_BOOKING and exposes at least 3 procedures.
-- - NBP_PAYMENT_PKG is tied to NBP_PAYMENT and exposes at least 3 procedures.

CREATE OR REPLACE PACKAGE NBP_BOOKING_PKG AS
    PROCEDURE CREATE_BOOKING (
        p_user_id           IN  NBP_BOOKING.USER_ID%TYPE,
        p_booking_type      IN  NBP_BOOKING.BOOKING_TYPE%TYPE,
        p_total_price       IN  NBP_BOOKING.TOTAL_PRICE%TYPE,
        p_travel_package_id IN  NBP_BOOKING.TRAVEL_PACKAGE_ID%TYPE,
        p_accommodation_id  IN  NBP_BOOKING.ACCOMMODATION_ID%TYPE,
        p_transport_id      IN  NBP_BOOKING.TRANSPORT_ID%TYPE,
        p_booking_id        OUT NBP_BOOKING.ID%TYPE
    );

    PROCEDURE CONFIRM_BOOKING (
        p_booking_id IN NBP_BOOKING.ID%TYPE
    );

    PROCEDURE CANCEL_BOOKING (
        p_booking_id IN NBP_BOOKING.ID%TYPE
    );

    PROCEDURE GET_BOOKINGS_FOR_USER (
        p_user_id IN  NBP_BOOKING.USER_ID%TYPE,
        p_result  OUT SYS_REFCURSOR
    );
END NBP_BOOKING_PKG;
/

CREATE OR REPLACE PACKAGE BODY NBP_BOOKING_PKG AS
    PROCEDURE ENSURE_BOOKING_EXISTS (
        p_booking_id IN NBP_BOOKING.ID%TYPE
    ) AS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_count
          FROM NBP_BOOKING
         WHERE ID = p_booking_id;

        IF v_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20010, 'Booking does not exist.');
        END IF;
    END ENSURE_BOOKING_EXISTS;

    PROCEDURE VALIDATE_BOOKING_TARGET (
        p_booking_type      IN NBP_BOOKING.BOOKING_TYPE%TYPE,
        p_travel_package_id IN NBP_BOOKING.TRAVEL_PACKAGE_ID%TYPE,
        p_accommodation_id  IN NBP_BOOKING.ACCOMMODATION_ID%TYPE,
        p_transport_id      IN NBP_BOOKING.TRANSPORT_ID%TYPE
    ) AS
        v_target_count NUMBER := 0;
    BEGIN
        v_target_count := CASE WHEN p_travel_package_id IS NULL THEN 0 ELSE 1 END
                        + CASE WHEN p_accommodation_id IS NULL THEN 0 ELSE 1 END
                        + CASE WHEN p_transport_id IS NULL THEN 0 ELSE 1 END;

        IF v_target_count != 1 THEN
            RAISE_APPLICATION_ERROR(-20011, 'Booking must reference exactly one travel item.');
        END IF;

        IF p_booking_type = 'TRAVEL_PACKAGE' AND p_travel_package_id IS NULL THEN
            RAISE_APPLICATION_ERROR(-20012, 'Travel package booking requires TRAVEL_PACKAGE_ID.');
        ELSIF p_booking_type = 'ACCOMMODATION' AND p_accommodation_id IS NULL THEN
            RAISE_APPLICATION_ERROR(-20013, 'Accommodation booking requires ACCOMMODATION_ID.');
        ELSIF p_booking_type = 'TRANSPORT' AND p_transport_id IS NULL THEN
            RAISE_APPLICATION_ERROR(-20014, 'Transport booking requires TRANSPORT_ID.');
        ELSIF p_booking_type NOT IN ('TRAVEL_PACKAGE', 'ACCOMMODATION', 'TRANSPORT') THEN
            RAISE_APPLICATION_ERROR(-20015, 'Unsupported booking type.');
        END IF;
    END VALIDATE_BOOKING_TARGET;

    PROCEDURE CREATE_BOOKING (
        p_user_id           IN  NBP_BOOKING.USER_ID%TYPE,
        p_booking_type      IN  NBP_BOOKING.BOOKING_TYPE%TYPE,
        p_total_price       IN  NBP_BOOKING.TOTAL_PRICE%TYPE,
        p_travel_package_id IN  NBP_BOOKING.TRAVEL_PACKAGE_ID%TYPE,
        p_accommodation_id  IN  NBP_BOOKING.ACCOMMODATION_ID%TYPE,
        p_transport_id      IN  NBP_BOOKING.TRANSPORT_ID%TYPE,
        p_booking_id        OUT NBP_BOOKING.ID%TYPE
    ) AS
    BEGIN
        VALIDATE_BOOKING_TARGET(
            p_booking_type,
            p_travel_package_id,
            p_accommodation_id,
            p_transport_id
        );

        INSERT INTO NBP_BOOKING (
            ID,
            USER_ID,
            BOOKING_TYPE,
            BOOKING_DATE,
            STATUS,
            TOTAL_PRICE,
            TRAVEL_PACKAGE_ID,
            ACCOMMODATION_ID,
            TRANSPORT_ID
        ) VALUES (
            NBP_BOOKING_SEQ.NEXTVAL,
            p_user_id,
            p_booking_type,
            SYSDATE,
            'PENDING',
            p_total_price,
            p_travel_package_id,
            p_accommodation_id,
            p_transport_id
        )
        RETURNING ID INTO p_booking_id;
    END CREATE_BOOKING;

    PROCEDURE CONFIRM_BOOKING (
        p_booking_id IN NBP_BOOKING.ID%TYPE
    ) AS
    BEGIN
        ENSURE_BOOKING_EXISTS(p_booking_id);

        UPDATE NBP_BOOKING
           SET STATUS = 'CONFIRMED'
         WHERE ID = p_booking_id
           AND STATUS != 'CANCELLED';

        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20016, 'Cancelled booking cannot be confirmed.');
        END IF;
    END CONFIRM_BOOKING;

    PROCEDURE CANCEL_BOOKING (
        p_booking_id IN NBP_BOOKING.ID%TYPE
    ) AS
    BEGIN
        ENSURE_BOOKING_EXISTS(p_booking_id);

        UPDATE NBP_BOOKING
           SET STATUS = 'CANCELLED'
         WHERE ID = p_booking_id;
    END CANCEL_BOOKING;

    PROCEDURE GET_BOOKINGS_FOR_USER (
        p_user_id IN  NBP_BOOKING.USER_ID%TYPE,
        p_result  OUT SYS_REFCURSOR
    ) AS
    BEGIN
        OPEN p_result FOR
            SELECT ID,
                   USER_ID,
                   BOOKING_TYPE,
                   BOOKING_DATE,
                   STATUS,
                   TOTAL_PRICE,
                   TRAVEL_PACKAGE_ID,
                   ACCOMMODATION_ID,
                   TRANSPORT_ID
              FROM NBP_BOOKING
             WHERE USER_ID = p_user_id
             ORDER BY BOOKING_DATE DESC, ID DESC;
    END GET_BOOKINGS_FOR_USER;
END NBP_BOOKING_PKG;
/

CREATE OR REPLACE PACKAGE NBP_PAYMENT_PKG AS
    PROCEDURE CREATE_PAYMENT (
        p_booking_id IN  NBP_PAYMENT.BOOKING_ID%TYPE,
        p_method     IN  NBP_PAYMENT.METHOD%TYPE,
        p_discount_id IN NBP_PAYMENT.DISCOUNT_ID%TYPE,
        p_payment_id OUT NBP_PAYMENT.ID%TYPE
    );

    PROCEDURE COMPLETE_PAYMENT (
        p_payment_id IN NBP_PAYMENT.ID%TYPE
    );

    PROCEDURE REFUND_PAYMENT (
        p_payment_id IN NBP_PAYMENT.ID%TYPE
    );

    PROCEDURE GET_PAYMENT_FOR_BOOKING (
        p_booking_id IN  NBP_PAYMENT.BOOKING_ID%TYPE,
        p_result     OUT SYS_REFCURSOR
    );
END NBP_PAYMENT_PKG;
/

CREATE OR REPLACE PACKAGE BODY NBP_PAYMENT_PKG AS
    PROCEDURE ENSURE_PAYMENT_EXISTS (
        p_payment_id IN NBP_PAYMENT.ID%TYPE
    ) AS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_count
          FROM NBP_PAYMENT
         WHERE ID = p_payment_id;

        IF v_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20020, 'Payment does not exist.');
        END IF;
    END ENSURE_PAYMENT_EXISTS;

    PROCEDURE CREATE_PAYMENT (
        p_booking_id IN  NBP_PAYMENT.BOOKING_ID%TYPE,
        p_method     IN  NBP_PAYMENT.METHOD%TYPE,
        p_discount_id IN NBP_PAYMENT.DISCOUNT_ID%TYPE,
        p_payment_id OUT NBP_PAYMENT.ID%TYPE
    ) AS
        v_amount          NBP_PAYMENT.AMOUNT%TYPE;
        v_discount_amount NBP_PAYMENT.DISCOUNT_AMOUNT%TYPE := 0;
        v_percentage      NBP_DISCOUNT.PERCENTAGE%TYPE;
    BEGIN
        SELECT TOTAL_PRICE
          INTO v_amount
          FROM NBP_BOOKING
         WHERE ID = p_booking_id;

        IF p_discount_id IS NOT NULL THEN
            SELECT PERCENTAGE
              INTO v_percentage
              FROM NBP_DISCOUNT
             WHERE ID = p_discount_id
               AND SYSDATE BETWEEN VALID_FROM AND VALID_TO;

            v_discount_amount := ROUND(v_amount * v_percentage / 100, 2);
        END IF;

        INSERT INTO NBP_PAYMENT (
            ID,
            BOOKING_ID,
            DISCOUNT_ID,
            AMOUNT,
            DISCOUNT_AMOUNT,
            FINAL_AMOUNT,
            PAYMENT_DATE,
            METHOD,
            STATUS
        ) VALUES (
            NBP_PAYMENT_SEQ.NEXTVAL,
            p_booking_id,
            p_discount_id,
            v_amount,
            v_discount_amount,
            v_amount - v_discount_amount,
            SYSDATE,
            p_method,
            'PENDING'
        )
        RETURNING ID INTO p_payment_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20021, 'Booking or active discount does not exist.');
    END CREATE_PAYMENT;

    PROCEDURE COMPLETE_PAYMENT (
        p_payment_id IN NBP_PAYMENT.ID%TYPE
    ) AS
        v_booking_id NBP_PAYMENT.BOOKING_ID%TYPE;
    BEGIN
        ENSURE_PAYMENT_EXISTS(p_payment_id);

        UPDATE NBP_PAYMENT
           SET STATUS = 'COMPLETED',
               PAYMENT_DATE = SYSDATE
         WHERE ID = p_payment_id
           AND STATUS IN ('PENDING', 'FAILED');

        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20022, 'Only pending or failed payments can be completed.');
        END IF;

        SELECT BOOKING_ID
          INTO v_booking_id
          FROM NBP_PAYMENT
         WHERE ID = p_payment_id;

        NBP_BOOKING_PKG.CONFIRM_BOOKING(v_booking_id);
    END COMPLETE_PAYMENT;

    PROCEDURE REFUND_PAYMENT (
        p_payment_id IN NBP_PAYMENT.ID%TYPE
    ) AS
    BEGIN
        ENSURE_PAYMENT_EXISTS(p_payment_id);

        UPDATE NBP_PAYMENT
           SET STATUS = 'REFUNDED'
         WHERE ID = p_payment_id
           AND STATUS = 'COMPLETED';

        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20023, 'Only completed payments can be refunded.');
        END IF;
    END REFUND_PAYMENT;

    PROCEDURE GET_PAYMENT_FOR_BOOKING (
        p_booking_id IN  NBP_PAYMENT.BOOKING_ID%TYPE,
        p_result     OUT SYS_REFCURSOR
    ) AS
    BEGIN
        OPEN p_result FOR
            SELECT ID,
                   BOOKING_ID,
                   DISCOUNT_ID,
                   AMOUNT,
                   DISCOUNT_AMOUNT,
                   FINAL_AMOUNT,
                   PAYMENT_DATE,
                   METHOD,
                   STATUS
              FROM NBP_PAYMENT
             WHERE BOOKING_ID = p_booking_id
             ORDER BY PAYMENT_DATE DESC, ID DESC;
    END GET_PAYMENT_FOR_BOOKING;
END NBP_PAYMENT_PKG;
/
