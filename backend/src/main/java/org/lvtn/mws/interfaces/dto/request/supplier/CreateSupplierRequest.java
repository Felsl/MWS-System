package org.lvtn.mws.interfaces.dto.request.supplier;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSupplierRequest {
    @NotBlank(message = "code không được trống")
    private String code;
    @NotBlank(message = "name không được trống")
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
}
