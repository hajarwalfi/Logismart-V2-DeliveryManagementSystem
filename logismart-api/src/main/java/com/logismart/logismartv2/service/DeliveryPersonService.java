package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonCreateDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonResponseDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonStatsDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonUpdateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.entity.DeliveryPerson;
import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.entity.Zone;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.DeliveryPersonMapper;
import com.logismart.logismartv2.mapper.ParcelMapper;
import com.logismart.logismartv2.repository.DeliveryPersonRepository;
import com.logismart.logismartv2.repository.ParcelRepository;
import com.logismart.logismartv2.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DeliveryPersonService {

    private final DeliveryPersonRepository deliveryPersonRepository;
    private final DeliveryPersonMapper deliveryPersonMapper;
    private final ZoneRepository zoneRepository;
    private final ParcelRepository parcelRepository;
    private final ParcelMapper parcelMapper;

    public DeliveryPersonResponseDTO create(DeliveryPersonCreateDTO dto) {
        log.info("Creating new delivery person with phone: {}", dto.getPhone());

        
        if (deliveryPersonRepository.existsByPhone(dto.getPhone())) {
            log.warn("Delivery person creation failed: phone '{}' already exists", dto.getPhone());
            throw new DuplicateResourceException("DeliveryPerson", "phone", dto.getPhone());
        }

        
        DeliveryPerson deliveryPerson = deliveryPersonMapper.toEntity(dto);

        
        if (dto.getAssignedZoneId() != null) {
            Zone zone = zoneRepository.findById(dto.getAssignedZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", dto.getAssignedZoneId()));
            deliveryPerson.setAssignedZone(zone);
            log.info("Assigned delivery person to zone ID: {}", zone.getId());
        }

        DeliveryPerson savedDeliveryPerson = deliveryPersonRepository.save(deliveryPerson);
        log.info("Delivery person created successfully with ID: {}", savedDeliveryPerson.getId());

        return deliveryPersonMapper.toResponseDTO(savedDeliveryPerson);
    }

    @Transactional(readOnly = true)
    public DeliveryPersonResponseDTO findById(String id) {
        log.info("Finding delivery person by ID: {}", id);

        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByIdWithZone(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", id));

        return deliveryPersonMapper.toResponseDTO(deliveryPerson);
    }

    @Transactional(readOnly = true)
    public List<DeliveryPersonResponseDTO> findAll() {
        log.info("Finding all delivery persons");
        List<DeliveryPerson> deliveryPersons = deliveryPersonRepository.findAllWithZones();
        return deliveryPersonMapper.toResponseDTOList(deliveryPersons);
    }

    public DeliveryPersonResponseDTO update(DeliveryPersonUpdateDTO dto) {
        log.info("Updating delivery person with ID: {}", dto.getId());

        DeliveryPerson existingDeliveryPerson = deliveryPersonRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", dto.getId()));

        
        if (!existingDeliveryPerson.getPhone().equals(dto.getPhone()) &&
                deliveryPersonRepository.existsByPhone(dto.getPhone())) {
            log.warn("Delivery person update failed: phone '{}' already exists", dto.getPhone());
            throw new DuplicateResourceException("DeliveryPerson", "phone", dto.getPhone());
        }

        
        deliveryPersonMapper.updateEntityFromDTO(dto, existingDeliveryPerson);

        
        if (dto.getAssignedZoneId() != null) {
            Zone zone = zoneRepository.findById(dto.getAssignedZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", dto.getAssignedZoneId()));
            existingDeliveryPerson.setAssignedZone(zone);
        } else {
            existingDeliveryPerson.setAssignedZone(null);  
        }

        DeliveryPerson updatedDeliveryPerson = deliveryPersonRepository.save(existingDeliveryPerson);
        log.info("Delivery person updated successfully with ID: {}", updatedDeliveryPerson.getId());

        return deliveryPersonMapper.toResponseDTO(updatedDeliveryPerson);
    }

    public void delete(String id) {
        log.info("Deleting delivery person with ID: {}", id);

        if (!deliveryPersonRepository.existsById(id)) {
            throw new ResourceNotFoundException("DeliveryPerson", "id", id);
        }

        deliveryPersonRepository.deleteById(id);
        log.info("Delivery person deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<DeliveryPersonResponseDTO> findByZone(String zoneId) {
        log.info("Finding delivery persons by zone ID: {}", zoneId);

        
        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("Zone", "id", zoneId);
        }

        List<DeliveryPerson> deliveryPersons = deliveryPersonRepository.findByAssignedZoneId(zoneId);
        return deliveryPersonMapper.toResponseDTOList(deliveryPersons);
    }

    @Transactional(readOnly = true)
    public List<DeliveryPersonResponseDTO> findUnassigned() {
        log.info("Finding unassigned delivery persons");
        List<DeliveryPerson> deliveryPersons = deliveryPersonRepository.findUnassignedDeliveryPersons();
        return deliveryPersonMapper.toResponseDTOList(deliveryPersons);
    }

    @Transactional(readOnly = true)
    public List<DeliveryPersonResponseDTO> findAvailable() {
        log.info("Finding available delivery persons (not currently delivering)");
        List<DeliveryPerson> deliveryPersons = deliveryPersonRepository.findAvailableDeliveryPersons();
        return deliveryPersonMapper.toResponseDTOList(deliveryPersons);
    }

    @Transactional(readOnly = true)
    public List<DeliveryPersonResponseDTO> findAvailableInZone(String zoneId) {
        log.info("Finding available delivery persons in zone ID: {}", zoneId);

        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("Zone", "id", zoneId);
        }

        List<DeliveryPerson> deliveryPersons = deliveryPersonRepository.findAvailableInZone(zoneId);
        return deliveryPersonMapper.toResponseDTOList(deliveryPersons);
    }

    @Transactional(readOnly = true)
    public Long countActiveParcels(String id) {
        log.info("Counting active parcels for delivery person ID: {}", id);

        if (!deliveryPersonRepository.existsById(id)) {
            throw new ResourceNotFoundException("DeliveryPerson", "id", id);
        }

        return deliveryPersonRepository.countActiveParcels(id);
    }

    @Transactional(readOnly = true)
    public Long countDeliveredParcels(String id) {
        log.info("Counting delivered parcels for delivery person ID: {}", id);

        if (!deliveryPersonRepository.existsById(id)) {
            throw new ResourceNotFoundException("DeliveryPerson", "id", id);
        }

        return deliveryPersonRepository.countDeliveredParcels(id);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> findUrgentParcels(String id) {
        log.info("Finding urgent parcels for delivery person ID: {}", id);

        
        if (!deliveryPersonRepository.existsById(id)) {
            throw new ResourceNotFoundException("DeliveryPerson", "id", id);
        }

        
        List<Parcel> allParcels = parcelRepository.findByDeliveryPersonId(id);

        
        List<Parcel> urgentParcels = allParcels.stream()
                .filter(parcel -> parcel.getPriority() == ParcelPriority.URGENT
                        || parcel.getPriority() == ParcelPriority.EXPRESS)
                .toList();

        log.info("Found {} urgent/express parcels for delivery person ID: {}", urgentParcels.size(), id);

        return parcelMapper.toResponseDTOList(urgentParcels);
    }

    @Transactional(readOnly = true)
    public DeliveryPersonStatsDTO getStats(String id) {
        log.info("Calculating statistics for delivery person ID: {}", id);

        DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", id));

        List<Parcel> allParcels = parcelRepository.findByDeliveryPersonId(id);

        Long totalParcels = (long) allParcels.size();
        Double totalWeight = allParcels.stream()
                .map(Parcel::getWeight)
                .mapToDouble(weight -> weight != null ? weight.doubleValue() : 0.0)
                .sum();

        Long activeParcels = allParcels.stream()
                .filter(parcel -> parcel.getStatus() != ParcelStatus.DELIVERED)
                .count();

        Long deliveredParcels = allParcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.DELIVERED)
                .count();

        Long inTransitParcels = allParcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.IN_TRANSIT)
                .count();

        Long collectedParcels = allParcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.COLLECTED)
                .count();

        Long inStockParcels = allParcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.IN_STOCK)
                .count();

        // Monthly statistics
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Parcel> parcelsThisMonth = allParcels.stream()
                .filter(parcel -> parcel.getCreatedAt() != null &&
                        !parcel.getCreatedAt().isBefore(startOfMonth) &&
                        !parcel.getCreatedAt().isAfter(endOfMonth))
                .toList();

        Long totalThisMonth = (long) parcelsThisMonth.size();
        Long deliveredThisMonth = parcelsThisMonth.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.DELIVERED)
                .count();

        // Success rate calculation
        Double successRate = totalParcels > 0
                ? (deliveredParcels.doubleValue() / totalParcels.doubleValue()) * 100.0
                : 0.0;

        // Average deliveries per day this month
        int dayOfMonth = java.time.LocalDate.now().getDayOfMonth();
        Double avgDeliveriesPerDay = dayOfMonth > 0
                ? deliveredThisMonth.doubleValue() / dayOfMonth
                : 0.0;

        String deliveryPersonName = deliveryPerson.getFirstName() + " " + deliveryPerson.getLastName();

        log.info("Statistics for delivery person {}: {} parcels, {} kg total weight, {}% success rate",
                deliveryPersonName, totalParcels, totalWeight, String.format("%.2f", successRate));

        DeliveryPersonStatsDTO stats = new DeliveryPersonStatsDTO();
        stats.setDeliveryPersonId(id);
        stats.setDeliveryPersonName(deliveryPersonName);
        stats.setTotalParcels(totalParcels);
        stats.setTotalWeight(totalWeight);
        stats.setActiveParcels(activeParcels);
        stats.setDeliveredParcels(deliveredParcels);
        stats.setInTransitParcels(inTransitParcels);
        stats.setDeliveredThisMonth(deliveredThisMonth);
        stats.setTotalThisMonth(totalThisMonth);
        stats.setSuccessRate(successRate);
        stats.setAvgDeliveriesPerDay(avgDeliveriesPerDay);
        stats.setCollectedParcels(collectedParcels);
        stats.setInStockParcels(inStockParcels);

        return stats;
    }

    /**
     * Find delivery person by user ID (for authenticated livreur)
     */
    @Transactional(readOnly = true)
    public DeliveryPersonResponseDTO findByUserId(String userId) {
        log.info("Finding delivery person by user ID: {}", userId);

        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "userId", userId));

        return deliveryPersonMapper.toResponseDTO(deliveryPerson);
    }

    /**
     * Get statistics for delivery person by user ID
     */
    @Transactional(readOnly = true)
    public DeliveryPersonStatsDTO getStatsByUserId(String userId) {
        log.info("Getting statistics for delivery person with user ID: {}", userId);

        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "userId", userId));

        return getStats(deliveryPerson.getId());
    }

    /**
     * Get delivery history for delivery person by user ID
     */
    @Transactional(readOnly = true)
    public List<ParcelResponseDTO> getDeliveryHistoryByUserId(String userId) {
        log.info("Getting delivery history for delivery person with user ID: {}", userId);

        DeliveryPerson deliveryPerson = deliveryPersonRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "userId", userId));

        List<Parcel> deliveredParcels = parcelRepository.findByDeliveryPersonIdAndStatus(
                deliveryPerson.getId(), ParcelStatus.DELIVERED);

        return parcelMapper.toResponseDTOList(deliveredParcels);
    }
}
