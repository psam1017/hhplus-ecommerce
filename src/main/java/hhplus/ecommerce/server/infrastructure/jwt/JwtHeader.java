package hhplus.ecommerce.server.infrastructure.jwt;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

public enum JwtHeader implements Documentable {

    X_REFRESH_TOKEN("X-Refresh-Token"),
    X_ACCESS_RENEWAL("X-Access-Renewal"),
    X_REFRESH_RENEWAL("X-Refresh-Renewal"),
    ;

    private final String value;

    JwtHeader(String value) {
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
