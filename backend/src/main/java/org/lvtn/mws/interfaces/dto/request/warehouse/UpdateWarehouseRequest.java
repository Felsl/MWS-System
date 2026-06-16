package org.lvtn.mws.interfaces.dto.request.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWarehouseRequest {

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String address;
}
