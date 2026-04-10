package com.example.PixelMageEcomerceProject.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {

    private String name;
    private String phoneNumber;
    private String avatarUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;

}
