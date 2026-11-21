package com.eliascampos.usermanagement.auth;

import com.eliascampos.usermanagement.auth.dto.LoginRequest;
import com.eliascampos.usermanagement.auth.dto.RefreshRequest;
import com.eliascampos.usermanagement.auth.dto.TokenResponse;
import com.eliascampos.usermanagement.exception.ApiError;
import com.eliascampos.usermanagement.security.AppUserDetails;
import com.eliascampos.usermanagement.security.JwtService;
import com.eliascampos.usermanagement.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "Autenticação", description = "Login, emissão de token e refresh")
@RestController
@RequestMapping(value = "/auth", produces = "application/json")
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

    @Operation(summary = "Autentica com e-mail e senha",
            description = "Valida as credenciais e devolve um access token (1 h) e um refresh token (7 dias). "
                    + "Bloqueia após 5 tentativas erradas por e-mail + IP em 15 min (429).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login",
                    headers = @Header(name = "Retry-After",
                            description = "Segundos até poder tentar novamente"),
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
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

    @Operation(summary = "Invalida os refresh tokens do usuário autenticado",
            description = "Encerra a sessão: o refresh token emitido deixa de valer. Exige access token.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sessão encerrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication) {
        userService.logout(authentication.getName());
    }

    @Operation(summary = "Troca o refresh token por um novo access token",
            description = "Use quando o access token expirar. O refresh é rotativo: cada uso devolve um par novo e invalida o anterior.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo par de tokens"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return jwtService.refresh(request.refreshToken());
    }
}
