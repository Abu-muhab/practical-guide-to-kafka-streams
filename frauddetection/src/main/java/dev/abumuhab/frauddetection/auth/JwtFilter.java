package dev.abumuhab.frauddetection.auth;

import dev.abumuhab.frauddetection.users.User;
import dev.abumuhab.frauddetection.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       String authHeader =  request.getHeader("Authorization");
       String token = authHeader!=null? authHeader.split(" ")[1].trim():null;

       String userId =this.jwtService.validateAuthToken(token);
       User user = null;

        if(userId!=null){
            Optional<User> userOptional = userRepository.findById(userId);
            if(userOptional.isPresent()){
                user = userOptional.get();
            }
        }

        if( user!=null){
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

       filterChain.doFilter(request,response);
    }
}
