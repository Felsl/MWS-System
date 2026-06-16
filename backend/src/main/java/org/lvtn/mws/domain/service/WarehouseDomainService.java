package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.repository.IBinLocationRepository;
import org.lvtn.mws.domain.repository.IWarehouseRepository;

import java.util.ArrayList;
import java.util.List;

public class WarehouseDomainService {

    private final IWarehouseRepository    warehouseRepository;
    private final IBinLocationRepository  binLocationRepository;

    public WarehouseDomainService(IWarehouseRepository warehouseRepository,
                                  IBinLocationRepository binLocationRepository) {
        this.warehouseRepository   = warehouseRepository;
        this.binLocationRepository = binLocationRepository;
    }

    // ─── Warehouse ────────────────────────────────────────────────────────

    public Warehouse create(String id, String code, String name, String address) {
        validateCodeNotTaken(code);
        Warehouse warehouse = new Warehouse.WarehouseBuilder()
                .id(id).code(code).name(name).address(address).build();
        return warehouseRepository.save(warehouse);
    }

    public Warehouse findById(String id) {
        return warehouseRepository.findById(id)
                .filter(w -> !w.isDeleted())
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
    }

    public List<Warehouse> findAllActive() {
        return warehouseRepository.findAllActive();
    }

    public Warehouse update(String id, String code, String name, String address) {
        Warehouse warehouse = findById(id);
        if (!warehouse.getCode().equals(code)) {
            validateCodeNotTakenExcluding(id, code);
        }
        warehouse.update(code, name, address);
        return warehouseRepository.save(warehouse);
    }

    public void delete(String id) {
        Warehouse warehouse = findById(id);
        warehouse.delete();
        warehouseRepository.save(warehouse);
    }

    // ─── BinLocation ──────────────────────────────────────────────────────

    public BinLocation createBinLocation(String id, String warehouseId,
                                         String zone, String aisle, String rack, String bin) {
        findById(warehouseId); // validate warehouse exists
        if (binLocationRepository.existsByCoordinate(warehouseId, zone, aisle, rack, bin)) {
            throw new IllegalArgumentException(
                    "Bin location already exists: " + zone + "-" + aisle + "-" + rack + "-" + bin);
        }
        BinLocation bl = new BinLocation.BinLocationBuilder()
                .id(id).warehouseId(warehouseId)
                .zone(zone).aisle(aisle).rack(rack).bin(bin)
                .build();
        return binLocationRepository.save(bl);
    }

    /**
     * Bulk generation: Nhận danh sách BinLocation đã được tầng UseCase build sẵn,
     * validate trùng tọa độ theo batch, rồi bulk insert.
     */
    public List<BinLocation> bulkGenerateBinLocations(String warehouseId,
                                                       List<BinLocation> candidates) {
        findById(warehouseId); // validate warehouse exists

        // Thu thập coordinate keys của candidates
        List<String> candidateKeys = new ArrayList<>();
        for (BinLocation bl : candidates) {
            candidateKeys.add(bl.toCoordinateKey());
        }

        // Check trùng với DB (1 query batch thay vì N queries)
        List<String> existingKeys = binLocationRepository
                .findExistingCoordinateKeys(warehouseId, candidateKeys);

        if (!existingKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "Duplicate bin locations detected: " + existingKeys);
        }

        return binLocationRepository.saveAll(candidates);
    }

    public BinLocation findBinLocationById(String id) {
        return binLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bin location not found: " + id));
    }

    public List<BinLocation> findBinLocationsByWarehouse(String warehouseId) {
        findById(warehouseId);
        return binLocationRepository.findByWarehouseId(warehouseId);
    }

    public void deleteBinLocation(String id) {
        findBinLocationById(id);
        binLocationRepository.deleteById(id);
    }

    // ─── Private ──────────────────────────────────────────────────────────

    private void validateCodeNotTaken(String code) {
        if (warehouseRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Warehouse code already exists: " + code);
        }
    }

    private void validateCodeNotTakenExcluding(String id, String code) {
        if (warehouseRepository.existsByCodeExcludingId(id, code)) {
            throw new IllegalArgumentException("Warehouse code already exists: " + code);
        }
    }
}
