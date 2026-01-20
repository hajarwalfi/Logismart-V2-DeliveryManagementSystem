package com.logismart.security.event;

import com.logismart.security.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new user with CLIENT role is registered.
 * This event is used to trigger creation of associated entities like SenderClient.
 */
@Getter
public class ClientUserRegisteredEvent extends ApplicationEvent {

    private final User user;

    public ClientUserRegisteredEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
