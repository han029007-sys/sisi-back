package com.skinforge.upgrader.auth;

import com.skinforge.upgrader.auth.dto.response.UserResponse;
import com.skinforge.upgrader.bll.service.user.UserService;
import com.skinforge.upgrader.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SteamAuthController {

    private final SteamAuthService steamAuthService;
    private final UserService userService;
    private final JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping("/steam")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect(steamAuthService.buildLoginUrl());
    }

    @GetMapping("/steam/callback")
    public void callback(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        User user = steamAuthService.authenticate(request);

        String token = jwtService.generate(user.getId());

        response.sendRedirect(
                frontendUrl + "/auth/callback?token=" + token
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        return ResponseEntity.ok(
                userService.getById(userId)
        );
    }
}