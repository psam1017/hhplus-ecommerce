package hhplus.ecommerce.server.interfaces.user;

import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import hhplus.ecommerce.server.interfaces.user.model.request.UserLoginPost;
import hhplus.ecommerce.server.interfaces.user.model.response.UserLoginResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/open/users")
@RestController
public class OpenUserController {

    @PostMapping("/login")
    public ApiResponse<UserLoginResult> login(
            @RequestBody @Valid UserLoginPost post
    ) {
        UserLoginResult response = UserLoginResult.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();
        return ApiResponse.ok(response);
    }
}
