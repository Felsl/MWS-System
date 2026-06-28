package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.ISupplierRepository;

import java.util.List;

/** Quản lý nhà cung cấp (suppliers). Thuần Java. */
public class SupplierDomainService {

    private final ISupplierRepository repository;
    private final IIdGenerator idGenerator;

    public SupplierDomainService(ISupplierRepository repository, IIdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public Supplier create(String code, String name, String contactName,
                           String phone, String email, String address) {
        if (repository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã NCC đã tồn tại: " + code);
        }
        Supplier s = new Supplier.Builder()
                .id(idGenerator.generate())
                .code(code).name(name).contactName(contactName)
                .phone(phone).email(email).address(address)
                .status(Supplier.Status.ACTIVE)
                .build();
        return repository.save(s);
    }

    public List<Supplier> findAll() { return repository.findAll(); }

    public Supplier findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy NCC: " + id));
    }

    public Supplier update(String id, String name, String contactName,
                           String phone, String email, String address) {
        Supplier s = findById(id);
        s.update(name, contactName, phone, email, address);
        return repository.save(s);
    }

    public void delete(String id) {
        Supplier s = findById(id);
        s.delete();
        repository.save(s);
    }
}
