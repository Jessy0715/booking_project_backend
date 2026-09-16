package com.jessy.booking_project.room;

import com.jessy.booking_project.room.dto.RoomOptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** findById / findAll / save / delete 由 Spring Data 生成，不用自己寫。 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** 方法名就是查詢條件：title LIKE %keyword%，忽略大小寫。實作由 Spring Data 生成。 */
    Page<Room> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * 下拉選單用：只撈 id 與 title。
     *
     * <p>回傳型別是 record 而不是 Entity —— Spring Data 看到 record 會做 <b>Projection</b>，
     * 依 record 的欄位名（id、title）生成 {@code SELECT r.id, r.title FROM rooms r}，
     * 不會把整列十個欄位搬回來。打開 show-sql 可以看到差別。
     *
     * <p>方法名 {@code findAllBy} 後面沒有條件 = 全部。Sort 由呼叫端傳入。
     */
    List<RoomOptionResponse> findAllBy(Sort sort);
}
