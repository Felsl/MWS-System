package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.interfaces.dto.response.supplier.SupplierResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SupplierWebMapper {
    public SupplierResponse toResponse(Supplier s) {
        if (s == null) return null;
        return SupplierResponse.builder()
                .id(s.getId()).code(s.getCode()).name(s.getName())
                .contactName(s.getContactName()).phone(s.getPhone())
                .email(s.getEmail()).address(s.getAddress())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .build();
    }
    public List<SupplierResponse> toResponseList(List<Supplier> list) {
        List<SupplierResponse> out = new ArrayList<>();
        if (list != null) for (Supplier s : list) out.add(toResponse(s));
        return out;
    }
}
