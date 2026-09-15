package com.jessy.booking_project.demo;

/** 重置結果，回給呼叫者看做了什麼。 */
public record DemoResetResult(
        int deletedBookings,
        int deletedUsers,
        int seededRooms,
        int deletedImages) {
}
