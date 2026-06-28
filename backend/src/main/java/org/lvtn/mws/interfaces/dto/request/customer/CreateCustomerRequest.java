package org.lvtn.mws.interfaces.dto.request.customer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCustomerRequest {
    @NotBlank(message = "code không được trống")
    private String code;
    @NotBlank(message = "name không được trống")
    private String name;
    private String taxCode;
    private String phone;
    private String email;
    private String address;
}
