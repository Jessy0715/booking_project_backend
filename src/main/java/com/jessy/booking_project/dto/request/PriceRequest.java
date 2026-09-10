package com.jessy.booking_project.dto.request;

/** 建立/修改場地時傳入的價格。值是數字，跟回應的字串不一樣（契約如此）。 */
public record PriceRequest(Integer morning, Integer afternoon, Integer night) {
}
