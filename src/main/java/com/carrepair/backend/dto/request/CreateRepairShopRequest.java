package com.carrepair.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRepairShopRequest {
    private Long userId;
    private String shopName;
    private String description;
    private String phone;
    private String address;
    private double latitude;
    private double longitude;
    private String cnicUrl;
    private String businessDocUrl;
}