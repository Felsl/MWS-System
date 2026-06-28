package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.domain.repository.ICustomerRepository;
import org.lvtn.mws.domain.repository.IIdGenerator;

import java.time.LocalDateTime;
import java.util.List;

/** Quản lý khách hàng (customers). Thuần Java. */
public class CustomerDomainService {

    private final ICustomerRepository repository;
    private final IIdGenerator idGenerator;

    public CustomerDomainService(ICustomerRepository repository, IIdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public Customer create(String code, String name, String taxCode,
                           String phone, String email, String address) {
        if (repository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã KH đã tồn tại: " + code);
        }
        Customer c = new Customer.Builder()
                .id(idGenerator.generate())
                .code(code).name(name).taxCode(taxCode)
                .phone(phone).email(email).address(address)
                .status(Customer.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        return repository.save(c);
    }

    public List<Customer> findAll() { return repository.findAll(); }

    public Customer findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy KH: " + id));
    }

    public Customer update(String id, String name, String taxCode,
                           String phone, String email, String address) {
        Customer c = findById(id);
        c.update(name, taxCode, phone, email, address);
        return repository.save(c);
    }

    public void delete(String id) {
        Customer c = findById(id);
        c.delete();
        repository.save(c);
    }
}
