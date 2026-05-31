-- V11: XML import procedure for NBP_BOOKING

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

    PROCEDURE IMPORT_BOOKINGS_XML (
        p_xml       IN  CLOB,
        p_inserted  OUT NUMBER,
        p_updated   OUT NUMBER
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

    PROCEDURE IMPORT_BOOKINGS_XML (
        p_xml       IN  CLOB,
        p_inserted  OUT NUMBER,
        p_updated   OUT NUMBER
    ) AS
        v_count NUMBER;
        v_existing NUMBER;
    BEGIN
        p_inserted := 0;
        p_updated := 0;

        FOR rec IN (
            SELECT
                TO_NUMBER(xt.id)                                        AS id,
                TO_NUMBER(xt.user_id)                                   AS user_id,
                xt.booking_type                                         AS booking_type,
                TO_DATE(xt.booking_date, 'YYYY-MM-DD')                  AS booking_date,
                xt.status                                               AS status,
                TO_NUMBER(REPLACE(xt.total_price, ',', '.'))            AS total_price,
                TO_NUMBER(NULLIF(xt.travel_package_id, ''))             AS travel_package_id,
                TO_NUMBER(NULLIF(xt.accommodation_id, ''))              AS accommodation_id,
                TO_NUMBER(NULLIF(xt.transport_id, ''))                  AS transport_id
            FROM XMLTABLE('/bookings/booking'
                PASSING XMLTYPE(p_xml)
                COLUMNS
                    id                 VARCHAR2(20) PATH 'id',
                    user_id            VARCHAR2(20) PATH 'userId',
                    booking_type       VARCHAR2(20) PATH 'bookingType',
                    booking_date       VARCHAR2(10) PATH 'bookingDate',
                    status             VARCHAR2(20) PATH 'status',
                    total_price        VARCHAR2(30) PATH 'totalPrice',
                    travel_package_id  VARCHAR2(20) PATH 'travelPackageId',
                    accommodation_id   VARCHAR2(20) PATH 'accommodationId',
                    transport_id       VARCHAR2(20) PATH 'transportId'
            ) xt
        ) LOOP
            SELECT COUNT(*)
              INTO v_existing
              FROM NBP_BOOKING
             WHERE ID = rec.id;

            IF v_existing > 0 THEN
                UPDATE NBP_BOOKING
                   SET USER_ID           = rec.user_id,
                       BOOKING_TYPE      = rec.booking_type,
                       BOOKING_DATE      = rec.booking_date,
                       STATUS            = rec.status,
                       TOTAL_PRICE       = rec.total_price,
                       TRAVEL_PACKAGE_ID = rec.travel_package_id,
                       ACCOMMODATION_ID  = rec.accommodation_id,
                       TRANSPORT_ID      = rec.transport_id
                 WHERE ID = rec.id;
                p_updated := p_updated + 1;
            ELSE
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
                    rec.id,
                    rec.user_id,
                    rec.booking_type,
                    rec.booking_date,
                    rec.status,
                    rec.total_price,
                    rec.travel_package_id,
                    rec.accommodation_id,
                    rec.transport_id
                );
                p_inserted := p_inserted + 1;
            END IF;
        END LOOP;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END IMPORT_BOOKINGS_XML;
END NBP_BOOKING_PKG;
/