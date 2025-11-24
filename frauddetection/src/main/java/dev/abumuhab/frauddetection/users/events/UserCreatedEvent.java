package dev.abumuhab.frauddetection.users.events;

import dev.abumuhab.frauddetection.common.DomainEvent;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class UserCreatedEvent extends DomainEvent {
    private String email;
    private String ipAddress;
}
