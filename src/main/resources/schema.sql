-- 部分唯一索引：同棚 + 同天 + 同時段，只有「有效」的預約（pending / approved）互斥。
-- rejected 的不算，所以被退回後別人還能預約同一個時段。
--
-- Hibernate 的 ddl-auto 做不出 WHERE 條件的索引，所以寫在這裡。
-- status 存的是 enum 的 name（大寫），不是 API 的小寫。
CREATE UNIQUE INDEX IF NOT EXISTS uk_booking_active_slot
    ON bookings (room_id, booking_date, time_slot)
    WHERE status IN ('PENDING', 'APPROVED');
