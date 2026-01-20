package com.logismart.logismartv2.listener;

import com.logismart.logismartv2.entity.SenderClient;
import com.logismart.logismartv2.repository.SenderClientRepository;
import com.logismart.security.entity.User;
import com.logismart.security.event.ClientUserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener for ClientUserRegisteredEvent.
 * Creates a SenderClient entity when a new user with CLIENT role registers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClientUserRegisteredEventListener {

    private final SenderClientRepository senderClientRepository;

    @EventListener
    @Transactional
    public void handleClientUserRegistered(ClientUserRegisteredEvent event) {
        User user = event.getUser();
        log.info("Handling ClientUserRegisteredEvent for user: {} (ID: {})", user.getUsername(), user.getId());

        // Check if SenderClient already exists for this user
        if (senderClientRepository.findByUserId(user.getId()).isPresent()) {
            log.warn("SenderClient already exists for user ID: {}", user.getId());
            return;
        }

        // Create a new SenderClient linked to the user
        SenderClient senderClient = new SenderClient();
        senderClient.setUserId(user.getId());
        senderClient.setFirstName(user.getFirstName() != null ? user.getFirstName() : user.getUsername());
        senderClient.setLastName(user.getLastName() != null ? user.getLastName() : "Client");
        senderClient.setEmail(user.getEmail());
        senderClient.setPhone(null); // User can update this later
        senderClient.setAddress(null); // User can update this later

        SenderClient savedSenderClient = senderClientRepository.save(senderClient);
        log.info("Created SenderClient (ID: {}) for user: {} (ID: {})",
                savedSenderClient.getId(), user.getUsername(), user.getId());
    }
}
