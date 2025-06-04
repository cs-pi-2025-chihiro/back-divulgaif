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
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
