package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryCreateDTO;
import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.entity.DeliveryHistory;
import com.logismart.logismartv2.entity.DeliveryPerson;
import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.DeliveryHistoryMapper;
import com.logismart.logismartv2.repository.DeliveryHistoryRepository;
import com.logismart.logismartv2.repository.DeliveryPersonRepository;
import com.logismart.logismartv2.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DeliveryHistoryService {

    private final DeliveryHistoryRepository deliveryHistoryRepository;
    private final DeliveryHistoryMapper deliveryHistoryMapper;
    private final ParcelRepository parcelRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;

    public DeliveryHistoryResponseDTO create(DeliveryHistoryCreateDTO dto) {
        log.info("Creating delivery history entry for parcel ID: {} with status: {}",
                dto.getParcelId(), dto.getStatus());

        
        Parcel parcel = parcelRepository.findById(dto.getParcelId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcel", "id", dto.getParcelId()));

        
        DeliveryHistory history = deliveryHistoryMapper.toEntity(dto);
        history.setParcel(parcel);

        
        DeliveryHistory savedHistory = deliveryHistoryRepository.save(history);
        log.info("Delivery history entry created successfully with ID: {}", savedHistory.getId());

        return deliveryHistoryMapper.toResponseDTO(savedHistory);
    }

    @Transactional(readOnly = true)
    public DeliveryHistoryResponseDTO findById(String id) {
        log.info("Finding delivery history by ID: {}", id);

        DeliveryHistory history = deliveryHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryHistory", "id", id));

        return deliveryHistoryMapper.toResponseDTO(history);
    }

    @Transactional(readOnly = true)
    public List<DeliveryHistoryResponseDTO> findAll() {
        log.info("Finding all delivery history entries");
        List<DeliveryHistory> histories = deliveryHistoryRepository.findAll();
        return deliveryHistoryMapper.toResponseDTOList(histories);
    }

    @Transactional(readOnly = true)
    public List<DeliveryHistoryResponseDTO> findByParcelId(String parcelId) {
        log.info("Finding delivery history for parcel ID: {}", parcelId);

        
        if (!parcelRepository.existsById(parcelId)) {
            throw new ResourceNotFoundException("Parcel", "id", parcelId);
        }

        List<DeliveryHistory> histories = deliveryHistoryRepository.findByParcelIdOrderByChangedAtAsc(parcelId);
        log.info("Found {} history entries for parcel ID: {}", histories.size(), parcelId);

        return deliveryHistoryMapper.toResponseDTOList(histories);
    }

    @Transactional(readOnly = true)
    public DeliveryHistoryResponseDTO findLatestByParcelId(String parcelId) {
        log.info("Finding latest delivery history for parcel ID: {}", parcelId);

        if (!parcelRepository.existsById(parcelId)) {
            throw new ResourceNotFoundException("Parcel", "id", parcelId);
        }

        DeliveryHistory history = deliveryHistoryRepository.findLatestByParcelId(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No delivery history found for parcel ID: " + parcelId));

        return deliveryHistoryMapper.toResponseDTO(history);
    }

    public void delete(String id) {
        log.warn("DELETING delivery history entry with ID: {} - This affects audit trail!", id);

        if (!deliveryHistoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("DeliveryHistory", "id", id);
        }

        deliveryHistoryRepository.deleteById(id);
        log.warn("Delivery history entry deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Long countByParcelId(String parcelId) {
        log.info("Counting history entries for parcel ID: {}", parcelId);

        if (!parcelRepository.existsById(parcelId)) {
            throw new ResourceNotFoundException("Parcel", "id", parcelId);
        }

        return deliveryHistoryRepository.countByParcelId(parcelId);
    }

    @Transactional(readOnly = true)
    public List<DeliveryHistoryResponseDTO> findEntriesWithComments() {
        log.info("Finding delivery history entries with comments");
        List<DeliveryHistory> histories = deliveryHistoryRepository.findEntriesWithComments();
        return deliveryHistoryMapper.toResponseDTOList(histories);
    }

    @Transactional(readOnly = true)
    public Long countDeliveriesToday() {
        log.info("Counting deliveries completed today");
        return deliveryHistoryRepository.countDeliveriesToday();
    }

    /**
     * Get delivery history for a delivery person (ROLE_LIVREUR)
     * Returns history of all parcels assigned to this delivery person
     */
    @Transactional(readOnly = true)
    public List<DeliveryHistoryResponseDTO> findMyHistoryForDeliveryPerson(String userId) {
        log.info("Finding delivery history for delivery person with user ID: {}", userId);

        // Find the delivery person by user ID
        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "userId", userId));

        // Get all parcels assigned to this delivery person
        List<Parcel> parcels = parcelRepository.findByDeliveryPersonId(deliveryPerson.getId());

        // Get all delivery history for these parcels
        List<DeliveryHistory> allHistory = parcels.stream()
                .flatMap(parcel -> deliveryHistoryRepository.findByParcelIdOrderByChangedAtAsc(parcel.getId()).stream())
                .collect(Collectors.toList());

        log.info("Found {} history entries for delivery person ID: {}", allHistory.size(), deliveryPerson.getId());

        return deliveryHistoryMapper.toResponseDTOList(allHistory);
    }
}
