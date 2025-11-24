package dev.abumuhab.frauddetection.users;


import dev.abumuhab.frauddetection.common.BaseEntity;
import dev.abumuhab.frauddetection.users.dtos.UserDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails  {
    private String email;
    private String password;

    public  User(String email,String password){
        this.email = email;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    public UserDto toDto(){
        return  new UserDto(this.getId(),this.getEmail());
    }
}
