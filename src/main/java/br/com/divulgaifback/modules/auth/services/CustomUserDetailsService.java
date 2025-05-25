package br.com.divulgaifback.modules.auth.services;

import br.com.divulgaifback.common.exceptions.custom.UnauthorizedException;
import br.com.divulgaifback.modules.auth.entities.AuthenticatedUser;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.userRepository.findByEmail(email)
                .map(AuthenticatedUser::new)
                .orElseThrow(UnauthorizedException::new);
    }

    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        return this.userRepository.findById(Integer.valueOf(id))
                .map(AuthenticatedUser::new)
                .orElseThrow(UnauthorizedException::new);
    }
}

