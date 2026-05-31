# Booking Indexes Explain Plan

## Chosen Indexes

```sql
CREATE INDEX IDX_NBP_BOOKING_USER_ID_ID
    ON NBP_BOOKING (USER_ID, ID);

CREATE INDEX IDX_NBP_BOOKING_PACKAGE_STATUS
    ON NBP_BOOKING (TRAVEL_PACKAGE_ID, STATUS);
```

## Why These Indexes Help

`IDX_NBP_BOOKING_USER_ID_ID` supports endpoints that filter bookings by the current user:

- `GET /api/bookings/my`
- `GET /api/payments/my`, because payments are joined to bookings and filtered by `b.USER_ID`

`IDX_NBP_BOOKING_PACKAGE_STATUS` supports package capacity checks during booking creation:

- `POST /api/bookings` when `bookingType = 'TRAVEL_PACKAGE'`
- Review lookups by travel package, because reviews join through `NBP_BOOKING.TRAVEL_PACKAGE_ID`

## Query 1: User Bookings

```sql
SELECT *
FROM NBP_BOOKING
WHERE USER_ID = 1
ORDER BY ID
OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY;
```

Before indexes:

```text
| Id | Operation                | Name        | Cost |
|  0 | SELECT STATEMENT         |             | 4    |
|  1 | VIEW                     |             | 4    |
|  2 | WINDOW SORT PUSHED RANK  |             | 4    |
|  3 | TABLE ACCESS FULL        | NBP_BOOKING | 3    |
```

After indexes:

```text
| Id | Operation                     | Name                       | Cost |
|  0 | SELECT STATEMENT              |                            | 2    |
|  1 | VIEW                          |                            | 2    |
|  2 | WINDOW NOSORT STOPKEY         |                            | 2    |
|  3 | TABLE ACCESS BY INDEX ROWID   | NBP_BOOKING                | 2    |
|  4 | INDEX RANGE SCAN              | IDX_NBP_BOOKING_USER_ID_ID | 1    |
```

Result: Oracle can filter by `USER_ID` through the index and preserve `ID` ordering without a separate sort.

## Query 2: Travel Package Capacity Check

```sql
SELECT COUNT(*)
FROM NBP_BOOKING
WHERE TRAVEL_PACKAGE_ID = 1
  AND STATUS != 'CANCELLED';
```

Before indexes:

```text
| Id | Operation          | Name        | Cost |
|  0 | SELECT STATEMENT   |             | 3    |
|  2 | TABLE ACCESS FULL  | NBP_BOOKING | 3    |
```

After indexes:

```text
| Id | Operation          | Name                           | Cost |
|  0 | SELECT STATEMENT   |                                | 1    |
|  2 | INDEX RANGE SCAN   | IDX_NBP_BOOKING_PACKAGE_STATUS | 1    |
```

Result: Oracle can count matching package bookings directly from the composite index.
