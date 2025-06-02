package br.com.divulgaifback.modules.auth.services;

import br.com.divulgaifback.common.exceptions.custom.UnauthorizedException;
import br.com.divulgaifback.modules.auth.entities.AuthenticatedUser;
import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshRequest;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshResponse;
import br.com.divulgaifback.modules.users.entities.Role;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import br.com.divulgaifback.providers.suap.SuapService;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final LoginResponse loginUserResponse;
    private final JwtService jwtService;
    private final SuapService suapService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshResponse refreshTokenResponse;

    @Value("${auth.jwt.access-token.expiration}")
    private Integer accessTokenExpirationTime;

    @Value("${auth.jwt.refresh-token.expiration}")
    private Integer refreshTokenExpirationTime;

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            String identifier = loginRequest.identifier();
            String password = loginRequest.password();

            if (suapService.comesFromSuap(identifier)) {
                return handleSuapLogin(identifier, password);
            } else {
                return handleRegularLogin(loginRequest);
            }
        } catch (RuntimeException e) {
            if (e instanceof UnauthorizedException || e instanceof BadCredentialsException) {
                throw new UnauthorizedException();
            }
            throw new RuntimeException("Error in logging in");
        }
    }

    public RefreshResponse refresh(RefreshRequest refreshTokenRequest) {
        DecodedJWT decodedJWT = this.jwtService.decodeAndValidateToken(refreshTokenRequest.refreshToken());

        if (this.jwtService.isTokenExpired(decodedJWT)) {
            throw new UnauthorizedException();
        }

        if (!this.jwtService.isRefreshToken(decodedJWT)) {
            throw new UnauthorizedException();
        }

        Integer userId = Integer.parseInt(decodedJWT.getSubject());
        User user = this.userRepository.findById(userId).orElseThrow(UnauthorizedException::new);
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        var accessToken = this.jwtService.generateToken(user.getId(), roles, accessTokenExpirationTime, "access");

        return refreshTokenResponse.toPresentation(accessToken);
    }

    @Transactional
    public LoginResponse handleSuapLogin(String matricula, String password) {
        try {
            SuapService.SuapTokenResponse suapTokens = suapService.authenticateWithSuap(matricula, password);

            User user = suapService.createOrUpdateSuapUser(matricula, suapTokens.accessToken());

            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .toList();

            // Vou apenas gerar os tokens do divulgaif já que o SUAP tem o mesmo tempo de expiração
            // para access tokens e refresh tokens o que é meio nada a ver
            return generateTokens(user.getId(), roles, user);

        } catch (UnauthorizedException e) {
            log.warn("SUAP authentication failed for matricula: {}", matricula);
            throw new UnauthorizedException();
        } catch (Exception e) {
            log.error("SUAP authentication error for matricula: {}", matricula, e);
            throw new RuntimeException("SUAP authentication service unavailable");
        }
    }

    private LoginResponse handleRegularLogin(LoginRequest loginRequest) {
        try {
            var token = new UsernamePasswordAuthenticationToken(loginRequest.identifier(), loginRequest.password());
            var authentication = this.authenticationManager.authenticate(token);

            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
            List<String> permissions = authenticatedUser.getRoles();

            return this.generateTokens(authenticatedUser.getId(), permissions, authenticatedUser.getUser());
        } catch (RuntimeException e) {
            throw new UnauthorizedException();
        }
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
}
