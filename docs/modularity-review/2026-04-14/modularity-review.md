# Modularity Review

**Scope**: Entire codebase (`nbp-travel` — Spring Boot JDBC monolith)
**Date**: 2026-04-14

## Executive Summary

NBP Travel is a travel booking backend built as a single Spring Boot application with a layered architecture (controller -> service -> repository). The system handles user management, catalog management, booking/payment workflows, and audit logging. The overall modularity is **healthy for a monolith of this size**, with a clean layered architecture and no circular dependencies. The most notable finding is that `BookingService` and `TravelPackageService` act as **integration hubs** with 6 repository dependencies each, creating [functional coupling](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) across multiple domain concepts — but given the single-team ownership and low [distance](https://coupling.dev/posts/dimensions-of-coupling/distance/), this is tolerable rather than critical.

## Coupling Overview

| Integration | [Strength](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | [Distance](https://coupling.dev/posts/dimensions-of-coupling/distance/) | [Volatility](https://coupling.dev/posts/dimensions-of-coupling/volatility/) | [Balanced?](https://coupling.dev/posts/core-concepts/balance/) |
| --- | --- | --- | --- | --- |
| PaymentService -> BookingRepository (status update) | [Functional](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | Low (same module) | High ([core subdomain](https://coupling.dev/posts/dimensions-of-coupling/volatility/)) | Balanced |
| BookingService -> TravelPackageRepository, AccommodationRepository, TransportRepository | [Functional](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | Low (same module) | High ([core subdomain](https://coupling.dev/posts/dimensions-of-coupling/volatility/)) | Balanced |
| TravelPackageService -> AccommodationMapper, TransportMapper (direct calls) | [Model](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | Low (same module) | Medium ([supporting](https://coupling.dev/posts/dimensions-of-coupling/volatility/)) | Balanced |
| Services -> Entity classes (shared domain model) | [Model](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | Low (same module) | Low | Balanced |
| Controllers -> AuthContext (ThreadLocal) | [Functional](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | Low (same module) | Low ([generic](https://coupling.dev/posts/dimensions-of-coupling/volatility/)) | Balanced |
| Enrichment pattern (Service -> secondary Repository for name lookup) | [Functional](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | Low (same module) | Low ([generic/supporting](https://coupling.dev/posts/dimensions-of-coupling/volatility/)) | Balanced |
| BookingService -> PaymentRepository (confirm check) | [Functional](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) | Low (same module) | High ([core](https://coupling.dev/posts/dimensions-of-coupling/volatility/)) | Balanced |

## Issue: PaymentService Directly Manages Booking State

**Integration**: PaymentService -> BookingRepository
**Severity**: Significant

### Knowledge Leakage

`PaymentService.create()` at line 94 directly calls `bookingRepository.updateStatus(bookingId, CONFIRMED.name())`. This means the payment component has [functional knowledge](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) of booking business rules — specifically that "a completed payment should transition a booking from PENDING to CONFIRMED." The booking state machine logic (PENDING -> CONFIRMED -> CANCELLED) is not encapsulated within `BookingService`; instead, it leaks into the payment layer.

### Complexity Impact

A developer modifying the booking confirmation rules (e.g., "bookings should only be confirmed after agent approval, even with payment") would need to trace the status change logic across both `BookingService.confirm()` and `PaymentService.create()`. The booking state transition has two entry points — the explicit `confirm()` action and the implicit payment side-effect — making the behavior harder to reason about. This is a [complexity](https://coupling.dev/posts/core-concepts/complexity/) concern: the outcome of "what confirms a booking?" is split across two services.

### Cascading Changes

- If the booking status values change (e.g., adding an `AWAITING_APPROVAL` state before `CONFIRMED`), both `BookingService` and `PaymentService` must be updated.
- If payment confirmation logic changes (e.g., deferred payments that don't immediately confirm), the side-effect in `PaymentService` must be removed and the logic relocated.
- If a refund feature is added, `PaymentService` would need to reverse the booking status — further deepening the knowledge leakage.

### Recommended Improvement

Move the booking status transition out of `PaymentService`. Instead, have `PaymentService.create()` return the payment result, and let the caller (controller or a dedicated orchestration method in `BookingService`) handle the booking confirmation:

```java
// In BookingService or BookingController:
var payment = paymentService.create(bookingId, request, userId);
bookingService.confirm(bookingId);
```

This keeps the booking state machine fully encapsulated in `BookingService`. The trade-off is an extra method call and slightly more code in the controller, but it makes the booking lifecycle predictable from a single location. Since [distance](https://coupling.dev/posts/dimensions-of-coupling/distance/) is low (same module, same team), this refactoring is low-cost.

## Issue: BookingService as Integration Hub (6 Repository Dependencies)

**Integration**: BookingService -> TravelPackageRepository, AccommodationRepository, TransportRepository, UserRepository, PaymentRepository, BookingRepository
**Severity**: Significant

### Knowledge Leakage

`BookingService` has [functional coupling](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) to three separate catalog domains (packages, accommodations, transports) because it needs to: (1) validate entity existence, (2) check capacity, and (3) calculate price. This means `BookingService` understands the internal pricing model of each entity — `basePrice` for packages, `pricePerNight` for accommodations, `price` for transports. It also understands the capacity model (`maxCapacity` vs. `capacity`).

### Complexity Impact

The `calculateTotalPrice()` method (lines 155-187) is a branching dispatch based on `BookingType` that reaches into three different repositories. Each branch understands a different entity's pricing semantics. A developer adding a fourth bookable entity type (e.g., "ACTIVITY") would need to modify `BookingService` across three private methods: `validateForeignKeys()`, `calculateTotalPrice()`, and the booking creation flow. This exceeds the ideal cognitive scope of a single component.

### Cascading Changes

- Adding a new bookable entity type requires changes to `BookingType` enum, `BookingRequest` DTO, `BookingEntity`, `BookingRepository`, and three methods in `BookingService`.
- Changing pricing logic for accommodations (e.g., seasonal pricing, multi-night discounts) requires modifying `BookingService.calculateTotalPrice()` instead of `AccommodationService`.
- Changing capacity rules (e.g., overbooking tolerance) requires modifying `BookingService` rather than the entity's own service.

### Recommended Improvement

Extract a `PricingStrategy` or `BookableEntityResolver` that each entity service implements. `BookingService` would call a unified interface rather than branching on type:

```java
interface BookableEntity {
    BigDecimal getPrice();
    int getCapacity();
    long getConfirmedBookingCount();
}
```

Each service (`TravelPackageService`, `AccommodationService`, `TransportService`) provides its own implementation. `BookingService` only knows the `BookableEntity` [contract](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/), reducing strength from functional to contract coupling. The trade-off is an additional abstraction layer, which is only worthwhile if you expect to add more bookable types. For a college project with 3 fixed types, the current approach is acceptable — flag this as technical debt for future growth.

## Issue: Repository Enrichment Creates Implicit Coupling

**Integration**: Multiple services -> Secondary repositories (for FK name lookups)
**Severity**: Minor

### Knowledge Leakage

Five services follow a "repository enrichment" pattern where they inject a secondary repository solely to resolve a foreign key into a human-readable name:
- `DestinationService` -> `CityRepository` (for `cityName`)
- `AccommodationService` -> `DestinationRepository` (for `destinationName`)
- `TransportService` -> `DestinationRepository` (for `destinationName`)
- `CityService` -> `CountryRepository` (for `countryName`)
- `TravelPackageService` -> `DestinationRepository` (for `destinationName`)

Each service has [model coupling](https://coupling.dev/posts/dimensions-of-coupling/integration-strength/) to the secondary entity — it knows the entity's structure (`getName()`) to extract the display name.

### Complexity Impact

This is a low-severity concern because the enrichment pattern is simple and consistent, and the entities involved are in [generic/supporting subdomains](https://coupling.dev/posts/dimensions-of-coupling/volatility/) with low [volatility](https://coupling.dev/posts/dimensions-of-coupling/volatility/). Country names, city names, and destination names change very rarely. The cognitive load is manageable because the pattern is uniform across all services.

### Cascading Changes

If the `DestinationEntity.getName()` method were renamed or the name field restructured, five services would need updating. However, since this is a low-volatility area, such changes are unlikely.

### Recommended Improvement

For a monolith of this size, the current approach is acceptable. If this were a distributed system, SQL JOINs in the repository layer would be the idiomatic solution — fetching the enriched data in a single query rather than N+1 lookups. The current approach is simple and readable; optimizing it would add complexity without proportional benefit. Accept the [unbalanced coupling](https://coupling.dev/posts/core-concepts/balance/) given the low volatility.

---

_This analysis was performed using the [Balanced Coupling](https://coupling.dev) model by [Vlad Khononov](https://vladikk.com)._
