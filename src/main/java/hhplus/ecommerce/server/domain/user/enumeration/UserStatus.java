package hhplus.ecommerce.server.domain.user.enumeration;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

import java.util.Objects;

public enum UserStatus implements Documentable {

    PENDING("가입대기"),
    ACTIVE("정상"),
    WITHDRAWN("탈퇴"),
    ;

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    @Override
    public String key() {
        return this.name();
    }

    @Override
    public String value() {
        return this.value;
    }

    public static UserStatus ofNullable(String str) {
        for (UserStatus e : UserStatus.values()) {
            if (Objects.equals(str, e.key()) || Objects.equals(str, e.value())) {
                return e;
            }
        }
        return null;
    }
}
