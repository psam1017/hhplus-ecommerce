package hhplus.ecommerce.server.interfaces.web.support.security.userdetails;

import hhplus.ecommerce.server.domain.user.enumeration.UserStatus;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PrincipalDetails implements UserDetails {

    private final String username;
    private final String status;
    private final String role;

    @Builder
    protected PrincipalDetails(String username, String status, String role) {
        this.username = username;
        this.status = status;
        this.role = role;
    }

    // UserDetails 시작
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authoritySet = new HashSet<>();
        authoritySet.add(() -> role);
        return authoritySet;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return null;
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        return Objects.equals(status, UserStatus.ACTIVE.key());
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 인증 정보(credentials) 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    // UserDetails 끝
}
