package com.jobnotifer.service;

import com.jobnotifer.dto.AuthResponse;
import com.jobnotifer.dto.LoginRequest;
import com.jobnotifer.dto.SignupRequest;
import com.jobnotifer.entity.User;
import com.jobnotifer.repository.UserRepository;
import com.jobnotifer.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    
    @Transactional
    public AuthResponse registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setFullName(signupRequest.getFullName());
        
        User savedUser = userRepository.save(user);
        
        String token = tokenProvider.generateTokenFromUserId(savedUser.getId());
        
        log.info("User registered successfully: {}", savedUser.getEmail());
        
        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getFullName());
    }
    
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        log.info("User authenticated successfully: {}", user.getEmail());
        
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName());
    }
}

