package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.senderclient.SenderClientCreateDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientResponseDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientUpdateDTO;
import com.logismart.logismartv2.entity.SenderClient;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.SenderClientMapper;
import com.logismart.logismartv2.repository.SenderClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SenderClientService
 * Service: 3/9 - Simple (CRUD + validation email unique + comptage)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SenderClientService Unit Tests")
class SenderClientServiceTest {

    @Mock
    private SenderClientRepository senderClientRepository;

    @Mock
    private SenderClientMapper senderClientMapper;

    @InjectMocks
    private SenderClientService senderClientService;

    private SenderClient client;
    private SenderClientCreateDTO createDTO;
    private SenderClientUpdateDTO updateDTO;
    private SenderClientResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        client = new SenderClient();
        client.setId("1");
        client.setFirstName("Youssef");
        client.setLastName("Mansouri");
        client.setEmail("youssef.mansouri@company.ma");
        client.setPhone("0522334455");
        client.setAddress("50 Avenue Hassan II, Casablanca");

        createDTO = new SenderClientCreateDTO();
        createDTO.setFirstName("Youssef");
        createDTO.setLastName("Mansouri");
        createDTO.setEmail("youssef.mansouri@company.ma");
        createDTO.setPhone("0522334455");
        createDTO.setAddress("50 Avenue Hassan II, Casablanca");

        updateDTO = new SenderClientUpdateDTO();
        updateDTO.setId("1");
        updateDTO.setFirstName("Karim");
        updateDTO.setLastName("Benjelloun");
        updateDTO.setEmail("karim.benjelloun@company.ma");
        updateDTO.setPhone("0522998877");
        updateDTO.setAddress("100 Boulevard Mohammed VI, Rabat");

