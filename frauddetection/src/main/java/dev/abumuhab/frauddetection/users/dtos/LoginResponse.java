package dev.abumuhab.frauddetection.users.dtos;

public record LoginResponse(UserDto user, String token) {
}
