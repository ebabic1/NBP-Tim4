-- V8: Scheduled maintenance for stale pending bookings
--
-- Advanced database feature:
-- - stored procedure encapsulates maintenance business logic
-- - DBMS_SCHEDULER job runs it periodically inside the database
-- - audit table receives a summary entry for each job execution

CREATE OR REPLACE PROCEDURE NBP_CANCEL_STALE_BOOKINGS (
    p_grace_days IN NUMBER DEFAULT 2
)
AUTHID DEFINER
AS
    v_cancelled_count NUMBER := 0;
BEGIN
    UPDATE NBP_BOOKING b
       SET b.STATUS = 'CANCELLED'
     WHERE b.STATUS = 'PENDING'
       AND b.BOOKING_DATE < SYSDATE - p_grace_days
       AND NOT EXISTS (
           SELECT 1
             FROM NBP_PAYMENT p
            WHERE p.BOOKING_ID = b.ID
              AND p.STATUS = 'COMPLETED'
       );

    v_cancelled_count := SQL%ROWCOUNT;

    INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
    VALUES (
        NBP_LOG_SEQ.NEXTVAL,
        'AUTO_CANCEL',
        'NBP_BOOKING: ' || v_cancelled_count,
        SYSTIMESTAMP,
        USER
    );

    COMMIT;
END;
/

/

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'NBP_STALE_BOOKINGS_JOB',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN NBP_CANCEL_STALE_BOOKINGS; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;BYHOUR=2;BYMINUTE=0;BYSECOND=0',
        enabled         => TRUE,
        comments        => 'Daily cancellation of pending bookings older than the payment grace period.'
    );
EXCEPTION
    WHEN OTHERS THEN
        -- Student schemas may not have CREATE JOB privilege. In that case the
        -- procedure is still created, and an admin can run this block later.
        IF SQLCODE != -27486 THEN
            RAISE;
        END IF;
END;
/
