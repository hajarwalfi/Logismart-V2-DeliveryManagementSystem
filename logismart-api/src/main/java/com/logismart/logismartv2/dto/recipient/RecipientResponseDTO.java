package com.logismart.logismartv2.dto.recipient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientResponseDTO {

    private String id;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String phone;

    private String address;

    public RecipientResponseDTO(String id, String firstName, String lastName,
                                 String email, String phone, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
