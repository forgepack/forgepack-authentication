package dev.forgepack.authentication.internal.model;

import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import dev.forgepack.authorization.internal.model.User;
import dev.forgepack.authorization.internal.model.Role;
import java.util.Collection;
import java.util.Set;

public class CustomUserDetails extends User implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role r : new CustomUserDetails().getRole()) {
            authorities.add(new SimpleGrantedAuthority(r.getName()));
            r.getPrivilege().forEach(p ->
                    authorities.add(new SimpleGrantedAuthority(p.getName())));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonLocked() { return new CustomUserDetails().getAttempt() == null || new CustomUserDetails().getAttempt() < 5; }

    @Override
    public boolean isEnabled() { return Boolean.TRUE.equals(new CustomUserDetails().getActive()); }
}
