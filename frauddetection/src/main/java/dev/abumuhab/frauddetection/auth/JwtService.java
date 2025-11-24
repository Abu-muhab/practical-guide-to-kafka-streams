package dev.abumuhab.frauddetection.auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    SecretKey getJwtKey(){
        String jwtKey = "i solemnly swear that i am up to no good";
        return Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

   public String validateAuthToken(String token){
        try{
            final Claims claims = Jwts.parser()
                    .verifyWith(getJwtKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();


            if(claims.getExpiration().before(new Date())){
                return  null;
            }

            return claims.getSubject();
        }catch (Exception e){
            System.out.println("Error validating token: "+e.getMessage());
            return  null;
        }
    }

    public String generateAuthToken(String userId){
        return Jwts.builder()
                .subject(userId)
                .expiration(new Date(System.currentTimeMillis()+ 30 * 60 * 1000))
                .signWith(getJwtKey())
                .compact();
    }
}
