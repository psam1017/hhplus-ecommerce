package hhplus.ecommerce.server.interfaces.web.user.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginResult {

    private String accessToken;
    private String refreshToken;
    private String type;

    @Builder
    protected UserLoginResult(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = "Bearer";
    }
}
