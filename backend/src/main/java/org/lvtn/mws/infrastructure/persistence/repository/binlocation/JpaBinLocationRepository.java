package org.lvtn.mws.infrastructure.persistence.repository.binlocation;

import org.lvtn.mws.infrastructure.persistence.entity.BinLocationDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaBinLocationRepository extends JpaRepository<BinLocationDbEntity, String> {

    List<BinLocationDbEntity> findByWarehouseId(String warehouseId);

    boolean existsByWarehouseIdAndZoneAndAisleAndRackAndBin(
            String warehouseId, String zone, String aisle, String rack, String bin);

    /**
     * Batch-check: trả về danh sách coordinate key đã tồn tại.
     * Dùng CONCAT để tổng hợp thành cùng format với BinLocation.toCoordinateKey()
     */
    @Query("SELECT CONCAT(b.warehouseId, '|', b.zone, '|', b.aisle, '|', b.rack, '|', b.bin) " +
           "FROM BinLocationDbEntity b " +
           "WHERE b.warehouseId = :warehouseId " +
           "AND CONCAT(b.warehouseId, '|', b.zone, '|', b.aisle, '|', b.rack, '|', b.bin) IN :keys")
    List<String> findExistingCoordinateKeys(@Param("warehouseId") String warehouseId,
                                            @Param("keys") List<String> keys);
}
