package com.YathzoBalo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {

    @Size(max = 50, message = "Display name must be less than 50 characters")
    private String displayName;

    @Email(message = "Email should be valid")
    private String email;
}
