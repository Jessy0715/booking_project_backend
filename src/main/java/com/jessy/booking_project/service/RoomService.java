package com.jessy.booking_project.service;

import com.jessy.booking_project.dto.response.RoomResponse;
import com.jessy.booking_project.entity.Room;
import com.jessy.booking_project.exception.RoomNotFoundException;
import com.jessy.booking_project.mapper.RoomMapper;
import com.jessy.booking_project.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 場地的業務邏輯。進出都是 DTO，Entity 只活在這個 class 內部。 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(RoomNotFoundException::new);
        return roomMapper.toResponse(room);
    }
}
