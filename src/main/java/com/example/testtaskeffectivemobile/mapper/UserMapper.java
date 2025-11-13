package com.example.testtaskeffectivemobile.mapper;

import com.example.testtaskeffectivemobile.dto.request.ProfileUpdateRequest;
import com.example.testtaskeffectivemobile.dto.request.RegistrationRequest;
import com.example.testtaskeffectivemobile.dto.response.UserResponse;
import com.example.testtaskeffectivemobile.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public void mergeUserInfo(final User savedUser,final ProfileUpdateRequest request){
        if (!savedUser.getFirstName().equals(request.getFirstName())) {
            savedUser.setFirstName(request.getFirstName());
        }
        if (!savedUser.getLastName().equals(request.getLastName())) {
            savedUser.setLastName(request.getLastName());
        }
        if (!request.getDateOfBirth().equals(savedUser.getDateOfBirth())) {
            savedUser.setDateOfBirth(request.getDateOfBirth());
        }
    }
    public User toUser(RegistrationRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .build();
    }

    public UserResponse mapToUserResponse(User user){
        return UserResponse
                .builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
