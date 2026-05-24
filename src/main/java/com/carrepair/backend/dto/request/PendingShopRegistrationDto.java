package com.carrepair.backend.dto.request;


import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingShopRegistrationDto implements Serializable {

    private String fullName;
    private String email;
    private String phone;

    private String shopName;
    private String description;
    private String address;

    private Double latitude;
    private Double longitude;
}
