package com.crm.backend.auth;

import com.crm.backend.others.ApiResponse;
import com.crm.backend.user.CustomUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * Serves as a controller for requests associated with authentication of app users.
 * This controller can be accessed by app users with any roles, as we can't check
 * authorize users who are not yet registered or logged in.
 *
 * @author shohrukhyakhyoev
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private final AuthService authService;

    @PostMapping("/register/")
    public ResponseEntity<ApiResponse> register(@RequestBody CustomUser request) {
        return authService.register(request);
    }

    @PostMapping("/authenticate/")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        return authService.authenticate(request);
    }

    @PostMapping("/refresh-token/")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return authService.refreshToken(request, response);
    }

}