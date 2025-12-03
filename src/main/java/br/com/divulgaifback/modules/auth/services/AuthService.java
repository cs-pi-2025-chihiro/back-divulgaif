package br.com.divulgaifback.modules.auth.services;

import br.com.divulgaifback.common.exceptions.custom.EmailException;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.common.exceptions.custom.SuapException;
import br.com.divulgaifback.common.exceptions.custom.UnauthorizedException;
import br.com.divulgaifback.common.exceptions.custom.ValidationException;
import br.com.divulgaifback.common.services.EmailService;
import br.com.divulgaifback.modules.auth.entities.AuthenticatedUser;
import br.com.divulgaifback.modules.auth.useCases.forgotPassword.ForgotPasswordRequest;
import br.com.divulgaifback.modules.auth.useCases.forgotPassword.ForgotPasswordResponse;
import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.auth.useCases.oauthLogin.OauthLoginRequest;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshRequest;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshResponse;
import br.com.divulgaifback.modules.auth.useCases.resetPassword.ResetPasswordRequest;
import br.com.divulgaifback.modules.auth.useCases.resetPassword.ResetPasswordResponse;
import br.com.divulgaifback.modules.users.entities.Role;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import br.com.divulgaifback.providers.suap.SuapService;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final LoginResponse loginUserResponse;
    private final JwtService jwtService;
    private final SuapService suapService;
    private final ObjectMapper objectMapper;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshResponse refreshTokenResponse;
    private final PasswordEncoder passwordEncoder;
    private final ForgotPasswordResponse forgotPasswordResponse;
    private final ResetPasswordResponse resetPasswordResponse;
    private final EmailService emailService;

    @Value("${auth.jwt.suap-token.secret}")
    private String SUAP_PROVIDER;

    @Value("${auth.jwt.access-token.expiration}")
    private Integer accessTokenExpirationTime;

    @Value("${auth.jwt.refresh-token.expiration}")
    private Integer refreshTokenExpirationTime;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            var token = new UsernamePasswordAuthenticationToken(loginRequest.identifier(), loginRequest.password());
            var authentication = this.authenticationManager.authenticate(token);

            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
            List<String> permissions = authenticatedUser.getRoles();

            return this.generateTokens(authenticatedUser.getId(), permissions, authenticatedUser.getUser());
        } catch (RuntimeException e) {
            if (e instanceof UnauthorizedException || e instanceof BadCredentialsException || e instanceof InternalAuthenticationServiceException) {
                throw new UnauthorizedException();
            }
            throw new RuntimeException("Error in logging in: " + e.getMessage());
        }
    }

    public LoginResponse oauthLogin(OauthLoginRequest request) {
        try {
            User user = null;

            if (Objects.equals(request.provider(), SUAP_PROVIDER)) {
                user = suapService.suapOauthLogin(request);
            }

            if (Objects.isNull(user)) throw new UnauthorizedException();

            List<String> permissions = user.getRoles().stream()
                    .map(Role::getName)
                    .toList();

            return generateTokens(user.getId(), permissions, user);
        } catch (Exception e) {
            if (e instanceof UnauthorizedException || e instanceof BadCredentialsException || e instanceof InternalAuthenticationServiceException) {
                throw new UnauthorizedException();
            }
            throw new SuapException("Error in suap login: " + e.getMessage());
        }
    }

    public RefreshResponse refresh(RefreshRequest refreshTokenRequest) {
        DecodedJWT decodedJWT = this.jwtService.decodeAndValidateToken(refreshTokenRequest.refreshToken());

        if (this.jwtService.isTokenExpired(decodedJWT)) throw new UnauthorizedException();

        if (!this.jwtService.isRefreshToken(decodedJWT)) throw new UnauthorizedException();

        Integer userId = Integer.parseInt(decodedJWT.getSubject());
        User user = this.userRepository.findById(userId).orElseThrow(UnauthorizedException::new);
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        var accessToken = this.jwtService.generateToken(user.getId(), roles, accessTokenExpirationTime, "access");

        return refreshTokenResponse.toPresentation(accessToken);
    }

    public LoginResponse generateTokens(Integer userId, List<String> permissions, User user) {
        var accessToken = this.jwtService.generateToken(userId, permissions, accessTokenExpirationTime, "access");
        var refreshToken = this.jwtService.generateToken(userId, permissions, refreshTokenExpirationTime, "refresh");

        return loginUserResponse.toPresentation(accessToken, refreshToken, user);
    }

    public static User getUserFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new UnauthorizedException();
        }

        return authenticatedUser.getUser();
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request, Locale locale) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> NotFoundException.with(User.class, "email", request.email()));

        String token = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        user.setForgotPasswordToken(token);
        user.setForgotPasswordTokenExpiresAt(expiresAt);
        userRepository.save(user);

        String languagePath = "";
        String resetRoute = "reset-password";

        if (Objects.isNull(locale)) locale = Locale.ENGLISH;
        String language = locale.getLanguage();

        if ("pt".equalsIgnoreCase(language)) {
            languagePath = "/pt";
            resetRoute = "alterar-senha";
        } else if ("en".equalsIgnoreCase(language)) languagePath = "/en";

        try {
            String resetLink = frontendUrl + languagePath + "/" + resetRoute + "?token=" + token;
            String userName = user.getName() != null ? user.getName() :
                ("pt".equalsIgnoreCase(language) ? "Usuário" : "User");

            emailService.sendPasswordResetEmail(user.getEmail(), userName, resetLink, language);
        } catch (Exception e) {
            log.error("Error sending password reset email to {}: {}", user.getEmail(), e.getMessage());
            throw new EmailException("Failed to send password reset email");
        }

        return forgotPasswordResponse.toPresentation("Password reset email sent successfully");
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByForgotPasswordToken(request.token())
                .orElseThrow(() -> new ValidationException("Invalid or expired reset token"));

        if (user.getForgotPasswordTokenExpiresAt() == null ||
            LocalDateTime.now().isAfter(user.getForgotPasswordTokenExpiresAt()))
            throw new ValidationException("Reset token has expired");

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setForgotPasswordToken(null);
        user.setForgotPasswordTokenExpiresAt(null);

        userRepository.save(user);

        return resetPasswordResponse.toPresentation("Password has been reset successfully");
    }
}
