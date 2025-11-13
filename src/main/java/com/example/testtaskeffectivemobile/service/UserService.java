package com.example.testtaskeffectivemobile.service;

import com.example.testtaskeffectivemobile.dto.request.ChangePasswordRequest;
import com.example.testtaskeffectivemobile.dto.request.ProfileUpdateRequest;
import com.example.testtaskeffectivemobile.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    void updateProfileInfo(ProfileUpdateRequest request, String userId);

    void changePassword(ChangePasswordRequest request, String userId);

    void deactivateAccount(String userId);

    void reactivateAccount(String userId);

    List<UserResponse> getActiveUsers();
}
