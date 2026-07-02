package org.lvtn.mws.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Lớp nền cho test tích hợp: dùng MỘT container MySQL thật theo mẫu "singleton".
 *
 * <p>Vì sao KHÔNG dùng @Testcontainers/@Container: annotation đó gắn vòng đời
 * container theo từng test-class (tắt sau mỗi class). Nhưng Spring cache lại
 * ApplicationContext để tái sử dụng giữa các class → sang class thứ hai, container
 * cũ đã tắt trong khi connection pool vẫn trỏ vào nó ⇒ "connection refused".
 *
 * <p>Mẫu singleton: tự start() container MỘT lần trong static block, không bao giờ
 * stop() thủ công — Testcontainers Ryuk sẽ dọn khi JVM thoát. Nhờ vậy datasource
 * mà context đã cache luôn còn hiệu lực xuyên suốt mọi test-class.
 *
 * <p>Yêu cầu: máy chạy test phải có Docker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractIntegrationTest {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }
}
