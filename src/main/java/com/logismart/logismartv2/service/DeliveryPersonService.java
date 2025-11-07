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

        String deliveryPersonName = deliveryPerson.getFirstName() + " " + deliveryPerson.getLastName();

        log.info("Statistics for delivery person {}: {} parcels, {} kg total weight",
                deliveryPersonName, totalParcels, totalWeight);

        return new DeliveryPersonStatsDTO(
                id,
                deliveryPersonName,
                totalParcels,
                totalWeight,
                activeParcels,
                deliveredParcels,
                inTransitParcels
        );
    }
}
