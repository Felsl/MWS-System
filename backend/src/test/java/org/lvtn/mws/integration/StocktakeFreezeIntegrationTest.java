package org.lvtn.mws.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.model.WarehouseFrozenException;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.lvtn.mws.infrastructure.freeze.StocktakeFreezeGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TẦNG 3 — ĐÓNG BĂNG KHO khi kiểm kê (WarehouseFreeze).
 *
 * <p>Khi mở phiên kiểm kê, kho chuyển trạng thái FREEZED. Chốt chặn nghiệp vụ
 * {@link StocktakeFreezeGuard#assertNotFrozen(String)} phải NÉM {@link WarehouseFrozenException}
 * cho kho đang đóng băng, và KHÔNG ảnh hưởng kho khác. Ngoài ra, không thể mở 2 phiên kiểm kê
 * chồng lên cùng một kho.
 *
 * <p>Test ở tầng domain/guard (không qua HTTP) vì đây mới là điểm thực thi đáng tin cậy —
 * interceptor tầng web chỉ là lớp chặn bổ sung.
 *
 * <p>{@code @Transactional}: mỗi test tự rollback. Yêu cầu Docker.
 */
@Transactional
class StocktakeFreezeIntegrationTest extends AbstractIntegrationTest {

    private static final String WH_A = "WH-FRZ-A";
    private static final String WH_B = "WH-FRZ-B";

    @Autowired StocktakeDomainService stocktakeService;
    @Autowired StocktakeFreezeGuard freezeGuard;
    @PersistenceContext EntityManager em;

    private void seedWarehouse(String id, String code) {
        em.createNativeQuery("INSERT INTO warehouses (id, code, name, address) VALUES (?,?,?,?)")
                .setParameter(1, id).setParameter(2, code)
                .setParameter(3, "Kho " + code).setParameter(4, "Địa chỉ " + code)
                .executeUpdate();
    }

    @Test
    @DisplayName("Mở kiểm kê -> kho bị đóng băng: guard chặn kho đó (409), kho khác vẫn thao tác được")
    void startStocktake_freezesOnlyThatWarehouse() {
        seedWarehouse(WH_A, "FRZ-A");
        seedWarehouse(WH_B, "FRZ-B");
        em.flush();

        // Trước khi kiểm kê: cả hai kho đều KHÔNG bị chặn.
        assertThatCode(() -> freezeGuard.assertNotFrozen(WH_A)).doesNotThrowAnyException();
        assertThatCode(() -> freezeGuard.assertNotFrozen(WH_B)).doesNotThrowAnyException();

        // Mở phiên kiểm kê cho kho A.
        StocktakeSession session = stocktakeService.startStocktake(WH_A, "auditor");
        assertThat(session.getStatus()).isEqualTo(StocktakeSession.Status.FREEZED);

        // Kho A bị đóng băng -> mọi biến động tồn bị chặn (WarehouseFrozenException).
        assertThatThrownBy(() -> freezeGuard.assertNotFrozen(WH_A))
                .isInstanceOf(WarehouseFrozenException.class);

        // Kho B KHÔNG bị ảnh hưởng.
        assertThatCode(() -> freezeGuard.assertNotFrozen(WH_B)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Không cho mở 2 phiên kiểm kê chồng nhau trên cùng một kho")
    void startStocktake_twiceSameWarehouse_isRejected() {
        seedWarehouse(WH_A, "FRZ-A2");
        em.flush();

        stocktakeService.startStocktake(WH_A, "auditor");

        assertThatThrownBy(() -> stocktakeService.startStocktake(WH_A, "auditor"))
                .isInstanceOf(IllegalStateException.class);
    }
}
