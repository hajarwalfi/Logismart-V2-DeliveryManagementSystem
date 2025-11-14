package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryCreateDTO;
import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.entity.DeliveryHistory;
import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.DeliveryHistoryMapper;
import com.logismart.logismartv2.repository.DeliveryHistoryRepository;
import com.logismart.logismartv2.repository.ParcelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DeliveryHistoryService
 * Service: 4/9 - Simple (Historique de livraison / Traçabilité)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryHistoryService Unit Tests")
class DeliveryHistoryServiceTest {

    @Mock
    private DeliveryHistoryRepository deliveryHistoryRepository;

    @Mock
    private DeliveryHistoryMapper deliveryHistoryMapper;

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private DeliveryHistoryService deliveryHistoryService;

    private DeliveryHistory history;
    private DeliveryHistoryCreateDTO createDTO;
    private DeliveryHistoryResponseDTO responseDTO;
    private Parcel parcel;

    @BeforeEach
    void setUp() {
        parcel = new Parcel();
        parcel.setId("parcel-1");

        history = new DeliveryHistory();
        history.setId("history-1");
        history.setParcel(parcel);
        history.setStatus(ParcelStatus.IN_TRANSIT);
        history.setComment("Package dispatched from warehouse");
        history.setChangedAt(LocalDateTime.now());

        createDTO = new DeliveryHistoryCreateDTO();
        createDTO.setParcelId("parcel-1");
        createDTO.setStatus(ParcelStatus.IN_TRANSIT);
        createDTO.setComment("Package dispatched from warehouse");

        responseDTO = new DeliveryHistoryResponseDTO();
        responseDTO.setId("history-1");
        responseDTO.setParcelId("parcel-1");
        responseDTO.setStatus(ParcelStatus.IN_TRANSIT);
        responseDTO.setComment("Package dispatched from warehouse");
    }

    // ==================== Tests pour create() ====================

    @Test
    @DisplayName("Should create delivery history successfully when parcel exists")
    void testCreate_Success() {
        // GIVEN
        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(deliveryHistoryMapper.toEntity(createDTO)).thenReturn(history);
        when(deliveryHistoryRepository.save(history)).thenReturn(history);
        when(deliveryHistoryMapper.toResponseDTO(history)).thenReturn(responseDTO);

        // WHEN
        DeliveryHistoryResponseDTO result = deliveryHistoryService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getParcelId()).isEqualTo("parcel-1");
        assertThat(result.getStatus()).isEqualTo(ParcelStatus.IN_TRANSIT);

