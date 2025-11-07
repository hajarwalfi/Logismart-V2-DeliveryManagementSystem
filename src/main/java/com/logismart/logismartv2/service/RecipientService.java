package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.recipient.RecipientCreateDTO;
import com.logismart.logismartv2.dto.recipient.RecipientResponseDTO;
import com.logismart.logismartv2.dto.recipient.RecipientUpdateDTO;
import com.logismart.logismartv2.entity.Recipient;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.RecipientMapper;
import com.logismart.logismartv2.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final RecipientMapper recipientMapper;

    public RecipientResponseDTO create(RecipientCreateDTO dto) {
        log.info("Creating new recipient with phone: {}", dto.getPhone());

        Recipient recipient = recipientMapper.toEntity(dto);
        Recipient savedRecipient = recipientRepository.save(recipient);
        log.info("Recipient created successfully with ID: {}", savedRecipient.getId());

        return recipientMapper.toResponseDTO(savedRecipient);
    }

    @Transactional(readOnly = true)
    public RecipientResponseDTO findById(String id) {
        log.info("Finding recipient by ID: {}", id);

        Recipient recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient", "id", id));

        return recipientMapper.toResponseDTO(recipient);
    }

    @Transactional(readOnly = true)
    public List<RecipientResponseDTO> findAll() {
        log.info("Finding all recipients");
        return recipientMapper.toResponseDTOList(recipientRepository.findAll());
    }

    public RecipientResponseDTO update(RecipientUpdateDTO dto) {
        log.info("Updating recipient with ID: {}", dto.getId());

        Recipient existingRecipient = recipientRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient", "id", dto.getId()));

        
        if (dto.getFirstName() != null) {
            existingRecipient.setFirstName(dto.getFirstName());
            log.info("Updated first name for recipient ID: {}", dto.getId());
        }

        
        if (dto.getLastName() != null) {
            existingRecipient.setLastName(dto.getLastName());
            log.info("Updated last name for recipient ID: {}", dto.getId());
        }

        
        if (dto.getEmail() != null) {
            existingRecipient.setEmail(dto.getEmail());
            log.info("Updated email for recipient ID: {}", dto.getId());
        }

        
        if (dto.getPhone() != null) {
            existingRecipient.setPhone(dto.getPhone());
            log.info("Updated phone for recipient ID: {}", dto.getId());
        }

        
        if (dto.getAddress() != null) {
            existingRecipient.setAddress(dto.getAddress());
            log.info("Updated address for recipient ID: {}", dto.getId());
        }

        Recipient updatedRecipient = recipientRepository.save(existingRecipient);
        log.info("Recipient updated successfully with ID: {}", updatedRecipient.getId());

        return recipientMapper.toResponseDTO(updatedRecipient);
    }

    public void delete(String id) {
        log.info("Deleting recipient with ID: {}", id);

        if (!recipientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recipient", "id", id);
        }

        recipientRepository.deleteById(id);
        log.info("Recipient deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<RecipientResponseDTO> searchByName(String keyword) {
        log.info("Searching recipients by keyword: {}", keyword);
        return recipientMapper.toResponseDTOList(recipientRepository.searchByName(keyword));
    }

    @Transactional(readOnly = true)
    public List<RecipientResponseDTO> findWithEmail() {
        log.info("Finding recipients with email");
        return recipientMapper.toResponseDTOList(recipientRepository.findRecipientsWithEmail());
    }

    @Transactional(readOnly = true)
    public List<RecipientResponseDTO> findWithoutEmail() {
        log.info("Finding recipients without email");
        return recipientMapper.toResponseDTOList(recipientRepository.findRecipientsWithoutEmail());
    }
}
