package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByIdAndHotel_IdAndRoomType_Id(UUID id, UUID hotelId, UUID roomTypeId);

    Optional<Inventory> findByHotel_IdAndRoomType_IdAndDate(UUID hotelId, UUID roomTypeId, LocalDate date);

    @Query(value = """
            select *
            from inventories
            where hotel_id = :hotelId
              and room_type_id = :roomTypeId
              and (cast(:fromDate as date) is null or stay_date >= cast(:fromDate as date))
              and (cast(:toDate as date) is null or stay_date < cast(:toDate as date))
            order by stay_date
            """, nativeQuery = true)
    List<Inventory> findByHotelAndRoomTypeInDateRange(@Param("hotelId") UUID hotelId,
                                                      @Param("roomTypeId") UUID roomTypeId,
                                                      @Param("fromDate") LocalDate fromDate,
                                                      @Param("toDate") LocalDate toDate);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms, stop_sell)
            values (:id, :hotelId, :roomTypeId, :date, :totalRooms, :availableRooms, :stopSell)
            on conflict (room_type_id, stay_date)
            do update set total_rooms = excluded.total_rooms,
                          available_rooms = excluded.available_rooms,
                          stop_sell = excluded.stop_sell,
                          updated_at = now()
            """, nativeQuery = true)
    int upsertInventory(@Param("id") UUID id,
                        @Param("hotelId") UUID hotelId,
                        @Param("roomTypeId") UUID roomTypeId,
                        @Param("date") LocalDate date,
                        @Param("totalRooms") int totalRooms,
                        @Param("availableRooms") int availableRooms,
                        @Param("stopSell") boolean stopSell);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update inventories
            set available_rooms = available_rooms - :quantity,
                updated_at = now()
            where hotel_id = :hotelId
              and room_type_id = :roomTypeId
              and stay_date = :stayDate
              and not stop_sell
              and available_rooms >= :quantity
            """, nativeQuery = true)
    int reserveAvailableRooms(@Param("hotelId") UUID hotelId,
                              @Param("roomTypeId") UUID roomTypeId,
                              @Param("stayDate") LocalDate stayDate,
                              @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update inventories
            set available_rooms = least(total_rooms, available_rooms + :quantity),
                updated_at = now()
            where room_type_id = :roomTypeId
              and stay_date = :stayDate
            """, nativeQuery = true)
    int releaseAvailableRooms(@Param("roomTypeId") UUID roomTypeId,
                              @Param("stayDate") LocalDate stayDate,
                              @Param("quantity") int quantity);
}
