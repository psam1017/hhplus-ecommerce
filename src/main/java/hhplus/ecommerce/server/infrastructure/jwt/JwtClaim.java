package hhplus.ecommerce.server.infrastructure.jwt;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

@SuppressWarnings("SpellCheckingInspection")
public enum JwtClaim implements Documentable {

    USERNAME("로그인아이디"),
    USER_STATUS("사용자상태"),
    USER_ROLE("권한이름"),
    REMOTE_IP("IP주소"),
    ;

    private final String value;

    JwtClaim(String value) {
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
}
