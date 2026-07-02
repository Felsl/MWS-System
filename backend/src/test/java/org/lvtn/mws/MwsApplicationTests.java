package org.lvtn.mws;

import org.junit.jupiter.api.Test;
import org.lvtn.mws.integration.AbstractIntegrationTest;

/**
 * Smoke test: xác nhận toàn bộ ApplicationContext khởi động được trên MySQL thật
 * (kế thừa container singleton từ AbstractIntegrationTest).
 */
class MwsApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
