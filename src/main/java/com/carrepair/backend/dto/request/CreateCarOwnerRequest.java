package com.carrepair.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCarOwnerRequest {
    private String fullName;
    private String email;
    private String phone;
}
