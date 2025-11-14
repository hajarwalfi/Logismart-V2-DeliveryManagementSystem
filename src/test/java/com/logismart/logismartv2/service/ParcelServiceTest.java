package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelCreateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelProductItemDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelUpdateDTO;
import com.logismart.logismartv2.entity.*;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.DeliveryHistoryMapper;
import com.logismart.logismartv2.mapper.ParcelMapper;
import com.logismart.logismartv2.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ParcelService
 * Service: 9/9 - Très Complexe (Gestion complète des colis avec produits et historique)
 * Méthodes testées: 27 méthodes publiques (CRUD + recherches + statistiques + historique)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ParcelService Unit Tests")
class ParcelServiceTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapper parcelMapper;

    @Mock
    private SenderClientRepository senderClientRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DeliveryPersonRepository deliveryPersonRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ParcelProductRepository parcelProductRepository;

    @Mock
    private DeliveryHistoryRepository deliveryHistoryRepository;

    @Mock
    private DeliveryHistoryMapper deliveryHistoryMapper;

    @InjectMocks
    private ParcelService parcelService;

    private Parcel parcel;
    private ParcelCreateDTO createDTO;
    private ParcelUpdateDTO updateDTO;
    private ParcelResponseDTO responseDTO;
    private SenderClient sender;
    private Recipient recipient;
    private Product product;
    private DeliveryPerson deliveryPerson;
    private Zone zone;

    @BeforeEach
    void setUp() {
        sender = new SenderClient();
        sender.setId("sender-1");

        recipient = new Recipient();
        recipient.setId("recipient-1");

        product = new Product();
        product.setId("product-1");

        zone = new Zone();
        zone.setId("zone-1");

        deliveryPerson = new DeliveryPerson();
        deliveryPerson.setId("dp-1");

        parcel = new Parcel();
        parcel.setId("parcel-1");
        parcel.setStatus(ParcelStatus.CREATED);
        parcel.setPriority(ParcelPriority.NORMAL);
        parcel.setSenderClient(sender);
        parcel.setRecipient(recipient);
        parcel.setWeight(new BigDecimal("2.5"));
        parcel.setDestinationCity("Casablanca");

        ParcelProductItemDTO productItem = new ParcelProductItemDTO();
        productItem.setProductId("product-1");
        productItem.setQuantity(2);
        productItem.setPrice(new BigDecimal("100.00"));

        createDTO = new ParcelCreateDTO();
        createDTO.setSenderClientId("sender-1");
        createDTO.setRecipientId("recipient-1");
        createDTO.setProducts(Arrays.asList(productItem));
        createDTO.setWeight(new BigDecimal("2.5"));

        updateDTO = new ParcelUpdateDTO();
        updateDTO.setId("parcel-1");
        updateDTO.setStatus(ParcelStatus.IN_TRANSIT);

        responseDTO = new ParcelResponseDTO();
        responseDTO.setId("parcel-1");
    }

    // ==================== Tests pour create() ====================

    @Test
    @DisplayName("Should create parcel with products and history successfully")
    void testCreate_Success() {
        // GIVEN
        when(senderClientRepository.findById("sender-1")).thenReturn(Optional.of(sender));
        when(recipientRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        when(productRepository.existsById("product-1")).thenReturn(true);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(parcelMapper.toEntity(createDTO)).thenReturn(parcel);
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);
        when(parcelProductRepository.save(any(ParcelProduct.class))).thenReturn(new ParcelProduct());
        when(deliveryHistoryRepository.save(any(DeliveryHistory.class))).thenReturn(new DeliveryHistory());

        // WHEN
        ParcelResponseDTO result = parcelService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("parcel-1");

        verify(senderClientRepository).findById("sender-1");
        verify(recipientRepository).findById("recipient-1");
        verify(parcelRepository).save(parcel);
        verify(parcelProductRepository).save(any(ParcelProduct.class));
        verify(deliveryHistoryRepository).save(any(DeliveryHistory.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating with non-existent sender")
    void testCreate_SenderNotFound() {
        // GIVEN
        when(senderClientRepository.findById("sender-999")).thenReturn(Optional.empty());
        createDTO.setSenderClientId("sender-999");

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SenderClient");

        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating with non-existent recipient")
    void testCreate_RecipientNotFound() {
        // GIVEN
        when(senderClientRepository.findById("sender-1")).thenReturn(Optional.of(sender));
        when(recipientRepository.findById("recipient-999")).thenReturn(Optional.empty());
        createDTO.setRecipientId("recipient-999");

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recipient");

        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating with non-existent product")
    void testCreate_ProductNotFound() {
        // GIVEN
        when(senderClientRepository.findById("sender-1")).thenReturn(Optional.of(sender));
        when(recipientRepository.findById("recipient-1")).thenReturn(Optional.of(recipient));
        when(productRepository.existsById("product-1")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");

        verify(parcelRepository, never()).save(any());
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find parcel by id successfully")
    void testFindById_Success() {
        // GIVEN
        when(parcelRepository.findByIdWithRelationships("parcel-1")).thenReturn(Optional.of(parcel));
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);

        // WHEN
        ParcelResponseDTO result = parcelService.findById("parcel-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when parcel not found")
    void testFindById_NotFound() {
        // GIVEN
        when(parcelRepository.findByIdWithRelationships("parcel-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.findById("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all parcels successfully")
    void testFindAll_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.findAll()).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findAll();

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should return paginated parcels successfully")
    void testFindAllPaginated_Success() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<Parcel> parcelPage = new PageImpl<>(Arrays.asList(parcel));

        when(parcelRepository.findAll(pageable)).thenReturn(parcelPage);
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);

        // WHEN
        Page<ParcelResponseDTO> result = parcelService.findAll(pageable);

        // THEN
        assertThat(result.getContent()).hasSize(1);
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("Should update parcel and create history entry when status changes")
    void testUpdate_StatusChange() {
        // GIVEN
        parcel.setStatus(ParcelStatus.CREATED);
        updateDTO.setStatus(ParcelStatus.IN_TRANSIT);

        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(parcel)).thenReturn(parcel);
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);
        when(deliveryHistoryRepository.save(any(DeliveryHistory.class))).thenReturn(new DeliveryHistory());

        // WHEN
        ParcelResponseDTO result = parcelService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(deliveryHistoryRepository).save(any(DeliveryHistory.class));
    }

    @Test
    @DisplayName("Should update parcel without creating history when status unchanged")
    void testUpdate_NoStatusChange() {
        // GIVEN
        parcel.setStatus(ParcelStatus.IN_TRANSIT);
        updateDTO.setStatus(null);
        updateDTO.setDescription("Updated description");

        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(parcel)).thenReturn(parcel);
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);

        // WHEN
        ParcelResponseDTO result = parcelService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(deliveryHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update parcel with delivery person assignment")
    void testUpdate_AssignDeliveryPerson() {
        // GIVEN
        updateDTO.setDeliveryPersonId("dp-1");

        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(deliveryPersonRepository.findById("dp-1")).thenReturn(Optional.of(deliveryPerson));
        when(parcelRepository.save(parcel)).thenReturn(parcel);
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);

        // WHEN
        ParcelResponseDTO result = parcelService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(deliveryPersonRepository).findById("dp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating with non-existent delivery person")
    void testUpdate_DeliveryPersonNotFound() {
        // GIVEN
        updateDTO.setDeliveryPersonId("dp-999");

        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(deliveryPersonRepository.findById("dp-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DeliveryPerson");
    }

    @Test
    @DisplayName("Should update parcel with zone assignment")
    void testUpdate_AssignZone() {
        // GIVEN
        updateDTO.setZoneId("zone-1");

        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(parcelRepository.save(parcel)).thenReturn(parcel);
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);

        // WHEN
        ParcelResponseDTO result = parcelService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(zoneRepository).findById("zone-1");
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete parcel successfully")
    void testDelete_Success() {
        // GIVEN
        when(parcelRepository.existsById("parcel-1")).thenReturn(true);

        // WHEN
        parcelService.delete("parcel-1");

        // THEN
        verify(parcelRepository).deleteById("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent parcel")
    void testDelete_NotFound() {
        // GIVEN
        when(parcelRepository.existsById("parcel-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.delete("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== Tests pour findByStatus() ====================

    @Test
    @DisplayName("Should find parcels by status successfully")
    void testFindByStatus_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.findByStatus(ParcelStatus.CREATED)).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findByStatus(ParcelStatus.CREATED);

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour findByPriority() ====================

    @Test
    @DisplayName("Should find parcels by priority successfully")
    void testFindByPriority_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.findByPriority(ParcelPriority.NORMAL)).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findByPriority(ParcelPriority.NORMAL);

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour findByStatusAndPriority() ====================

    @Test
    @DisplayName("Should find parcels by status and priority successfully")
    void testFindByStatusAndPriority_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.findByStatusAndPriority(ParcelStatus.CREATED, ParcelPriority.NORMAL))
                .thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findByStatusAndPriority(
                ParcelStatus.CREATED, ParcelPriority.NORMAL);

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour countByStatus() ====================

    @Test
    @DisplayName("Should count parcels by status successfully")
    void testCountByStatus_Success() {
        // GIVEN
        when(parcelRepository.countByStatus(ParcelStatus.DELIVERED)).thenReturn(10L);

        // WHEN
        Long result = parcelService.countByStatus(ParcelStatus.DELIVERED);

        // THEN
        assertThat(result).isEqualTo(10L);
    }

    // ==================== Tests pour findBySenderClientId() ====================

    @Test
    @DisplayName("Should find parcels by sender client successfully")
    void testFindBySenderClientId_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(senderClientRepository.existsById("sender-1")).thenReturn(true);
        when(parcelRepository.findBySenderClientId("sender-1")).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findBySenderClientId("sender-1");

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding by non-existent sender")
    void testFindBySenderClientId_NotFound() {
        // GIVEN
        when(senderClientRepository.existsById("sender-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.findBySenderClientId("sender-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SenderClient");
    }

    // ==================== Tests pour findInProgressBySenderClientId() ====================

    @Test
    @DisplayName("Should find in-progress parcels by sender successfully")
    void testFindInProgressBySenderClientId_Success() {
        // GIVEN
        Parcel deliveredParcel = new Parcel();
        deliveredParcel.setStatus(ParcelStatus.DELIVERED);

        List<Parcel> allParcels = Arrays.asList(parcel, deliveredParcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(senderClientRepository.existsById("sender-1")).thenReturn(true);
        when(parcelRepository.findBySenderClientId("sender-1")).thenReturn(allParcels);
        when(parcelMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findInProgressBySenderClientId("sender-1");

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour findDeliveredBySenderClientId() ====================

    @Test
    @DisplayName("Should find delivered parcels by sender successfully")
    void testFindDeliveredBySenderClientId_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(senderClientRepository.existsById("sender-1")).thenReturn(true);
        when(parcelRepository.findBySenderClientIdAndStatus("sender-1", ParcelStatus.DELIVERED))
                .thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findDeliveredBySenderClientId("sender-1");

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour findByRecipientId() ====================

    @Test
    @DisplayName("Should find parcels by recipient successfully")
    void testFindByRecipientId_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(recipientRepository.existsById("recipient-1")).thenReturn(true);
        when(parcelRepository.findByRecipientId("recipient-1")).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findByRecipientId("recipient-1");

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour findByDeliveryPersonId() ====================

    @Test
    @DisplayName("Should find parcels by delivery person successfully")
    void testFindByDeliveryPersonId_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(deliveryPersonRepository.existsById("dp-1")).thenReturn(true);
        when(parcelRepository.findByDeliveryPersonId("dp-1")).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findByDeliveryPersonId("dp-1");

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour findByZoneId() ====================

    @Test
    @DisplayName("Should find parcels by zone successfully")
    void testFindByZoneId_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(zoneRepository.existsById("zone-1")).thenReturn(true);
        when(parcelRepository.findByZoneId("zone-1")).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findByZoneId("zone-1");

        // THEN
        assertThat(result).hasSize(1);
    }

    // ==================== Tests pour special queries ====================

    @Test
    @DisplayName("Should find unassigned parcels successfully")
    void testFindUnassignedParcels_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.findUnassignedParcels()).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findUnassignedParcels();

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should find high priority pending parcels successfully")
    void testFindHighPriorityPending_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.findHighPriorityPending()).thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findHighPriorityPending();

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should find parcels by destination city successfully")
    void testFindByDestinationCity_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel);
        List<ParcelResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.findByDestinationCityContainingIgnoreCase("casa"))
                .thenReturn(parcels);
        when(parcelMapper.toResponseDTOList(parcels)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelResponseDTO> result = parcelService.findByDestinationCity("casa");

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should count total parcels successfully")
    void testCountTotalParcels_Success() {
        // GIVEN
        when(parcelRepository.count()).thenReturn(100L);

        // WHEN
        Long result = parcelService.countTotalParcels();

        // THEN
        assertThat(result).isEqualTo(100L);
    }

    // ==================== Tests pour searchParcels() ====================

    @Test
    @DisplayName("Should search parcels with filters successfully")
    void testSearchParcels_Success() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<Parcel> parcelPage = new PageImpl<>(Arrays.asList(parcel));

        when(parcelRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(parcelPage);
        when(parcelMapper.toResponseDTO(parcel)).thenReturn(responseDTO);

        // WHEN
        Page<ParcelResponseDTO> result = parcelService.searchParcels(
                ParcelStatus.CREATED, ParcelPriority.NORMAL, "zone-1",
                "Casa", "dp-1", "sender-1", "recipient-1", false, pageable);

        // THEN
        assertThat(result.getContent()).hasSize(1);
    }

    // ==================== Tests pour grouping methods ====================

    @Test
    @DisplayName("Should group parcels by status successfully")
    void testGroupByStatus_Success() {
        // GIVEN
        Parcel createdParcel = new Parcel();
        createdParcel.setStatus(ParcelStatus.CREATED);

        Parcel deliveredParcel = new Parcel();
        deliveredParcel.setStatus(ParcelStatus.DELIVERED);

        when(parcelRepository.findAll()).thenReturn(Arrays.asList(createdParcel, deliveredParcel));

        // WHEN
        Map<String, Long> result = parcelService.groupByStatus();

        // THEN
        assertThat(result.get("CREATED")).isEqualTo(1L);
        assertThat(result.get("DELIVERED")).isEqualTo(1L);
        assertThat(result.get("IN_TRANSIT")).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should group parcels by priority successfully")
    void testGroupByPriority_Success() {
        // GIVEN
        Parcel normalParcel = new Parcel();
        normalParcel.setPriority(ParcelPriority.NORMAL);

        Parcel urgentParcel = new Parcel();
        urgentParcel.setPriority(ParcelPriority.URGENT);

        when(parcelRepository.findAll()).thenReturn(Arrays.asList(normalParcel, urgentParcel));

        // WHEN
        Map<String, Long> result = parcelService.groupByPriority();

        // THEN
        assertThat(result.get("NORMAL")).isEqualTo(1L);
        assertThat(result.get("URGENT")).isEqualTo(1L);
        assertThat(result.get("EXPRESS")).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should group parcels by zone successfully")
    void testGroupByZone_Success() {
        // GIVEN
        parcel.setZone(zone);
        zone.setName("Zone Centre");

        when(parcelRepository.findAll()).thenReturn(Arrays.asList(parcel));

        // WHEN
        Map<String, Long> result = parcelService.groupByZone();

        // THEN
        assertThat(result.get("Zone Centre")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should group parcels by city successfully")
    void testGroupByCity_Success() {
        // GIVEN
        parcel.setDestinationCity("Casablanca");

        when(parcelRepository.findAll()).thenReturn(Arrays.asList(parcel));

        // WHEN
        Map<String, Long> result = parcelService.groupByCity();

        // THEN
        assertThat(result.get("Casablanca")).isEqualTo(1L);
    }

    // ==================== Tests pour getParcelHistory() ====================

    @Test
    @DisplayName("Should get parcel history successfully")
    void testGetParcelHistory_Success() {
        // GIVEN
        List<DeliveryHistory> history = Arrays.asList(new DeliveryHistory());
        List<DeliveryHistoryResponseDTO> historyDTOs = Arrays.asList(new DeliveryHistoryResponseDTO());

        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(deliveryHistoryRepository.findByParcelIdOrderByChangedAtAsc("parcel-1"))
                .thenReturn(history);
        when(deliveryHistoryMapper.toResponseDTOList(history)).thenReturn(historyDTOs);

        // WHEN
        List<DeliveryHistoryResponseDTO> result = parcelService.getParcelHistory("parcel-1");

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting history for non-existent parcel")
    void testGetParcelHistory_NotFound() {
        // GIVEN
        when(parcelRepository.existsById("parcel-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelService.getParcelHistory("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");
    }
}
