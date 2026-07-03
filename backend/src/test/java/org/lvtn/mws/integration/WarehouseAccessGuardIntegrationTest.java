package org.lvtn.mws.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.repository.IUserWarehouseAccessRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TẦNG 3 — Data-scope GHI (A2): WarehouseAccessGuard chặn thao tác chéo kho (HTTP 403)
 * trên MySQL thật. Guard đọc user đăng nhập từ SecurityContext rồi truy vấn
 * user_warehouse_access (join bảng users).
 *
 * @Transactional: mỗi test tự rollback.
 */
@Transactional
class WarehouseAccessGuardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    WarehouseAccessGuard guard;
    @Autowired
    IUserWarehouseAccessRepository accessRepository;
    @PersistenceContext
    EntityManager em;

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    /** Chèn 1 user thật (query của guard join bảng users theo username). */
    private void seedUser(String id, String username) {
        em.createNativeQuery(
                "INSERT INTO users (id, username, password, full_name, role_id) " +
                "VALUES (?, ?, ?, ?, ?)")
                .setParameter(1, id)
                .setParameter(2, username)
                .setParameter(3, "x")
                .setParameter(4, "Test " + username)
                .setParameter(5, "R1")
                .executeUpdate();
        em.flush();
    }

    private void login(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList()));
    }

    @Test
    @DisplayName("Thủ kho được giao WH-1: thao tác WH-1 OK, thao tác WH-2 bị chặn 403")
    void storekeeperBlockedOnForeignWarehouse() {
        seedUser("USRGUARD01", "storekeeper1");
        accessRepository.assignWarehouses("USRGUARD01", List.of("WH-1"));
        em.flush();
        login("storekeeper1");

        assertThatCode(() -> guard.check("WH-1")).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.check("WH-2"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("User toàn cục (không có bản ghi access) được thao tác mọi kho")
    void globalUserAllowedEverywhere() {
        seedUser("USRGUARD02", "admin1"); // không gán kho nào
        login("admin1");

        assertThatCode(() -> guard.check("WH-1")).doesNotThrowAnyException();
        assertThatCode(() -> guard.check("WH-9")).doesNotThrowAnyException();
    }
}
