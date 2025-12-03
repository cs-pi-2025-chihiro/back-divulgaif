package br.com.divulgaifback.modules.auth.useCases.forgotPassword;

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
public class ForgotPasswordResponse {
    private String message;

    public ForgotPasswordResponse toPresentation(String message) {
        return new ForgotPasswordResponse(message);
    }
}

