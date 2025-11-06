package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.statistics.DeliveryPersonStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.GlobalStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.ZoneStatisticsDTO;
import com.logismart.logismartv2.entity.*;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final ParcelRepository parcelRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final ZoneRepository zoneRepository;
    private final SenderClientRepository senderClientRepository;
    private final RecipientRepository recipientRepository;
    private final ProductRepository productRepository;

    public DeliveryPersonStatisticsDTO getDeliveryPersonStatistics(String deliveryPersonId) {
        log.info("Calculating statistics for delivery person ID: {}", deliveryPersonId);

        
        DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPerson", "id", deliveryPersonId));

        
        List<Parcel> parcels = parcelRepository.findByDeliveryPersonId(deliveryPersonId);

        
        BigDecimal totalWeight = parcels.stream()
                .map(Parcel::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        
        BigDecimal averageWeight = parcels.isEmpty() ? BigDecimal.ZERO :
                totalWeight.divide(BigDecimal.valueOf(parcels.size()), 2, RoundingMode.HALF_UP);

        
        long created = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.CREATED).count();
        long collected = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.COLLECTED).count();
        long inStock = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_STOCK).count();
        long inTransit = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_TRANSIT).count();
        long delivered = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.DELIVERED).count();

        
        double deliveryRate = parcels.isEmpty() ? 0.0 :
                (double) delivered / parcels.size() * 100.0;

        return DeliveryPersonStatisticsDTO.builder()
                .deliveryPersonId(deliveryPersonId)
                .deliveryPersonName(deliveryPerson.getFirstName() + " " + deliveryPerson.getLastName())
                .zoneName(deliveryPerson.getAssignedZone() != null ?
                        deliveryPerson.getAssignedZone().getName() : "Unassigned")
                .totalParcels((long) parcels.size())
                .totalWeight(totalWeight)
                .averageWeight(averageWeight)
                .parcelsCreated(created)
                .parcelsCollected(collected)
                .parcelsInStock(inStock)
                .parcelsInTransit(inTransit)
                .parcelsDelivered(delivered)
                .deliveryRate(Math.round(deliveryRate * 100.0) / 100.0)
                .build();
    }

    public ZoneStatisticsDTO getZoneStatistics(String zoneId) {
        log.info("Calculating statistics for zone ID: {}", zoneId);

        
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneId));

        
        List<Parcel> parcels = parcelRepository.findByZoneId(zoneId);

        
        Long deliveryPersonCount = zoneRepository.countDeliveryPersonsByZoneId(zoneId);

        
        BigDecimal totalWeight = parcels.stream()
                .map(Parcel::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        
        BigDecimal averageWeight = parcels.isEmpty() ? BigDecimal.ZERO :
                totalWeight.divide(BigDecimal.valueOf(parcels.size()), 2, RoundingMode.HALF_UP);

        
        long created = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.CREATED).count();
        long collected = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.COLLECTED).count();
        long inStock = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_STOCK).count();
        long inTransit = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_TRANSIT).count();
        long delivered = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.DELIVERED).count();

        
        long normal = parcels.stream().filter(p -> p.getPriority() == ParcelPriority.NORMAL).count();
        long urgent = parcels.stream().filter(p -> p.getPriority() == ParcelPriority.URGENT).count();
        long express = parcels.stream().filter(p -> p.getPriority() == ParcelPriority.EXPRESS).count();

        
        double avgParcelsPerPerson = deliveryPersonCount == 0 ? 0.0 :
                (double) parcels.size() / deliveryPersonCount;

        return ZoneStatisticsDTO.builder()
                .zoneId(zoneId)
                .zoneName(zone.getName())
                .postalCode(zone.getPostalCode())
                .totalParcels((long) parcels.size())
                .totalWeight(totalWeight)
                .averageWeight(averageWeight)
                .deliveryPersonCount(deliveryPersonCount)
                .parcelsCreated(created)
                .parcelsCollected(collected)
                .parcelsInStock(inStock)
                .parcelsInTransit(inTransit)
                .parcelsDelivered(delivered)
                .parcelsNormal(normal)
                .parcelsUrgent(urgent)
                .parcelsExpress(express)
                .averageParcelsPerDeliveryPerson(Math.round(avgParcelsPerPerson * 100.0) / 100.0)
                .build();
    }

    public GlobalStatisticsDTO getGlobalStatistics() {
        log.info("Calculating global system statistics");

        
        Long totalParcels = parcelRepository.count();
        Long totalZones = zoneRepository.count();
        Long totalDeliveryPersons = deliveryPersonRepository.count();
        Long totalSenderClients = senderClientRepository.count();
        Long totalRecipients = recipientRepository.count();
        Long totalProducts = productRepository.count();

        
        List<Parcel> allParcels = parcelRepository.findAll();

        
        BigDecimal totalWeight = allParcels.stream()
                .map(Parcel::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        
        BigDecimal averageWeight = allParcels.isEmpty() ? BigDecimal.ZERO :
                totalWeight.divide(BigDecimal.valueOf(allParcels.size()), 2, RoundingMode.HALF_UP);

        
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CREATED", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.CREATED).count());
        byStatus.put("COLLECTED", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.COLLECTED).count());
        byStatus.put("IN_STOCK", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_STOCK).count());
        byStatus.put("IN_TRANSIT", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_TRANSIT).count());
        byStatus.put("DELIVERED", allParcels.stream().filter(p -> p.getStatus() == ParcelStatus.DELIVERED).count());

        
        Map<String, Long> byPriority = new HashMap<>();
        byPriority.put("NORMAL", allParcels.stream().filter(p -> p.getPriority() == ParcelPriority.NORMAL).count());
        byPriority.put("URGENT", allParcels.stream().filter(p -> p.getPriority() == ParcelPriority.URGENT).count());
        byPriority.put("EXPRESS", allParcels.stream().filter(p -> p.getPriority() == ParcelPriority.EXPRESS).count());

        
        Long unassignedParcels = allParcels.stream()
                .filter(p -> p.getDeliveryPerson() == null)
                .count();

        
        Long highPriorityPending = allParcels.stream()
                .filter(p -> (p.getPriority() == ParcelPriority.URGENT || p.getPriority() == ParcelPriority.EXPRESS))
                .filter(p -> p.getStatus() != ParcelStatus.DELIVERED)
                .count();

        
        double avgParcelsPerPerson = totalDeliveryPersons == 0 ? 0.0 :
                (double) totalParcels / totalDeliveryPersons;

        return GlobalStatisticsDTO.builder()
                .totalParcels(totalParcels)
                .totalWeight(totalWeight)
                .totalZones(totalZones)
                .totalDeliveryPersons(totalDeliveryPersons)
                .totalSenderClients(totalSenderClients)
                .totalRecipients(totalRecipients)
                .totalProducts(totalProducts)
                .parcelsByStatus(byStatus)
                .parcelsByPriority(byPriority)
                .unassignedParcels(unassignedParcels)
                .highPriorityPending(highPriorityPending)
                .averageParcelsPerDeliveryPerson(Math.round(avgParcelsPerPerson * 100.0) / 100.0)
                .averageWeight(averageWeight)
                .build();
    }

    public List<DeliveryPersonStatisticsDTO> getAllDeliveryPersonStatistics() {
        log.info("Calculating statistics for all delivery persons");

        List<DeliveryPerson> deliveryPersons = deliveryPersonRepository.findAll();

        return deliveryPersons.stream()
                .map(dp -> getDeliveryPersonStatistics(dp.getId()))
                .toList();
    }

    public List<ZoneStatisticsDTO> getAllZoneStatistics() {
        log.info("Calculating statistics for all zones");

        List<Zone> zones = zoneRepository.findAll();

        return zones.stream()
                .map(z -> getZoneStatistics(z.getId()))
                .toList();
    }
}
