package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivineHelperResponse {
    private String uprightMeaning;
    private String reversedMeaning;
    private String zodiacSign;
    private String element;
    private String keywords;
}
