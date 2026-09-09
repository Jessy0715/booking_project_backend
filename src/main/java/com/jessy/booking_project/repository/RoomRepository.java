package com.jessy.booking_project.repository;

import com.jessy.booking_project.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

/** findById / findAll / save / delete 由 Spring Data 生成，不用自己寫。 */
public interface RoomRepository extends JpaRepository<Room, Long> {
}
