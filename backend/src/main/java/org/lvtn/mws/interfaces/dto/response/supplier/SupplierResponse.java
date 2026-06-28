package org.lvtn.mws.interfaces.dto.response.supplier;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SupplierResponse {
    private String id;
    private String code;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String status;
}
