package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelCreateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelProductItemDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelUpdateDTO;
import com.logismart.logismartv2.entity.*;
import com.logismart.logismartv2.exception.BadRequestException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.DeliveryHistoryMapper;
import com.logismart.logismartv2.mapper.ParcelMapper;
import com.logismart.logismartv2.repository.*;
import com.logismart.logismartv2.repository.ParcelSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final ParcelMapper parcelMapper;
    private final SenderClientRepository senderClientRepository;
    private final RecipientRepository recipientRepository;
    private final ProductRepository productRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final ZoneRepository zoneRepository;
    private final ParcelProductRepository parcelProductRepository;
    private final DeliveryHistoryRepository deliveryHistoryRepository;
    private final DeliveryHistoryMapper deliveryHistoryMapper;

    public ParcelResponseDTO create(ParcelCreateDTO dto) {
        log.info("Creating new parcel for sender ID: {} to recipient ID: {}",
                dto.getSenderClientId(), dto.getRecipientId());

        
        SenderClient sender = senderClientRepository.findById(dto.getSenderClientId())
                .orElseThrow(() -> new ResourceNotFoundException("SenderClient", "id", dto.getSenderClientId()));

        
        Recipient recipient = recipientRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient", "id", dto.getRecipientId()));

        
        for (ParcelProductItemDTO productItem : dto.getProducts()) {
            if (!productRepository.existsById(productItem.getProductId())) {
                throw new ResourceNotFoundException("Product", "id", productItem.getProductId());
            }
        }

        
        Parcel parcel = parcelMapper.toEntity(dto);
        parcel.setStatus(ParcelStatus.CREATED); 
        parcel.setSenderClient(sender);
        parcel.setRecipient(recipient);
        

        
        Parcel savedParcel = parcelRepository.save(parcel);
        log.info("Parcel created with ID: {}", savedParcel.getId());

        
        for (ParcelProductItemDTO productItem : dto.getProducts()) {
            Product product = productRepository.findById(productItem.getProductId()).get();

            ParcelProduct parcelProduct = new ParcelProduct();
            parcelProduct.setParcel(savedParcel);
            parcelProduct.setProduct(product);
            parcelProduct.setQuantity(productItem.getQuantity());
            parcelProduct.setPrice(productItem.getPrice());

            parcelProductRepository.save(parcelProduct);
            log.info("Added product ID: {} (qty: {}) to parcel ID: {}",
                    product.getId(), productItem.getQuantity(), savedParcel.getId());
        }

        
        DeliveryHistory initialHistory = new DeliveryHistory();
        initialHistory.setParcel(savedParcel);
        initialHistory.setStatus(ParcelStatus.CREATED);
        initialHistory.setChangedAt(LocalDateTime.now());
        initialHistory.setComment("Parcel created");

        deliveryHistoryRepository.save(initialHistory);
        log.info("Initial delivery history created for parcel ID: {}", savedParcel.getId());

        log.info("Parcel creation complete - ID: {}, Products: {}, Status: CREATED",
                savedParcel.getId(), dto.getProducts().size());

        return parcelMapper.toResponseDTO(savedParcel);
    }

    @Transactional(readOnly = true)
    public ParcelResponseDTO findById(String id) {
        log.info("Finding parcel by ID: {}", id);

        Parcel parcel = parcelRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel", "id", id));

        return parcelMapper.toResponseDTO(parcel);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findAll() {
        log.info("Finding all parcels");
        List<Parcel> parcels = parcelRepository.findAll();

        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public Page<ParcelResponseDTO> findAll(Pageable pageable) {
        log.info("Finding all parcels with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<Parcel> parcelPage = parcelRepository.findAll(pageable);
        return parcelPage.map(parcelMapper::toResponseDTO);
    }

    public ParcelResponseDTO update(ParcelUpdateDTO dto) {
        log.info("Updating parcel with ID: {}", dto.getId());

        
        Parcel existingParcel = parcelRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcel", "id", dto.getId()));

        ParcelStatus oldStatus = existingParcel.getStatus();

        
        if (dto.getDescription() != null) {
            existingParcel.setDescription(dto.getDescription());
            log.info("Updated description for parcel ID: {}", dto.getId());
        }

        
        if (dto.getWeight() != null) {
            existingParcel.setWeight(dto.getWeight());
            log.info("Updated weight to {} kg for parcel ID: {}", dto.getWeight(), dto.getId());
        }

        
        if (dto.getPriority() != null) {
            existingParcel.setPriority(dto.getPriority());
            log.info("Updated priority to {} for parcel ID: {}", dto.getPriority(), dto.getId());
        }

        
        if (dto.getDestinationCity() != null) {
            existingParcel.setDestinationCity(dto.getDestinationCity());
            log.info("Updated destination city to {} for parcel ID: {}", dto.getDestinationCity(), dto.getId());
        }

        
        if (dto.getStatus() != null) {
            existingParcel.setStatus(dto.getStatus());
            log.info("Updated status to {} for parcel ID: {}", dto.getStatus(), dto.getId());
        }

        
        if (dto.getDeliveryPersonId() != null) {
            DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(dto.getDeliveryPersonId())
                    .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", dto.getDeliveryPersonId()));
            existingParcel.setDeliveryPerson(deliveryPerson);
            log.info("Assigned delivery person ID: {} to parcel ID: {}", deliveryPerson.getId(), dto.getId());
        }

        
        if (dto.getZoneId() != null) {
            Zone zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", dto.getZoneId()));
            existingParcel.setZone(zone);
            log.info("Assigned zone ID: {} to parcel ID: {}", zone.getId(), dto.getId());
        }

        
        Parcel updatedParcel = parcelRepository.save(existingParcel);
        log.info("Parcel updated successfully with ID: {}", updatedParcel.getId());

        
        if (dto.getStatus() != null && oldStatus != dto.getStatus()) {
            DeliveryHistory historyEntry = new DeliveryHistory();
            historyEntry.setParcel(updatedParcel);
            historyEntry.setStatus(dto.getStatus());
            historyEntry.setChangedAt(LocalDateTime.now());
            historyEntry.setComment("Status updated from " + oldStatus + " to " + dto.getStatus());

            deliveryHistoryRepository.save(historyEntry);
            log.info("Delivery history entry created: {} → {}", oldStatus, dto.getStatus());
        }

        return parcelMapper.toResponseDTO(updatedParcel);
    }

    public void delete(String id) {
        log.info("Deleting parcel with ID: {}", id);

        if (!parcelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parcel", "id", id);
        }

        parcelRepository.deleteById(id);
        log.info("Parcel deleted successfully with ID: {} (cascade deleted products and history)", id);
    }

    

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findByStatus(ParcelStatus status) {
        log.info("Finding parcels with status: {}", status);
        List<Parcel> parcels = parcelRepository.findByStatus(status);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findByPriority(ParcelPriority priority) {
        log.info("Finding parcels with priority: {}", priority);
        List<Parcel> parcels = parcelRepository.findByPriority(priority);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findByStatusAndPriority(ParcelStatus status, ParcelPriority priority) {
        log.info("Finding parcels with status: {} and priority: {}", status, priority);
        List<Parcel> parcels = parcelRepository.findByStatusAndPriority(status, priority);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public Long countByStatus(ParcelStatus status) {
        log.info("Counting parcels with status: {}", status);
        return parcelRepository.countByStatus(status);
    }

    

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findBySenderClientId(String senderClientId) {
        log.info("Finding parcels for sender client ID: {}", senderClientId);

        if (!senderClientRepository.existsById(senderClientId)) {
            throw new ResourceNotFoundException("SenderClient", "id", senderClientId);
        }

        List<Parcel> parcels = parcelRepository.findBySenderClientId(senderClientId);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findInProgressBySenderClientId(String senderClientId) {
        log.info("Finding in-progress parcels for sender client ID: {}", senderClientId);

        if (!senderClientRepository.existsById(senderClientId)) {
            throw new ResourceNotFoundException("SenderClient", "id", senderClientId);
        }

        List<Parcel> parcels = parcelRepository.findBySenderClientId(senderClientId);

        
        List<Parcel> inProgressParcels = parcels.stream()
                .filter(parcel -> parcel.getStatus() != ParcelStatus.DELIVERED)
                .toList();

        return parcelMapper.toResponseDTOList(inProgressParcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findDeliveredBySenderClientId(String senderClientId) {
        log.info("Finding delivered parcels for sender client ID: {}", senderClientId);

        if (!senderClientRepository.existsById(senderClientId)) {
            throw new ResourceNotFoundException("SenderClient", "id", senderClientId);
        }

        List<Parcel> parcels = parcelRepository.findBySenderClientIdAndStatus(senderClientId, ParcelStatus.DELIVERED);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findByRecipientId(String recipientId) {
        log.info("Finding parcels for recipient ID: {}", recipientId);

        if (!recipientRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException("Recipient", "id", recipientId);
        }

        List<Parcel> parcels = parcelRepository.findByRecipientId(recipientId);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findByDeliveryPersonId(String deliveryPersonId) {
        log.info("Finding parcels for delivery person ID: {}", deliveryPersonId);

        if (!deliveryPersonRepository.existsById(deliveryPersonId)) {
            throw new ResourceNotFoundException("DeliveryPerson", "id", deliveryPersonId);
        }

        List<Parcel> parcels = parcelRepository.findByDeliveryPersonId(deliveryPersonId);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findByZoneId(String zoneId) {
        log.info("Finding parcels in zone ID: {}", zoneId);

        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("Zone", "id", zoneId);
        }

        List<Parcel> parcels = parcelRepository.findByZoneId(zoneId);
        return parcelMapper.toResponseDTOList(parcels);
    }

    

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findUnassignedParcels() {
        log.info("Finding unassigned parcels");
        List<Parcel> parcels = parcelRepository.findUnassignedParcels();
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findHighPriorityPending() {
        log.info("Finding high priority pending parcels");
        List<Parcel> parcels = parcelRepository.findHighPriorityPending();
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findByDestinationCity(String city) {
        log.info("Finding parcels for destination city: {}", city);
        List<Parcel> parcels = parcelRepository.findByDestinationCityContainingIgnoreCase(city);
        return parcelMapper.toResponseDTOList(parcels);
    }

    @Transactional(readOnly = true)
    public Long countTotalParcels() {
        log.info("Counting total parcels");
        return parcelRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<ParcelResponseDTO> searchParcels(
            ParcelStatus status,
            ParcelPriority priority,
            String zoneId,
            String destinationCity,
            String deliveryPersonId,
            String senderClientId,
            String recipientId,
            Boolean unassignedOnly,
            Pageable pageable) {

        log.info("Searching parcels with filters - status: {}, priority: {}, zoneId: {}, city: {}, " +
                        "deliveryPersonId: {}, senderClientId: {}, recipientId: {}, unassignedOnly: {}, page: {}, size: {}",
                status, priority, zoneId, destinationCity, deliveryPersonId, senderClientId,
                recipientId, unassignedOnly, pageable.getPageNumber(), pageable.getPageSize());

        
        Page<Parcel> parcelPage = parcelRepository.findAll(
                ParcelSpecification.withFilters(status, priority, zoneId, destinationCity,
                        deliveryPersonId, senderClientId, recipientId, unassignedOnly),
                pageable
        );

        log.info("Found {} parcels matching filters (page {} of {})",
                parcelPage.getNumberOfElements(), parcelPage.getNumber() + 1, parcelPage.getTotalPages());

        return parcelPage.map(parcelMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> groupByStatus() {
        log.info("Grouping parcels by status");

        List<Parcel> allParcels = parcelRepository.findAll();
        Map<String, Long> grouped = new HashMap<>();

        grouped.put("CREATED", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.CREATED).count());
        grouped.put("COLLECTED", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.COLLECTED).count());
        grouped.put("IN_STOCK", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_STOCK).count());
        grouped.put("IN_TRANSIT", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_TRANSIT).count());
        grouped.put("DELIVERED", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.DELIVERED).count());

        log.info("Grouped parcels by status: {}", grouped);
        return grouped;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> groupByPriority() {
        log.info("Grouping parcels by priority");

        List<Parcel> allParcels = parcelRepository.findAll();
        Map<String, Long> grouped = new HashMap<>();

        grouped.put("NORMAL", allParcels.stream().filter(p -> p.getPriority() == ParcelPriority.NORMAL).count());
        grouped.put("URGENT", allParcels.stream().filter(p -> p.getPriority() == ParcelPriority.URGENT).count());
        grouped.put("EXPRESS", allParcels.stream().filter(p -> p.getPriority() == ParcelPriority.EXPRESS).count());

        log.info("Grouped parcels by priority: {}", grouped);
        return grouped;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> groupByZone() {
        log.info("Grouping parcels by zone");

        List<Parcel> allParcels = parcelRepository.findAll();
        Map<String, Long> grouped = new HashMap<>();

        
        allParcels.forEach(parcel -> {
            String zoneName = parcel.getZone() != null ?
                    parcel.getZone().getName() : "Unassigned";
            grouped.merge(zoneName, 1L, Long::sum);
        });

        log.info("Grouped parcels by zone: {}", grouped);
        return grouped;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> groupByCity() {
        log.info("Grouping parcels by destination city");

        List<Parcel> allParcels = parcelRepository.findAll();
        Map<String, Long> grouped = new HashMap<>();

        
        allParcels.forEach(parcel -> {
            String city = parcel.getDestinationCity() != null ?
                    parcel.getDestinationCity() : "Unknown";
            grouped.merge(city, 1L, Long::sum);
        });

        log.info("Grouped parcels by city: {}", grouped);
        return grouped;
    }

    @Transactional(readOnly = true)
    public List<DeliveryHistoryResponseDTO> getParcelHistory(String parcelId) {
        log.info("Fetching delivery history for parcel ID: {}", parcelId);


        if (!parcelRepository.existsById(parcelId)) {
            throw new ResourceNotFoundException("Parcel", "id", parcelId);
        }


        List<DeliveryHistory> history = deliveryHistoryRepository.findByParcelIdOrderByChangedAtAsc(parcelId);

        log.info("Found {} history entries for parcel ID: {}", history.size(), parcelId);


        return deliveryHistoryMapper.toResponseDTOList(history);
    }

    /**
     * Get parcels assigned to a delivery person (ROLE_LIVREUR)
     */
    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findMyParcelsForDeliveryPerson(String userId) {
        log.info("Finding parcels for delivery person with user ID: {}", userId);

        // Find the delivery person by user ID
        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "userId", userId));

        // Get all parcels assigned to this delivery person
        List<Parcel> parcels = parcelRepository.findByDeliveryPersonId(deliveryPerson.getId());

        log.info("Found {} parcels for delivery person ID: {}", parcels.size(), deliveryPerson.getId());

        return parcelMapper.toResponseDTOList(parcels);
    }

    /**
     * Get parcels created by a client (ROLE_CLIENT)
     */
    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findMyParcelsForClient(String userId) {
        log.info("Finding parcels for client with user ID: {}", userId);

        // Find the sender client by user ID
        SenderClient senderClient = senderClientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("SenderClient", "userId", userId));

        // Get all parcels sent by this client
        List<Parcel> parcels = parcelRepository.findBySenderClientId(senderClient.getId());

        log.info("Found {} parcels for sender client ID: {}", parcels.size(), senderClient.getId());

        return parcelMapper.toResponseDTOList(parcels);
    }

    /**
     * Update parcel status (for ROLE_LIVREUR - restricted to their own parcels)
     */
    public ParcelResponseDTO updateParcelStatusForDeliveryPerson(String parcelId, ParcelStatus newStatus, String userId) {
        log.info("Delivery person (user ID: {}) attempting to update parcel {} to status {}", userId, parcelId, newStatus);

        // Find the delivery person
        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "userId", userId));

        // Find the parcel
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel", "id", parcelId));

        // Verify that the parcel is assigned to this delivery person
        if (parcel.getDeliveryPerson() == null || !parcel.getDeliveryPerson().getId().equals(deliveryPerson.getId())) {
            throw new BadRequestException("You can only update status for parcels assigned to you");
        }

        // Update the status
        ParcelStatus oldStatus = parcel.getStatus();
        parcel.setStatus(newStatus);
        Parcel updatedParcel = parcelRepository.save(parcel);

        // Create delivery history entry
        DeliveryHistory history = new DeliveryHistory();
        history.setParcel(updatedParcel);
        history.setStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setComment(String.format("Status updated from %s to %s by delivery person", oldStatus, newStatus));
        deliveryHistoryRepository.save(history);

        log.info("Parcel {} status updated from {} to {} by delivery person {}",
                parcelId, oldStatus, newStatus, deliveryPerson.getId());

        return parcelMapper.toResponseDTO(updatedParcel);
    }

    /**
     * Verify if a parcel belongs to a client (for access control)
     */
    @Transactional(readOnly = true)
    public boolean isParcelOwnedByClient(String parcelId, String userId) {
        SenderClient senderClient = senderClientRepository.findByUserId(userId)
                .orElse(null);

        if (senderClient == null) {
            return false;
        }

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElse(null);

        if (parcel == null) {
            return false;
        }

        return parcel.getSenderClient().getId().equals(senderClient.getId());
    }

    /**
     * Verify if a parcel is assigned to a delivery person (for access control)
     */
    @Transactional(readOnly = true)
    public boolean isParcelAssignedToDeliveryPerson(String parcelId, String userId) {
        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByUserId(userId)
                .orElse(null);

        if (deliveryPerson == null) {
            return false;
        }

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElse(null);

        if (parcel == null || parcel.getDeliveryPerson() == null) {
            return false;
        }

        return parcel.getDeliveryPerson().getId().equals(deliveryPerson.getId());
    }

}
