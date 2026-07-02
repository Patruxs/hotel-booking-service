package org.example.hotelbookingservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource("email", username);
        List<AccountAuthUser> accounts = jdbcTemplate.query("""
                        select id, email, password_hash, email_verified
                        from accounts
                        where lower(email) = lower(:email)
                        """,
                params,
                (rs, rowNum) -> AccountAuthUser.builder()
                        .accountId((UUID) rs.getObject("id"))
                        .email(rs.getString("email"))
                        .passwordHash(rs.getString("password_hash"))
                        .emailVerified(rs.getBoolean("email_verified"))
                        .authorities(loadAuthorities((UUID) rs.getObject("id")))
                        .build());

        if (accounts.isEmpty()) {
            throw new UsernameNotFoundException("Account not found");
        }

        return accounts.getFirst();
    }

    private List<SimpleGrantedAuthority> loadAuthorities(UUID accountId) {
        return jdbcTemplate.query("""
                        select r.name
                        from account_roles ar
                        join roles r on r.id = ar.role_id
                        where ar.account_id = :accountId
                        order by r.name
                        """,
                new MapSqlParameterSource("accountId", accountId),
                (rs, rowNum) -> new SimpleGrantedAuthority(rs.getString("name")));
    }
}
