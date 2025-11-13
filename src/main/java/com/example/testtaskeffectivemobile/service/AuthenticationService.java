package com.example.testtaskeffectivemobile.service;

import com.example.testtaskeffectivemobile.dto.request.AuthenticationRequest;
import com.example.testtaskeffectivemobile.dto.request.RefreshRequest;
import com.example.testtaskeffectivemobile.dto.request.RegistrationRequest;
import com.example.testtaskeffectivemobile.dto.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse login (AuthenticationRequest request);

    void register (RegistrationRequest request);

    AuthenticationResponse refreshToken (RefreshRequest request);

    void logout(String userId);
}
