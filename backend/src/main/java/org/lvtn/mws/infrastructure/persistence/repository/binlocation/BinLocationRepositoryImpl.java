package org.lvtn.mws.infrastructure.persistence.repository.binlocation;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.domain.repository.IBinLocationRepository;
import org.lvtn.mws.infrastructure.persistence.entity.BinLocationDbEntity;
import org.lvtn.mws.infrastructure.persistence.mapper.BinLocationInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BinLocationRepositoryImpl implements IBinLocationRepository {

    private final JpaBinLocationRepository jpaRepository;
    private final BinLocationInfraMapper   mapper;

    @Override
    public BinLocation save(BinLocation binLocation) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(binLocation)));
    }

    @Override
    public List<BinLocation> saveAll(List<BinLocation> binLocations) {
        List<BinLocationDbEntity> entities =
                binLocations.stream().map(mapper::toEntity).collect(Collectors.toList());
        return jpaRepository.saveAll(entities)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<BinLocation> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<BinLocation> findByWarehouseId(String warehouseId) {
        return jpaRepository.findByWarehouseId(warehouseId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCoordinate(String warehouseId, String zone,
                                      String aisle, String rack, String bin) {
        return jpaRepository.existsByWarehouseIdAndZoneAndAisleAndRackAndBin(
                warehouseId, zone, aisle, rack, bin);
    }

    @Override
    public List<String> findExistingCoordinateKeys(String warehouseId, List<String> coordinateKeys) {
        if (coordinateKeys == null || coordinateKeys.isEmpty()) return List.of();
        return jpaRepository.findExistingCoordinateKeys(warehouseId, coordinateKeys);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
