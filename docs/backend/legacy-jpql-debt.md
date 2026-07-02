# Legacy JPQL Debt

These repository methods still use JPQL against the pre-migration integer-ID model. They are quarantined as migration debt because issue #41 establishes the PostgreSQL/Flyway foundation but does not rewrite each domain module's service and entity model.

Future module migration issues must replace these queries with raw PostgreSQL SQL through `NamedParameterJdbcTemplate` or a small repository-local native query, targeting the Flyway schema.

- `src/main/java/org/example/hotelbookingservice/repository/BookingRepository.java`
- `src/main/java/org/example/hotelbookingservice/repository/HotelRepository.java`
- `src/main/java/org/example/hotelbookingservice/repository/HotelamenityRepository.java`
- `src/main/java/org/example/hotelbookingservice/repository/ReviewRepository.java`
- `src/main/java/org/example/hotelbookingservice/repository/RoomRepository.java`
- `src/main/java/org/example/hotelbookingservice/repository/RoomamenityRepository.java`
- `src/main/java/org/example/hotelbookingservice/repository/UserRepository.java`
