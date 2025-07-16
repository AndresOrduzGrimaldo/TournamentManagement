package com.tournament.infrastructure.security;

import com.tournament.domain.repository.UserRepository;
import com.tournament.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT que intercepta las peticiones HTTP
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.extractUsername(jwt);
                Long userId = jwtTokenProvider.extractUserId(jwt);
                String userRole = jwtTokenProvider.extractUserRole(jwt);

                // Buscar usuario en la base de datos
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

                // Verificar que el usuario esté activo
                if (!user.getIsActive()) {
                    log.warn("Usuario inactivo intentando acceder: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Crear UserDetails
                UserDetails userDetails = createUserDetails(user);

                // Crear autenticación
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole))
                    );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer autenticación en el contexto
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Usuario autenticado: {} con rol: {}", username, userRole);
            }
        } catch (Exception e) {
            log.error("Error procesando token JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization
     * @param request Petición HTTP
     * @return Token JWT o null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * Crea UserDetails a partir de la entidad User
     * @param user Entidad User
     * @return UserDetails
     */
    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
} 