        responseDTO = new SenderClientResponseDTO();
        responseDTO.setId("1");
        responseDTO.setFirstName("Youssef");
        responseDTO.setLastName("Mansouri");
        responseDTO.setEmail("youssef.mansouri@company.ma");
        responseDTO.setPhone("0522334455");
        responseDTO.setAddress("50 Avenue Hassan II, Casablanca");
    }

    // ==================== Tests pour create() ====================

    @Test
    @DisplayName("Should create sender client successfully when email is unique")
    void testCreate_Success() {
        // GIVEN
        when(senderClientRepository.existsByEmail(createDTO.getEmail())).thenReturn(false);
        when(senderClientMapper.toEntity(createDTO)).thenReturn(client);
        when(senderClientRepository.save(client)).thenReturn(client);
        when(senderClientMapper.toResponseDTO(client)).thenReturn(responseDTO);

        // WHEN
        SenderClientResponseDTO result = senderClientService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("youssef.mansouri@company.ma");

        verify(senderClientRepository).existsByEmail(createDTO.getEmail());
        verify(senderClientMapper).toEntity(createDTO);
        verify(senderClientRepository).save(client);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating client with existing email")
    void testCreate_DuplicateEmail() {
        // GIVEN
        when(senderClientRepository.existsByEmail(createDTO.getEmail())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> senderClientService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SenderClient")
                .hasMessageContaining("email");

        verify(senderClientRepository).existsByEmail(createDTO.getEmail());
        verify(senderClientRepository, never()).save(any());
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find sender client by id successfully")
    void testFindById_Success() {
        // GIVEN
        when(senderClientRepository.findById("1")).thenReturn(Optional.of(client));
        when(senderClientMapper.toResponseDTO(client)).thenReturn(responseDTO);

        // WHEN
        SenderClientResponseDTO result = senderClientService.findById("1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");

        verify(senderClientRepository).findById("1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client not found")
    void testFindById_NotFound() {
        // GIVEN
        when(senderClientRepository.findById("999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> senderClientService.findById("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SenderClient");

        verify(senderClientRepository).findById("999");
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all sender clients successfully")
    void testFindAll_Success() {
        // GIVEN
        List<SenderClient> clients = Arrays.asList(client);
        List<SenderClientResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(senderClientRepository.findAll()).thenReturn(clients);
        when(senderClientMapper.toResponseDTOList(clients)).thenReturn(responseDTOs);

        // WHEN
        List<SenderClientResponseDTO> result = senderClientService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(senderClientRepository).findAll();
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("Should update sender client successfully")
    void testUpdate_Success() {
        // GIVEN
        client.setEmail("old@example.com");
        updateDTO.setEmail("new@example.com");

        when(senderClientRepository.findById(updateDTO.getId())).thenReturn(Optional.of(client));
        when(senderClientRepository.existsByEmailAndIdNot("new@example.com", "1")).thenReturn(false);
        when(senderClientRepository.save(client)).thenReturn(client);
        when(senderClientMapper.toResponseDTO(client)).thenReturn(responseDTO);

        // WHEN
        SenderClientResponseDTO result = senderClientService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(senderClientRepository).findById(updateDTO.getId());
        verify(senderClientRepository).save(client);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating with existing email")
    void testUpdate_DuplicateEmail() {
        // GIVEN
        client.setEmail("old@example.com");
        updateDTO.setEmail("existing@example.com");

        when(senderClientRepository.findById(updateDTO.getId())).thenReturn(Optional.of(client));
        when(senderClientRepository.existsByEmailAndIdNot("existing@example.com", "1")).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> senderClientService.update(updateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SenderClient")
                .hasMessageContaining("email");

        verify(senderClientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent client")
    void testUpdate_ClientNotFound() {
        // GIVEN
        when(senderClientRepository.findById(updateDTO.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> senderClientService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(senderClientRepository).findById(updateDTO.getId());
        verify(senderClientRepository, never()).save(any());
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete sender client successfully")
    void testDelete_Success() {
        // GIVEN
        when(senderClientRepository.existsById("1")).thenReturn(true);
        doNothing().when(senderClientRepository).deleteById("1");

        // WHEN
        senderClientService.delete("1");

        // THEN
        verify(senderClientRepository).existsById("1");
        verify(senderClientRepository).deleteById("1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent client")
    void testDelete_NotFound() {
        // GIVEN
        when(senderClientRepository.existsById("999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> senderClientService.delete("999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(senderClientRepository, never()).deleteById(anyString());
    }

    // ==================== Tests pour findByEmail() ====================

    @Test
    @DisplayName("Should find sender client by email successfully")
    void testFindByEmail_Success() {
        // GIVEN
        when(senderClientRepository.findByEmail("youssef.mansouri@company.ma"))
                .thenReturn(Optional.of(client));
        when(senderClientMapper.toResponseDTO(client)).thenReturn(responseDTO);

        // WHEN
        SenderClientResponseDTO result = senderClientService.findByEmail("youssef.mansouri@company.ma");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("youssef.mansouri@company.ma");

        verify(senderClientRepository).findByEmail("youssef.mansouri@company.ma");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when email not found")
    void testFindByEmail_NotFound() {
        // GIVEN
        when(senderClientRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> senderClientService.findByEmail("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("email");

        verify(senderClientRepository).findByEmail("nonexistent@example.com");
    }

    // ==================== Tests pour searchByName() ====================

    @Test
    @DisplayName("Should search sender clients by name successfully")
    void testSearchByName_Success() {
        // GIVEN
        List<SenderClient> clients = Arrays.asList(client);
        List<SenderClientResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(senderClientRepository.searchByName("youssef")).thenReturn(clients);
        when(senderClientMapper.toResponseDTOList(clients)).thenReturn(responseDTOs);

        // WHEN
        List<SenderClientResponseDTO> result = senderClientService.searchByName("youssef");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(senderClientRepository).searchByName("youssef");
    }

    // ==================== Tests pour countParcels() ====================

    @Test
    @DisplayName("Should count parcels for sender client successfully")
    void testCountParcels_Success() {
        // GIVEN
        when(senderClientRepository.existsById("1")).thenReturn(true);
        when(senderClientRepository.countParcelsBySenderClientId("1")).thenReturn(5L);

        // WHEN
        Long result = senderClientService.countParcels("1");

        // THEN
        assertThat(result).isEqualTo(5L);

        verify(senderClientRepository).existsById("1");
        verify(senderClientRepository).countParcelsBySenderClientId("1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when counting parcels for non-existent client")
    void testCountParcels_ClientNotFound() {
        // GIVEN
        when(senderClientRepository.existsById("999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> senderClientService.countParcels("999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(senderClientRepository).existsById("999");
        verify(senderClientRepository, never()).countParcelsBySenderClientId(anyString());
    }
}
