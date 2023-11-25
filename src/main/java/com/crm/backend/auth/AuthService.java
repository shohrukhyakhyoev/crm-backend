package com.crm.backend.auth;

import com.crm.backend.exception.ApiRequestException;
import com.crm.backend.others.ApiResponse;
import com.crm.backend.security.EmailValidator;
import com.crm.backend.security.JwtService;
import com.crm.backend.token.Token;
import com.crm.backend.token.TokenService;
import com.crm.backend.user.CustomUser;
import com.crm.backend.user.User;
import com.crm.backend.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;



/**
 * Serves as a service layer for requests associated with authorization of app users.
 *
 * @author shohrukhyakhyoev
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailValidator emailValidator;
    private final AuthenticationManager authenticationManager;



    /**
     * Registers new user.
     * Firstly, email of new user is validated: whether it is unique and
     * valid. If so, new user is added. Otherwise, exception is thrown.
     *
     * @param requestUser object of a new app user.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    public ResponseEntity<ApiResponse> register(CustomUser requestUser) {

        if (userService.findByEmail(requestUser.getEmail()).isPresent()) {
            throw new ApiRequestException("This email already exists!");
        }

        if (!emailValidator.isValid(requestUser.getEmail())) {
            throw new ApiRequestException("Email isn't valid.");
        }

        User user = User.builder()
                .firstName(requestUser.getFirstName())
                .lastName(requestUser.getLastName())
                .email(requestUser.getEmail())
                .age(requestUser.getAge())
                .phoneNumber(requestUser.getPhoneNumber())
                .password(passwordEncoder.encode(requestUser.getPassword()))
                .role(requestUser.getRole())
                .build();

        userService.saveUser(user);

        return new ResponseEntity<>(new ApiResponse("User is saved!"), HttpStatus.OK);

    }



    /**
     * Authorizes a user.
     * Firstly, it is checked if user with given email (from AuthRequest) exists.
     * Otherwise, exception is thrown. Then access and refresh tokens are generated
     * with method calls of jwtService.
     * Once new tokens are generated, all other previous tokens of users are revoked.
     * Then new tokens are saved and object of AuthResponse containing new tokens and
     * some user details is returned.
     *
     * @param request AuthRequest object containing email and password of app user.
     * @return ResponseEntity with AuthResponse object.
     * @throws ApiRequestException
     */
    public ResponseEntity<AuthResponse> authenticate(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException("User is not found!"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return new ResponseEntity<>((new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getRole().name())),
                HttpStatus.OK
        );

    }



    /**
     * Saves user token calling saveToken method from tokenService.
     *
     * @param user User object.
     * @param jwtToken String type of jwt token.
     * @throws ApiRequestException
     */
    private void saveUserToken(User user, String jwtToken) {

        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenService.saveToken(token);
    }



    /**
     * Deletes all unnecessary tokens of user.
     * Firstly, they are set to expired and revoked status and
     * then deleted.
     *
     * @param user object of a new app user.
     * @throws ApiRequestException
     */
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenService.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        validUserTokens.forEach(tokenService::delete);
    }



    /**
     * Refreshes access token of user.
     * Header is retrieved from the given request. Then refresh token is
     * retrieved from this header.
     * Firstly, email of new user is validated: whether it is unique and
     * valid. If so, new user is added. Otherwise, exception is thrown.
     *
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response
    ) throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            throw new ApiRequestException("Refresh token is not provided!");
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new ApiRequestException("User is not found!"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                return new ResponseEntity<>(new AuthResponse(
                        accessToken,
                        refreshToken,
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name()
                ), HttpStatus.OK);
            }

            throw new ApiRequestException("Refresh token is invalid");
        }

        throw new ApiRequestException("Can't extract username from token");
    }

    public String getStatusIfAgent(User user) {
        String status = "";
        if (user.getRole().name().equals("AGENT")) {
            status = userService.findAgentById(user.getId()).isPresent()
                    ? userService.findAgentById(user.getId()).get().getStatus().name()
                    : "";
        }
        return status;
    }

}