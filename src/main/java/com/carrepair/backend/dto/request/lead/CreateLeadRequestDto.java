package com.carrepair.backend.dto.request.lead;


import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequestDto {
    private String title;
    private String description;
    private String carMake;
    private String carModel;
    private Integer carYear;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<String> imageKeys;
}