package com.eliascampos.usermanagement.auth;

import com.eliascampos.usermanagement.auth.dto.LoginRequest;
import com.eliascampos.usermanagement.auth.dto.RefreshRequest;
import com.eliascampos.usermanagement.auth.dto.TokenResponse;
import com.eliascampos.usermanagement.security.AppUserDetails;
import com.eliascampos.usermanagement.security.JwtService;
import com.eliascampos.usermanagement.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Tag(name = "Autenticacao", description = "Login, emissao de token e refresh")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UserService userService, LoginRateLimiter loginRateLimiter) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @Operation(summary = "Autentica com e-mail e senha e devolve access e refresh token")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String key = request.email().trim().toLowerCase(Locale.ROOT) + "@" + httpRequest.getRemoteAddr();
        loginRateLimiter.check(key);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            loginRateLimiter.recordFailure(key);
            throw ex;
        }
        loginRateLimiter.reset(key);

        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        return jwtService.issue(principal.getUser());
    }

    @Operation(summary = "Invalida os refresh tokens do usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication) {
        userService.logout(authentication.getName());
    }

    @Operation(summary = "Troca o refresh token por um novo access token")
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return jwtService.refresh(request.refreshToken());
    }
}
