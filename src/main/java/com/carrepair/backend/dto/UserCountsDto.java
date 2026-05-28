package com.carrepair.backend.dto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserCountsDto {
    private Long all;
    private Long carOwners;
    private Long shopOwners;
    private Long admins;
}
