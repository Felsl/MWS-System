package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.auth.GetCurrentUserUseCase;
import org.lvtn.mws.application.usecases.auth.LoginUseCase;
import org.lvtn.mws.infrastructure.security.JwtTokenProvider;
import org.lvtn.mws.interfaces.dto.request.auth.LoginRequest;
import org.lvtn.mws.interfaces.dto.response.auth.LoginResponse;
import org.lvtn.mws.interfaces.dto.response.user.UserResponse;
import org.lvtn.mws.interfaces.mapper.UserWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase          loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final JwtTokenProvider      tokenProvider;
    private final UserWebMapper         userWebMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader("Authorization") String bearer) {
        String token  = bearer.substring(7);
        String userId = tokenProvider.extractUserId(token);
        return ResponseEntity.ok(userWebMapper.toResponse(getCurrentUserUseCase.execute(userId)));
    }
}
