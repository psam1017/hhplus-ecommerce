package hhplus.ecommerce.server.infrastructure.jwt;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

@SuppressWarnings("SpellCheckingInspection")
public enum JwtStatus implements Documentable {

    BLANK("토큰없음"),
    ILLEGAL_SIGNATURE("서명불일치"),
    EXPIRED("토큰만료"),
    MALFORMED("변조됨"),
    UNSUPPORTED("미지원"),
    FORBIDDEN("기타사유"),
    ILLEGAL_IP_ACCESS("IP불일치"),
    ;

    private final String value;

    JwtStatus(String value) {
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
