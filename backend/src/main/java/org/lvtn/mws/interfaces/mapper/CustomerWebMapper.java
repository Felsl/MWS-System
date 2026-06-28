package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.interfaces.dto.response.customer.CustomerResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerWebMapper {
    public CustomerResponse toResponse(Customer c) {
        if (c == null) return null;
        return CustomerResponse.builder()
                .id(c.getId()).code(c.getCode()).name(c.getName())
                .taxCode(c.getTaxCode()).phone(c.getPhone())
                .email(c.getEmail()).address(c.getAddress())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .build();
    }
    public List<CustomerResponse> toResponseList(List<Customer> list) {
        List<CustomerResponse> out = new ArrayList<>();
        if (list != null) for (Customer c : list) out.add(toResponse(c));
        return out;
    }
}
