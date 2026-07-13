package com.ssd.agenda_SSD_back.security;

import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Ponte entre a nossa entidade User (entity/User.java) e o UserDetails que o
 * Spring Security entende. É aqui que o JwtAuthenticationFilter busca o
 * usuário do email gravado no token pra popular o contexto de segurança da
 * requisição.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // Authority no formato "ROLE_X" é a convenção que o Spring Security
        // espera pra casar com hasRole("X") nas regras de autorização.
        // ".disabled(...)" reflete o "enabled" do usuário — é o que faz um
        // usuário desativado pelo admin parar de autenticar, mesmo com token
        // ainda válido (ver JwtAuthenticationFilter).
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .disabled(!user.isEnabled())
                .build();
    }
}
