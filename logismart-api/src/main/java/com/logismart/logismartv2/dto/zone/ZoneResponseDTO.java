package com.logismart.logismartv2.dto.zone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponseDTO {

    private String id;
    private String name;
    private String postalCode;

    public ZoneResponseDTO(String name, String postalCode) {
        this.name = name;
        this.postalCode = postalCode;
    }
}
