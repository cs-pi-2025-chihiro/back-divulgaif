package br.com.divulgaifback.modules.auth.useCases.resetPassword;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {
    private String message;

    public ResetPasswordResponse toPresentation(String message) {
        return new ResetPasswordResponse(message);
    }
}

