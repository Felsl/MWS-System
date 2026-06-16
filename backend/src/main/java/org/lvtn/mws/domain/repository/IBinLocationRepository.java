package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.BinLocation;

import java.util.List;
import java.util.Optional;

public interface IBinLocationRepository {
    BinLocation save(BinLocation binLocation);
    List<BinLocation> saveAll(List<BinLocation> binLocations);
    Optional<BinLocation> findById(String id);
    List<BinLocation> findByWarehouseId(String warehouseId);
    boolean existsByCoordinate(String warehouseId, String zone, String aisle, String rack, String bin);
    List<String> findExistingCoordinateKeys(String warehouseId, List<String> coordinateKeys);
    void deleteById(String id);
}