        verify(parcelRepository).findById("parcel-1");
        verify(deliveryHistoryMapper).toEntity(createDTO);
        verify(deliveryHistoryRepository).save(history);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating history for non-existent parcel")
    void testCreate_ParcelNotFound() {
        // GIVEN
        when(parcelRepository.findById("parcel-999")).thenReturn(Optional.empty());
        createDTO.setParcelId("parcel-999");

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryHistoryService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");

        verify(parcelRepository).findById("parcel-999");
        verify(deliveryHistoryRepository, never()).save(any());
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find delivery history by id successfully")
    void testFindById_Success() {
        // GIVEN
        when(deliveryHistoryRepository.findById("history-1")).thenReturn(Optional.of(history));
        when(deliveryHistoryMapper.toResponseDTO(history)).thenReturn(responseDTO);

        // WHEN
        DeliveryHistoryResponseDTO result = deliveryHistoryService.findById("history-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("history-1");

        verify(deliveryHistoryRepository).findById("history-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when history not found")
    void testFindById_NotFound() {
        // GIVEN
        when(deliveryHistoryRepository.findById("history-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryHistoryService.findById("history-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DeliveryHistory");

        verify(deliveryHistoryRepository).findById("history-999");
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all delivery histories successfully")
    void testFindAll_Success() {
        // GIVEN
        List<DeliveryHistory> histories = Arrays.asList(history);
        List<DeliveryHistoryResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(deliveryHistoryRepository.findAll()).thenReturn(histories);
        when(deliveryHistoryMapper.toResponseDTOList(histories)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryHistoryResponseDTO> result = deliveryHistoryService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(deliveryHistoryRepository).findAll();
    }

    // ==================== Tests pour findByParcelId() ====================

    @Test
    @DisplayName("Should find delivery histories by parcel id successfully")
    void testFindByParcelId_Success() {
        // GIVEN
        List<DeliveryHistory> histories = Arrays.asList(history);
        List<DeliveryHistoryResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(deliveryHistoryRepository.findByParcelIdOrderByChangedAtAsc("parcel-1"))
                .thenReturn(histories);
        when(deliveryHistoryMapper.toResponseDTOList(histories)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryHistoryResponseDTO> result = deliveryHistoryService.findByParcelId("parcel-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(parcelRepository).existsById("parcel-1");
        verify(deliveryHistoryRepository).findByParcelIdOrderByChangedAtAsc("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding histories for non-existent parcel")
    void testFindByParcelId_ParcelNotFound() {
        // GIVEN
        when(parcelRepository.existsById("parcel-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryHistoryService.findByParcelId("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");

        verify(parcelRepository).existsById("parcel-999");
        verify(deliveryHistoryRepository, never()).findByParcelIdOrderByChangedAtAsc(anyString());
    }

    // ==================== Tests pour findLatestByParcelId() ====================

    @Test
    @DisplayName("Should find latest delivery history for parcel successfully")
    void testFindLatestByParcelId_Success() {
        // GIVEN
        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(deliveryHistoryRepository.findLatestByParcelId("parcel-1"))
                .thenReturn(Optional.of(history));
        when(deliveryHistoryMapper.toResponseDTO(history)).thenReturn(responseDTO);

        // WHEN
        DeliveryHistoryResponseDTO result = deliveryHistoryService.findLatestByParcelId("parcel-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getParcelId()).isEqualTo("parcel-1");

        verify(parcelRepository).existsById("parcel-1");
        verify(deliveryHistoryRepository).findLatestByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no history exists for parcel")
    void testFindLatestByParcelId_NoHistory() {
        // GIVEN
        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(deliveryHistoryRepository.findLatestByParcelId("parcel-1"))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryHistoryService.findLatestByParcelId("parcel-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No delivery history found");

        verify(deliveryHistoryRepository).findLatestByParcelId("parcel-1");
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete delivery history successfully")
    void testDelete_Success() {
        // GIVEN
        when(deliveryHistoryRepository.existsById("history-1")).thenReturn(true);
        doNothing().when(deliveryHistoryRepository).deleteById("history-1");

        // WHEN
        deliveryHistoryService.delete("history-1");

        // THEN
        verify(deliveryHistoryRepository).existsById("history-1");
        verify(deliveryHistoryRepository).deleteById("history-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent history")
    void testDelete_NotFound() {
        // GIVEN
        when(deliveryHistoryRepository.existsById("history-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryHistoryService.delete("history-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(deliveryHistoryRepository, never()).deleteById(anyString());
    }

    // ==================== Tests pour countByParcelId() ====================

    @Test
    @DisplayName("Should count delivery histories for parcel successfully")
    void testCountByParcelId_Success() {
        // GIVEN
        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(deliveryHistoryRepository.countByParcelId("parcel-1")).thenReturn(3L);

        // WHEN
        Long result = deliveryHistoryService.countByParcelId("parcel-1");

        // THEN
        assertThat(result).isEqualTo(3L);

        verify(parcelRepository).existsById("parcel-1");
        verify(deliveryHistoryRepository).countByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when counting for non-existent parcel")
    void testCountByParcelId_ParcelNotFound() {
        // GIVEN
        when(parcelRepository.existsById("parcel-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryHistoryService.countByParcelId("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(parcelRepository).existsById("parcel-999");
        verify(deliveryHistoryRepository, never()).countByParcelId(anyString());
    }

    // ==================== Tests pour findEntriesWithComments() ====================

    @Test
    @DisplayName("Should find delivery histories with comments successfully")
    void testFindEntriesWithComments_Success() {
        // GIVEN
        List<DeliveryHistory> histories = Arrays.asList(history);
        List<DeliveryHistoryResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(deliveryHistoryRepository.findEntriesWithComments()).thenReturn(histories);
        when(deliveryHistoryMapper.toResponseDTOList(histories)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryHistoryResponseDTO> result = deliveryHistoryService.findEntriesWithComments();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(deliveryHistoryRepository).findEntriesWithComments();
    }

    // ==================== Tests pour countDeliveriesToday() ====================

    @Test
    @DisplayName("Should count deliveries today successfully")
    void testCountDeliveriesToday_Success() {
        // GIVEN
        when(deliveryHistoryRepository.countDeliveriesToday()).thenReturn(15L);

        // WHEN
        Long result = deliveryHistoryService.countDeliveriesToday();

        // THEN
        assertThat(result).isEqualTo(15L);

        verify(deliveryHistoryRepository).countDeliveriesToday();
    }
}
