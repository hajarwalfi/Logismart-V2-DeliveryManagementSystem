package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.zone.ZoneCreateDTO;
import com.logismart.logismartv2.dto.zone.ZoneResponseDTO;
import com.logismart.logismartv2.dto.zone.ZoneStatsDTO;
import com.logismart.logismartv2.dto.zone.ZoneUpdateDTO;
import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.entity.Zone;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.ZoneMapper;
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
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final ZoneMapper zoneMapper;
    private final ParcelRepository parcelRepository;

    public ZoneResponseDTO create(ZoneCreateDTO dto) {
        log.info("Creating new zone with name: {}", dto.getName());

        
        if (zoneRepository.existsByName(dto.getName())) {
            log.warn("Zone creation failed: name '{}' already exists", dto.getName());
            throw new DuplicateResourceException("Zone", "name", dto.getName());
        }

        if (zoneRepository.existsByPostalCode(dto.getPostalCode())) {
            log.warn("Zone creation failed: postal code '{}' already exists", dto.getPostalCode());
            throw new DuplicateResourceException("Zone", "postalCode", dto.getPostalCode());
        }

        
        Zone zone = zoneMapper.toEntity(dto);

        
        Zone savedZone = zoneRepository.save(zone);
        log.info("Zone created successfully with ID: {}", savedZone.getId());

        
        return zoneMapper.toResponseDTO(savedZone);
    }

    @Transactional(readOnly = true)
    public ZoneResponseDTO findById(String id) {
        log.info("Finding zone by ID: {}", id);

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Zone not found with ID: {}", id);
                    return new ResourceNotFoundException("Zone", "id", id);
                });

        return zoneMapper.toResponseDTO(zone);
    }

    @Transactional(readOnly = true)
    public List<ZoneResponseDTO> findAll() {
        log.info("Finding all zones");

        List<Zone> zones = zoneRepository.findAll();
        log.info("Found {} zones", zones.size());

        return zoneMapper.toResponseDTOList(zones);
    }

    public ZoneResponseDTO update(ZoneUpdateDTO dto) {
        log.info("Updating zone with ID: {}", dto.getId());

        
        Zone existingZone = zoneRepository.findById(dto.getId())
                .orElseThrow(() -> {
                    log.warn("Zone not found with ID: {}", dto.getId());
                    return new ResourceNotFoundException("Zone", "id", dto.getId());
                });

        
        if (!existingZone.getName().equals(dto.getName()) &&
                zoneRepository.existsByName(dto.getName())) {
            log.warn("Zone update failed: name '{}' already exists", dto.getName());
            throw new DuplicateResourceException("Zone", "name", dto.getName());
        }

        
        if (!existingZone.getPostalCode().equals(dto.getPostalCode()) &&
                zoneRepository.existsByPostalCode(dto.getPostalCode())) {
            log.warn("Zone update failed: postal code '{}' already exists", dto.getPostalCode());
            throw new DuplicateResourceException("Zone", "postalCode", dto.getPostalCode());
        }

        
        zoneMapper.updateEntityFromDTO(dto, existingZone);

        
        Zone updatedZone = zoneRepository.save(existingZone);
        log.info("Zone updated successfully with ID: {}", updatedZone.getId());

        return zoneMapper.toResponseDTO(updatedZone);
    }

    public void delete(String id) {
        log.info("Deleting zone with ID: {}", id);

        
        if (!zoneRepository.existsById(id)) {
            log.warn("Zone not found with ID: {}", id);
            throw new ResourceNotFoundException("Zone", "id", id);
        }

        zoneRepository.deleteById(id);
        log.info("Zone deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public ZoneResponseDTO findByName(String name) {
        log.info("Finding zone by name: {}", name);

        Zone zone = zoneRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Zone not found with name: {}", name);
                    return new ResourceNotFoundException("Zone", "name", name);
                });

        return zoneMapper.toResponseDTO(zone);
    }

    @Transactional(readOnly = true)
    public ZoneResponseDTO findByPostalCode(String postalCode) {
        log.info("Finding zone by postal code: {}", postalCode);

        Zone zone = zoneRepository.findByPostalCode(postalCode)
                .orElseThrow(() -> {
                    log.warn("Zone not found with postal code: {}", postalCode);
                    return new ResourceNotFoundException("Zone", "postalCode", postalCode);
                });

        return zoneMapper.toResponseDTO(zone);
    }

    @Transactional(readOnly = true)
    public List<ZoneResponseDTO> searchByName(String keyword) {
        log.info("Searching zones by keyword: {}", keyword);

        List<Zone> zones = zoneRepository.findByNameContainingIgnoreCase(keyword);
        log.info("Found {} zones matching keyword '{}'", zones.size(), keyword);

        return zoneMapper.toResponseDTOList(zones);
    }

    @Transactional(readOnly = true)
    public List<ZoneResponseDTO> findZonesWithDeliveryPersons() {
        log.info("Finding zones with delivery persons");

        List<Zone> zones = zoneRepository.findZonesWithDeliveryPersons();
        log.info("Found {} zones with delivery persons", zones.size());

        return zoneMapper.toResponseDTOList(zones);
    }

    @Transactional(readOnly = true)
    public List<ZoneResponseDTO> findZonesWithoutDeliveryPersons() {
        log.info("Finding zones without delivery persons");

        List<Zone> zones = zoneRepository.findZonesWithoutDeliveryPersons();
        log.info("Found {} zones without delivery persons", zones.size());

        return zoneMapper.toResponseDTOList(zones);
    }

    @Transactional(readOnly = true)
    public Long countDeliveryPersons(String id) {
        log.info("Counting delivery persons in zone ID: {}", id);

        
        if (!zoneRepository.existsById(id)) {
            log.warn("Zone not found with ID: {}", id);
            throw new ResourceNotFoundException("Zone", "id", id);
        }

        Long count = zoneRepository.countDeliveryPersonsByZoneId(id);
        log.info("Zone ID {} has {} delivery persons", id, count);

        return count;
    }

    @Transactional(readOnly = true)
    public Long countParcels(String id) {
        log.info("Counting parcels in zone ID: {}", id);

        
        if (!zoneRepository.existsById(id)) {
            log.warn("Zone not found with ID: {}", id);
            throw new ResourceNotFoundException("Zone", "id", id);
        }

        Long count = parcelRepository.countByZoneId(id);
        log.info("Zone ID {} has {} parcels", id, count);

        return count;
    }

    @Transactional(readOnly = true)
    public ZoneStatsDTO getStats(String id) {
        log.info("Calculating statistics for zone ID: {}", id);

        
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));

        
        List<Parcel> allParcels = parcelRepository.findByZoneId(id);

        
        Long totalParcels = (long) allParcels.size();
        Double totalWeight = allParcels.stream()
                .map(Parcel::getWeight)
                .mapToDouble(weight -> weight != null ? weight.doubleValue() : 0.0)
                .sum();

        Long inTransitParcels = allParcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.IN_TRANSIT)
                .count();

        Long deliveredParcels = allParcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.DELIVERED)
                .count();

        Long unassignedParcels = allParcels.stream()
                .filter(parcel -> parcel.getDeliveryPerson() == null)
                .count();

        log.info("Statistics for zone {}: {} parcels, {} kg total weight",
                zone.getName(), totalParcels, totalWeight);

        return new ZoneStatsDTO(
                id,
                zone.getName(),
                totalParcels,
                totalWeight,
                inTransitParcels,
                deliveredParcels,
                unassignedParcels
        );
    }
}
