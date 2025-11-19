package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.recipient.RecipientCreateDTO;
import com.logismart.logismartv2.dto.recipient.RecipientResponseDTO;
import com.logismart.logismartv2.dto.recipient.RecipientUpdateDTO;
import com.logismart.logismartv2.entity.Recipient;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.RecipientMapper;
import com.logismart.logismartv2.repository.RecipientRepository;
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
 * Tests unitaires pour RecipientService
 * Service: 2/9 - Simple (CRUD + recherches)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecipientService Unit Tests")
class RecipientServiceTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientMapper recipientMapper;

    @InjectMocks
    private RecipientService recipientService;

    private Recipient recipient;
    private RecipientCreateDTO createDTO;
    private RecipientUpdateDTO updateDTO;
    private RecipientResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        recipient = new Recipient();
        recipient.setId("1");
        recipient.setFirstName("Ahmed");
        recipient.setLastName("Benali");
        recipient.setEmail("ahmed.benali@example.com");
        recipient.setPhone("0612345678");
        recipient.setAddress("123 Rue Mohammed V, Casablanca");

        createDTO = new RecipientCreateDTO();
        createDTO.setFirstName("Ahmed");
        createDTO.setLastName("Benali");
        createDTO.setEmail("ahmed.benali@example.com");
        createDTO.setPhone("0612345678");
        createDTO.setAddress("123 Rue Mohammed V, Casablanca");

        updateDTO = new RecipientUpdateDTO();
        updateDTO.setId("1");
        updateDTO.setFirstName("Hassan");
        updateDTO.setLastName("Alami");
        updateDTO.setEmail("hassan.alami@example.com");
        updateDTO.setPhone("0698765432");
        updateDTO.setAddress("456 Boulevard Zerktouni, Rabat");

        responseDTO = new RecipientResponseDTO();
        responseDTO.setId("1");
        responseDTO.setFirstName("Ahmed");
        responseDTO.setLastName("Benali");
        responseDTO.setEmail("ahmed.benali@example.com");
        responseDTO.setPhone("0612345678");
        responseDTO.setAddress("123 Rue Mohammed V, Casablanca");
    }

    // ==================== Tests pour create() ====================

    @Test
    @DisplayName("Should create recipient successfully")
    void testCreate_Success() {
        // GIVEN
        when(recipientMapper.toEntity(createDTO)).thenReturn(recipient);
        when(recipientRepository.save(recipient)).thenReturn(recipient);
        when(recipientMapper.toResponseDTO(recipient)).thenReturn(responseDTO);

        // WHEN
        RecipientResponseDTO result = recipientService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Ahmed");
        assertThat(result.getPhone()).isEqualTo("0612345678");

        verify(recipientMapper).toEntity(createDTO);
        verify(recipientRepository).save(recipient);
        verify(recipientMapper).toResponseDTO(recipient);
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find recipient by id successfully")
    void testFindById_Success() {
        // GIVEN
        when(recipientRepository.findById("1")).thenReturn(Optional.of(recipient));
        when(recipientMapper.toResponseDTO(recipient)).thenReturn(responseDTO);

        // WHEN
        RecipientResponseDTO result = recipientService.findById("1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getFirstName()).isEqualTo("Ahmed");

        verify(recipientRepository).findById("1");
        verify(recipientMapper).toResponseDTO(recipient);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when recipient not found by id")
    void testFindById_NotFound() {
        // GIVEN
        when(recipientRepository.findById("999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> recipientService.findById("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recipient")
                .hasMessageContaining("id");

        verify(recipientRepository).findById("999");
        verify(recipientMapper, never()).toResponseDTO(any());
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all recipients successfully")
    void testFindAll_Success() {
        // GIVEN
        Recipient recipient2 = new Recipient();
        recipient2.setId("2");
        recipient2.setFirstName("Fatima");

        RecipientResponseDTO responseDTO2 = new RecipientResponseDTO();
        responseDTO2.setId("2");
        responseDTO2.setFirstName("Fatima");

        List<Recipient> recipients = Arrays.asList(recipient, recipient2);
        List<RecipientResponseDTO> responseDTOs = Arrays.asList(responseDTO, responseDTO2);

        when(recipientRepository.findAll()).thenReturn(recipients);
        when(recipientMapper.toResponseDTOList(recipients)).thenReturn(responseDTOs);

        // WHEN
        List<RecipientResponseDTO> result = recipientService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("Ahmed");
        assertThat(result.get(1).getFirstName()).isEqualTo("Fatima");

        verify(recipientRepository).findAll();
        verify(recipientMapper).toResponseDTOList(recipients);
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("Should update recipient successfully with all fields")
    void testUpdate_Success_AllFields() {
        // GIVEN
        when(recipientRepository.findById(updateDTO.getId())).thenReturn(Optional.of(recipient));
        when(recipientRepository.save(recipient)).thenReturn(recipient);
        when(recipientMapper.toResponseDTO(recipient)).thenReturn(responseDTO);

        // WHEN
        RecipientResponseDTO result = recipientService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(recipientRepository).findById(updateDTO.getId());
        verify(recipientRepository).save(recipient);
    }

    @Test
    @DisplayName("Should update recipient with partial fields only")
    void testUpdate_PartialFields() {
        // GIVEN - Seulement firstName et email
        updateDTO.setFirstName("Hassan");
        updateDTO.setLastName(null);
        updateDTO.setEmail("new.email@example.com");
        updateDTO.setPhone(null);
        updateDTO.setAddress(null);

        when(recipientRepository.findById(updateDTO.getId())).thenReturn(Optional.of(recipient));
        when(recipientRepository.save(recipient)).thenReturn(recipient);
        when(recipientMapper.toResponseDTO(recipient)).thenReturn(responseDTO);

        // WHEN
        RecipientResponseDTO result = recipientService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(recipientRepository).save(recipient);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent recipient")
    void testUpdate_RecipientNotFound() {
        // GIVEN
        when(recipientRepository.findById(updateDTO.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> recipientService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recipient")
                .hasMessageContaining("id");

        verify(recipientRepository).findById(updateDTO.getId());
        verify(recipientRepository, never()).save(any());
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete recipient successfully")
    void testDelete_Success() {
        // GIVEN
        when(recipientRepository.existsById("1")).thenReturn(true);
        doNothing().when(recipientRepository).deleteById("1");

        // WHEN
        recipientService.delete("1");

        // THEN
        verify(recipientRepository).existsById("1");
        verify(recipientRepository).deleteById("1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent recipient")
    void testDelete_NotFound() {
        // GIVEN
        when(recipientRepository.existsById("999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> recipientService.delete("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recipient")
                .hasMessageContaining("id");

        verify(recipientRepository).existsById("999");
        verify(recipientRepository, never()).deleteById(anyString());
    }

    // ==================== Tests pour searchByName() ====================

    @Test
    @DisplayName("Should search recipients by name keyword successfully")
    void testSearchByName_Success() {
        // GIVEN
        List<Recipient> recipients = Arrays.asList(recipient);
        List<RecipientResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(recipientRepository.searchByName("ahmed")).thenReturn(recipients);
        when(recipientMapper.toResponseDTOList(recipients)).thenReturn(responseDTOs);

        // WHEN
        List<RecipientResponseDTO> result = recipientService.searchByName("ahmed");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Ahmed");

        verify(recipientRepository).searchByName("ahmed");
        verify(recipientMapper).toResponseDTOList(recipients);
    }

    // ==================== Tests pour findWithEmail() ====================

    @Test
    @DisplayName("Should find recipients with email successfully")
    void testFindWithEmail_Success() {
        // GIVEN
        List<Recipient> recipients = Arrays.asList(recipient);
        List<RecipientResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(recipientRepository.findRecipientsWithEmail()).thenReturn(recipients);
        when(recipientMapper.toResponseDTOList(recipients)).thenReturn(responseDTOs);

        // WHEN
        List<RecipientResponseDTO> result = recipientService.findWithEmail();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isNotNull();

        verify(recipientRepository).findRecipientsWithEmail();
        verify(recipientMapper).toResponseDTOList(recipients);
    }

    // ==================== Tests pour findWithoutEmail() ====================

    @Test
    @DisplayName("Should find recipients without email successfully")
    void testFindWithoutEmail_Success() {
        // GIVEN
        Recipient recipientNoEmail = new Recipient();
        recipientNoEmail.setId("3");
        recipientNoEmail.setFirstName("Omar");
        recipientNoEmail.setEmail(null);

        RecipientResponseDTO responseDTONoEmail = new RecipientResponseDTO();
        responseDTONoEmail.setId("3");
        responseDTONoEmail.setFirstName("Omar");
        responseDTONoEmail.setEmail(null);

        List<Recipient> recipients = Arrays.asList(recipientNoEmail);
        List<RecipientResponseDTO> responseDTOs = Arrays.asList(responseDTONoEmail);

        when(recipientRepository.findRecipientsWithoutEmail()).thenReturn(recipients);
        when(recipientMapper.toResponseDTOList(recipients)).thenReturn(responseDTOs);

        // WHEN
        List<RecipientResponseDTO> result = recipientService.findWithoutEmail();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(recipientRepository).findRecipientsWithoutEmail();
        verify(recipientMapper).toResponseDTOList(recipients);
    }
}
