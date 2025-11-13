package com.example.testtaskeffectivemobile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationRequest {
    @NotBlank(message = "VALIDATION.REGISTRATION.EMAIL.NOT_BLANK")
    @Email(message = "VALIDATION.REGISTRATION.EMAIL.FORMAT")
    private String email;
    @NotBlank(message = "VALIDATION.REGISTRATION.PASSWORD.NOT_BLANK")
    private String password;
    @NotBlank(message = "VALIDATION.REGISTRATION.PASSWORD.NOT_BLANK")
    private String confirmPassword;
    @NotBlank(message = "VALIDATION.REGISTRATION.FIRSTNAME.NOT_BLANK")
    private String firstName;
    @NotBlank(message = "VALIDATION.REGISTRATION.LASTNAME.NOT_BLANK")
    private String lastName;
}
