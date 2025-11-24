package dev.abumuhab.frauddetection.users;


import dev.abumuhab.frauddetection.users.dtos.CreateUserRequest;
import dev.abumuhab.frauddetection.users.dtos.LoginRequest;
import dev.abumuhab.frauddetection.users.dtos.LoginResponse;
import dev.abumuhab.frauddetection.users.dtos.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users/me")
    public UserDto me(@AuthenticationPrincipal() UserDetails userDetails){
        return  ((User) userDetails).toDto();
    }

    @PostMapping("/auth/signup")
    public LoginResponse createUser(HttpServletRequest servletRequest, @RequestBody() CreateUserRequest request){
        if(request.email()==null || request.password() ==null){
            throw new RuntimeException("Missing required field(s)");
        }

        User user =  this.userService.createUser(request.email(),request.password(),servletRequest);
        if(user!=null){
            return this.userService.login(request.email(),request.password());
        }

        throw new RuntimeException("Error setting up your account");
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody LoginRequest request){
       return  this.userService.login(request.email(),request.password());
    }
}
