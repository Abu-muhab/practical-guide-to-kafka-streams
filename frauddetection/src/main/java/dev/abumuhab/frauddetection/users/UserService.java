package dev.abumuhab.frauddetection.users;

import dev.abumuhab.frauddetection.auth.JwtService;
import dev.abumuhab.frauddetection.users.dtos.LoginResponse;
import dev.abumuhab.frauddetection.users.events.UserCreatedEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService implements UserDetailsService {
    final private UserRepository userRepository;
    final private JwtService jwtService;
    final private PasswordEncoder passwordEncoder;
    @Autowired
    public  UserService(UserRepository userRepository,JwtService jwtService,PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Optional<User> user = this.userRepository.findById(username);

        if(user.isEmpty()){
            throw new UsernameNotFoundException("User "+username+" not found");
        }

        return user.get();
    }


    public User createUser(String email, String password, HttpServletRequest servletRequest){
        User existingUser = this.userRepository.findByEmail(email);
        if(existingUser!=null){
            throw new RuntimeException("User with email "+email+" exists");
        }

        User newUser = new User(email,this.passwordEncoder.encode(password));
        newUser.addEvent(new UserCreatedEvent(newUser.getEmail(),getClientIpAddress(servletRequest)));
        newUser = this.userRepository.save(newUser);
        return newUser;
    }

    public LoginResponse login(String email, String password){
        User user = this.userRepository.findByEmail(email);
        if(user==null || !this.passwordEncoder.matches(password,user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }

        return new LoginResponse(user.toDto(),this.jwtService.generateAuthToken(user.getId()));
    }


    public String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
