-- 部分唯一索引：同棚 + 同天 + 同時段，只有「有效」的預約互斥。
-- rejected 的不算，所以被退回後別人還能預約同一個時段。
--
-- Hibernate 的 ddl-auto 做不出 WHERE 條件的索引，所以寫在這裡。
-- status 存的是 enum 的 name（大寫），不是 API 的小寫。
--
-- CREATE INDEX IF NOT EXISTS 不會更新既有索引的條件，所以要先 DROP 再建。
DROP INDEX IF EXISTS uk_booking_active_slot;

CREATE UNIQUE INDEX uk_booking_active_slot
    ON bookings (room_id, booking_date, time_slot)
    -- 被取消的預約 status 仍是 PENDING，只有 deleted_at 有值，
    -- 所以要一起排除，否則它會繼續佔住時段。
    WHERE status IN ('PENDING', 'APPROVED') AND deleted_at IS NULL;
