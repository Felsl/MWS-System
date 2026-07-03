package org.lvtn.mws.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScopeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TẦNG 3 — Data-scope ĐỌC (A2) trên MySQL thật.
 * Kiểm chứng WarehouseScopeSpecs.restrict sinh đúng điều kiện
 * "AND warehouse_id IN (...)" khi WarehouseScopeContext có danh sách kho,
 * và trả toàn bộ khi context rỗng (ADMIN/toàn cục).
 *
 * @Transactional: mỗi test tự rollback → cách ly dữ liệu.
 */
@Transactional
class WarehouseScopeReadIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    ISalesOrderRepository soRepository;

    @AfterEach
    void clearScope() {
        WarehouseScopeContext.clear();
    }

    private SalesOrder so(String id, String soNumber, String warehouseId) {
        return new SalesOrder.Builder()
                .id(id).soNumber(soNumber).warehouseId(warehouseId).customerId("C1")
                .status(SalesOrder.Status.DRAFT).createdBy("u1")
                .build();
    }

    @Test
    @DisplayName("Context = [WH-1] chỉ trả đơn thuộc WH-1, ẩn WH-2")
    void scopedReturnsOnlyAllowedWarehouse() {
        soRepository.save(so("SOSCOPE01", "SO-SCOPE-A", "WH-1"));
        soRepository.save(so("SOSCOPE02", "SO-SCOPE-B", "WH-2"));

        WarehouseScopeContext.set(List.of("WH-1"));
        List<SalesOrder> result = soRepository.findAllScoped();

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(SalesOrder::getWarehouseId).containsOnly("WH-1");
        assertThat(result).extracting(SalesOrder::getSoNumber).contains("SO-SCOPE-A");
        assertThat(result).extracting(SalesOrder::getSoNumber).doesNotContain("SO-SCOPE-B");
    }

    @Test
    @DisplayName("Context rỗng (ADMIN) trả cả hai kho")
    void emptyContextReturnsAllWarehouses() {
        soRepository.save(so("SOSCOPE03", "SO-SCOPE-C", "WH-1"));
        soRepository.save(so("SOSCOPE04", "SO-SCOPE-D", "WH-2"));

        WarehouseScopeContext.set(List.of()); // toàn cục
        List<SalesOrder> result = soRepository.findAllScoped();

        assertThat(result).extracting(SalesOrder::getSoNumber)
                .contains("SO-SCOPE-C", "SO-SCOPE-D");
    }
}
