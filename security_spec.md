# Security Specification — Мой гараж

## 1. Data Invariants
1. **Multi-User Isolation**: Every document under `/users/{userId}/items/{itemId}`, `/users/{userId}/platforms/{platformId}`, `/users/{userId}/dialogs/{dialogId}`, and `/users/{userId}/zones/{zoneId}` belongs exclusively to `userId` and requires `request.auth != null && request.auth.uid == userId`.
2. **Identity Integrity**: On creation, `incoming().userId == request.auth.uid && incoming().userId == userId`. On update, `incoming().userId == existing().userId` and `incoming().createdAt == existing().createdAt`.
3. **Path Variable Hardening**: All document ID variables (`userId`, `itemId`, `platformId`, `dialogId`, `zoneId`) must pass `isValidId(id)` (`^[a-zA-Z0-9_\-]+$`, max 128 chars) on single-document operations (`get`, `create`, `update`, `delete`).
4. **Strict Key & Schema Enforcement**: All writes must pass `hasAll`, `hasOnly`, `affectedKeys().hasOnly(...)`, string `.size()` bounds, numeric bounds, and `<= request.time` timestamp bounds.

## 2. The Dirty Dozen Payloads
1. **Unauthenticated Read/Write**: `auth == null` reading `/users/alice/items` -> Rejected.
2. **Cross-User Read**: `bob` reading `/users/alice/items/item1` -> Rejected.
3. **Cross-User List**: `bob` listing `/users/alice/items` -> Rejected.
4. **Identity Spoofing on Create**: `alice` creating `/users/alice/items/item1` with `userId: "bob"` -> Rejected.
5. **Shadow Field Injection on Create**: `alice` creating `/users/alice/items/item1` with extra field `isAdmin: true` -> Rejected by `hasOnly()`.
6. **Shadow Field Injection on Update**: `alice` updating `/users/alice/items/item1` with `hacked: 1` -> Rejected by `affectedKeys().hasOnly()`.
7. **Immutable Field Mutation**: `alice` updating `createdAt` or `userId` on `/users/alice/items/item1` -> Rejected.
8. **String Overflow (Denial of Wallet)**: `alice` setting `title` to a 5000-character string -> Rejected by `title.size() <= 150`.
9. **Negative Price / Quantity**: `alice` setting `price: -100` -> Rejected by `price >= 0`.
10. **Invalid Enum Status**: `alice` setting `publicationStatus: "hacked_status"` -> Rejected by enum allowlist.
11. **Unbounded Array**: `alice` setting `publishedPlatforms` with 20 elements -> Rejected by `size() <= 10`.
12. **Future Timestamp Spoofing**: `alice` setting `createdAt` in the future (`> request.time`) -> Rejected by `<= request.time`.
