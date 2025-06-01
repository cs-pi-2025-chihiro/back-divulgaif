package br.com.divulgaifback.providers.suap;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.common.exceptions.custom.SuapException;
import br.com.divulgaifback.common.exceptions.custom.UnauthorizedException;
import br.com.divulgaifback.common.utils.RestClientUtils;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.entities.Role;
import br.com.divulgaifback.modules.users.entities.enums.RoleEnum;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import br.com.divulgaifback.modules.users.repositories.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuapService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Value("${suap.api.base-url}")
    private String suapBaseUrl;

    private static final String SUAP_AUTH_ROUTE = "/api/token/pair";
    private static final String STUDENT_DATA_ROUTE = "/api/edu/dados-aluno-matriculado/?";
    private static final String TEACHER_DATA_ROUTE = "/api/rh/meus-dados/?";

    private static final List<String> SUAP_HEADERS = Arrays.asList(
            "Content-Type", "application/json",
            "User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"
    );

    private static final Pattern SUAP_MATRICULA_PATTERN = Pattern.compile("^\\d{8,11}$");

    public SuapTokenResponse authenticateWithSuap(String matricula, String password) throws IOException, InterruptedException {
        RestClientUtils client = new RestClientUtils(suapBaseUrl);

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "username", matricula,
                "password", password
        ));

        HttpResponse<String> response = client.post(SUAP_AUTH_ROUTE, requestBody, SUAP_HEADERS);

        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            return new SuapTokenResponse(
                    jsonResponse.get("access").asText(),
                    jsonResponse.get("refresh").asText(),
                    jsonResponse.get("username").asText()
            );
        } else if (response.statusCode() == 401) {
            throw new UnauthorizedException();
        } else {
            throw new SuapException("Error in SUAP authentication!");
        }
    }

    public User createOrUpdateSuapUser(String matricula, String suapAccessToken) {
        try {
            Optional<User> existingUser = userRepository.findByRa(matricula);

            if (existingUser.isPresent()) {
                return existingUser.get();
            }

            SuapUserData userData = fetchUserDataFromSuap(matricula, suapAccessToken);
            if (Objects.isNull(userData)) throw new NullPointerException("User Data is null for matricula" + matricula);

            return createUserFromSuapData(userData);
        } catch (Exception e) {
            if (e instanceof UnauthorizedException || e instanceof BadCredentialsException) {
                throw new UnauthorizedException();
            }
            log.error("Failed to create/update SUAP user for matricula: {}", matricula, e);
            throw new SuapException("Failed to create/update SUAP user");
        }
    }

    private SuapUserData fetchUserDataFromSuap(String matricula, String accessToken) {
        try {
            List<String> authHeader = Arrays.asList(
                    "Authorization", "Bearer " + accessToken,
                    "User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36",
                    "Content-Type", "application/json"
            );

            RestClientUtils client = new RestClientUtils(suapBaseUrl);

            HttpResponse<String> response = client.get(
                    STUDENT_DATA_ROUTE + "matricula=" + matricula,
                    authHeader
            );

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                return mapStudentData(jsonResponse);
            }

            response = client.get(
                    TEACHER_DATA_ROUTE + "matricula=" + matricula,
                    authHeader
            );

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                return mapTeacherData(jsonResponse);
            }

            if (response.statusCode() == 401) {
                throw new UnauthorizedException();
            }

            log.error("Failed to fetch user data from SUAP for matricula: {}", matricula);
            throw new SuapException("Failed to fetch user data from SUAP for matricula: " + matricula);
        } catch (Exception e) {
            if (e instanceof UnauthorizedException || e instanceof BadCredentialsException) {
                throw new UnauthorizedException();
            }
            throw new SuapException("Failed to fetch user data from SUAP for matricula: " + matricula);
        }
    }

    private SuapUserData mapStudentData(JsonNode jsonResponse) {
        return SuapUserData.builder()
                .matricula(jsonResponse.get("matricula").asText())
                .name(jsonResponse.get("nome").asText())
                .cpf(jsonResponse.get("cpf").asText())
                .userType(RoleEnum.IS_STUDENT.getValue())
                .build();
    }

    private SuapUserData mapTeacherData(JsonNode jsonResponse) {
        String email = jsonResponse.get("email").asText();
        return SuapUserData.builder()
                .matricula(jsonResponse.get("matricula").asText())
                .name(jsonResponse.get("nome_usual").asText())
                .cpf(jsonResponse.get("cpf").asText())
                .email(email)
                .userType(RoleEnum.IS_TEACHER.getValue())
                .build();
    }

    private User createUserFromSuapData(SuapUserData userData) {
        User user = new User();
        user.setRa(userData.getMatricula());
        user.setName(userData.getName());
        user.setCpf(userData.getCpf());
        user.setEmail(userData.getEmail());

        Set<Role> roles = new HashSet<>();
        String roleName = userData.getUserType().
                equals(RoleEnum.IS_TEACHER.getValue())
                ? RoleEnum.IS_TEACHER.getValue()
                : RoleEnum.IS_STUDENT.getValue();

        Role role = roleRepository.findByName(roleName).orElseThrow(() -> NotFoundException.with(Role.class, "name",  roleName));
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public boolean comesFromSuap(String identifier) {
        return SUAP_MATRICULA_PATTERN.matcher(identifier).matches();
    }

    public record SuapTokenResponse(String accessToken, String refreshToken, String username) {}

    @Builder
    @Data
    public static class SuapUserData {
        private String matricula;
        private String name;
        private String cpf;
        private String email;
        public String rg;
        public String nascimento;
        public String data_nascimento;
        private String userType;
    }
}