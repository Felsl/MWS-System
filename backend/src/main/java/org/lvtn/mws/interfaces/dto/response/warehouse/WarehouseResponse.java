package org.lvtn.mws.interfaces.dto.response.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {
    private String        id;
    private String        code;
    private String        name;
    private String        address;
    private String        status;
    private LocalDateTime createdAt;
}
