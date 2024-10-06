package hhplus.ecommerce.server.domain.user.enumeration;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

import java.util.Objects;

public enum UserRole implements Documentable {

    ROLE_ADMIN("관리자"),
    ROLE_MEMBER("일반회원"),
    ;

    private final String value;

    UserRole(String value) {
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

    public static UserRole ofNullable(String str) {
        for (UserRole e : UserRole.values()) {
            if (Objects.equals(str, e.key()) || Objects.equals(str, e.value())) {
                return e;
            }
        }
        return null;
    }
}
