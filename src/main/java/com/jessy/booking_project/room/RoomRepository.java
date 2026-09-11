package com.jessy.booking_project.room;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** findById / findAll / save / delete 由 Spring Data 生成，不用自己寫。 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** 方法名就是查詢條件：title LIKE %keyword%，忽略大小寫。實作由 Spring Data 生成。 */
    Page<Room> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
