package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.senderclient.SenderClientCreateDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientResponseDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientUpdateDTO;
import com.logismart.logismartv2.entity.SenderClient;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.SenderClientMapper;
import com.logismart.logismartv2.repository.SenderClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SenderClientService {

    private final SenderClientRepository senderClientRepository;
    private final SenderClientMapper senderClientMapper;

    public SenderClientResponseDTO create(SenderClientCreateDTO dto) {
        log.info("Creating new sender client with email: {}", dto.getEmail());

        
        if (senderClientRepository.existsByEmail(dto.getEmail())) {
            log.warn("Sender client creation failed: email '{}' already exists", dto.getEmail());
            throw new DuplicateResourceException("SenderClient", "email", dto.getEmail());
        }

        SenderClient client = senderClientMapper.toEntity(dto);
        SenderClient savedClient = senderClientRepository.save(client);
        log.info("Sender client created successfully with ID: {}", savedClient.getId());

        return senderClientMapper.toResponseDTO(savedClient);
    }

    @Transactional(readOnly = true)
    public SenderClientResponseDTO findById(String id) {
        log.info("Finding sender client by ID: {}", id);

        SenderClient client = senderClientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SenderClient", "id", id));

        return senderClientMapper.toResponseDTO(client);
    }

    @Transactional(readOnly = true)
    public List<SenderClientResponseDTO> findAll() {
        log.info("Finding all sender clients");
        List<SenderClient> clients = senderClientRepository.findAll();
        return senderClientMapper.toResponseDTOList(clients);
    }

    public SenderClientResponseDTO update(SenderClientUpdateDTO dto) {
        log.info("Updating sender client with ID: {}", dto.getId());

        SenderClient existingClient = senderClientRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("SenderClient", "id", dto.getId()));

        
        if (dto.getFirstName() != null) {
            existingClient.setFirstName(dto.getFirstName());
            log.info("Updated first name for client ID: {}", dto.getId());
        }

        
        if (dto.getLastName() != null) {
            existingClient.setLastName(dto.getLastName());
            log.info("Updated last name for client ID: {}", dto.getId());
        }

        
        if (dto.getEmail() != null) {
            
            if (!existingClient.getEmail().equals(dto.getEmail()) &&
                    senderClientRepository.existsByEmailAndIdNot(dto.getEmail(), dto.getId())) {
                log.warn("Sender client update failed: email '{}' already exists", dto.getEmail());
                throw new DuplicateResourceException("SenderClient", "email", dto.getEmail());
            }
            existingClient.setEmail(dto.getEmail());
            log.info("Updated email for client ID: {}", dto.getId());
        }

        
        if (dto.getPhone() != null) {
            existingClient.setPhone(dto.getPhone());
            log.info("Updated phone for client ID: {}", dto.getId());
        }

        
        if (dto.getAddress() != null) {
            existingClient.setAddress(dto.getAddress());
            log.info("Updated address for client ID: {}", dto.getId());
        }

        SenderClient updatedClient = senderClientRepository.save(existingClient);
        log.info("Sender client updated successfully with ID: {}", updatedClient.getId());

        return senderClientMapper.toResponseDTO(updatedClient);
    }

    public void delete(String id) {
        log.info("Deleting sender client with ID: {}", id);

        if (!senderClientRepository.existsById(id)) {
            throw new ResourceNotFoundException("SenderClient", "id", id);
        }

        senderClientRepository.deleteById(id);
        log.info("Sender client deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public SenderClientResponseDTO findByEmail(String email) {
        log.info("Finding sender client by email: {}", email);

        SenderClient client = senderClientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("SenderClient", "email", email));

        return senderClientMapper.toResponseDTO(client);
    }

    @Transactional(readOnly = true)
    public List<SenderClientResponseDTO> searchByName(String keyword) {
        log.info("Searching sender clients by keyword: {}", keyword);
        List<SenderClient> clients = senderClientRepository.searchByName(keyword);
        return senderClientMapper.toResponseDTOList(clients);
    }

    @Transactional(readOnly = true)
    public Long countParcels(String id) {
        log.info("Counting parcels for sender client ID: {}", id);

        if (!senderClientRepository.existsById(id)) {
            throw new ResourceNotFoundException("SenderClient", "id", id);
        }

        return senderClientRepository.countParcelsBySenderClientId(id);
    }
}
