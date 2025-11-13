package com.example.testtaskeffectivemobile.service;

import com.example.testtaskeffectivemobile.dto.request.ChangePasswordRequest;
import com.example.testtaskeffectivemobile.dto.request.ProfileUpdateRequest;
import com.example.testtaskeffectivemobile.dto.response.UserResponse;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.exception.BusinessException;
import com.example.testtaskeffectivemobile.exception.ErrorCode;
import com.example.testtaskeffectivemobile.mapper.UserMapper;
import com.example.testtaskeffectivemobile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public void updateProfileInfo(ProfileUpdateRequest request, String userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND,userId));
        userMapper.mergeUserInfo(user,request);
        userRepository.save(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, String userId) {
        if(!request.getNewPassword().equals(request.getNewPasswordConfirm())){
            throw new BusinessException(ErrorCode.CHANGE_PASSWORD_MISMATCH);
        }
        final User user = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND,userId));
        if(!passwordEncoder.matches(request.getOldPassword(),user.getPassword())){
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }
        final String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    public void deactivateAccount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND,userId));
        if(!user.isEnabled()){
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DEACTIVATED);
        }
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void reactivateAccount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND,userId));
        if(user.isEnabled()){
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_ACTIVATED);
        }
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(()->new UsernameNotFoundException
                        ("User with email "+ email +" not found"));
    }

    @Override
    public List<UserResponse> getActiveUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return List.of();
        }
        List<User> activeUsers = users.stream()
                .filter(User::isEnabled)
                .filter(user -> !user.isLocked())
                .filter(user -> !user.isCredentialsExpired())
                .toList();
        if (activeUsers.isEmpty()) {
            return List.of();
        }
        return activeUsers.stream()
                .map(userMapper::mapToUserResponse)
                .toList();
    }

}
