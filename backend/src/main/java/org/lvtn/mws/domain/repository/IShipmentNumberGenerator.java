package org.lvtn.mws.domain.repository;

/** Port sinh shipment_number hiển thị (vd SHP-yyyyMMdd-####). */
public interface IShipmentNumberGenerator {
    String next();
}
