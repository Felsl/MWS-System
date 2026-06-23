package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.domain.repository.IShipmentNumberGenerator;
import org.lvtn.mws.domain.repository.IShipmentRepository;

import java.util.List;

/**
 * Nghiệp vụ Vận đơn (luồng Sales Order). Thuần Java.
 * Việc khấu trừ tồn kho vật lý + ghi thẻ kho được thực hiện bởi listener của
 * ShipmentShippedEvent (giữ cùng một @Transactional để rollback an toàn).
 */
public class ShipmentDomainService {

    private final IShipmentRepository shipmentRepository;
    private final ISalesOrderRepository soRepository;
    private final IPickingListRepository pickingRepository;
    private final IIdGenerator idGenerator;
    private final IShipmentNumberGenerator shipmentNumberGenerator;

    public ShipmentDomainService(IShipmentRepository shipmentRepository,
                                 ISalesOrderRepository soRepository,
                                 IPickingListRepository pickingRepository,
                                 IIdGenerator idGenerator,
                                 IShipmentNumberGenerator shipmentNumberGenerator) {
        this.shipmentRepository      = shipmentRepository;
        this.soRepository            = soRepository;
        this.pickingRepository       = pickingRepository;
        this.idGenerator             = idGenerator;
        this.shipmentNumberGenerator = shipmentNumberGenerator;
    }

    /** Tạo vận đơn (PACKED) cho một SO đã gom hàng xong. */
    public Shipment createForSalesOrder(String salesOrderId, String carrierId) {
        SalesOrder so = soRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn bán hàng: " + salesOrderId));
        if (so.getStatus() != SalesOrder.Status.PICKING) {
            throw new IllegalStateException("Chỉ tạo vận đơn khi đơn ở trạng thái PICKING");
        }
        PickingList pl = pickingRepository.findBySoId(salesOrderId)
                .orElseThrow(() -> new IllegalStateException("Đơn chưa có lệnh gom hàng"));
        if (!pl.isCompleted()) {
            throw new IllegalStateException("Lệnh gom hàng chưa COMPLETED, chưa thể tạo vận đơn");
        }

        Shipment shipment = new Shipment.Builder()
                .id(idGenerator.generate())
                .shipmentNumber(shipmentNumberGenerator.next())
                .salesOrderId(salesOrderId)
                .carrierId(carrierId)
                .status(Shipment.Status.PACKED)
                .build();
        return shipmentRepository.save(shipment);
    }

    public Shipment assignTracking(String shipmentId, String carrierId, String trackingNumber) {
        Shipment s = getById(shipmentId);
        s.assignTracking(carrierId, trackingNumber);
        return shipmentRepository.save(s);
    }

    /**
     * PACKED/HANDED_OVER -> SHIPPING. Trả về Shipment để UseCase phát ShipmentShippedEvent.
     * Việc trừ kho thực hiện ở listener.
     */
    public Shipment ship(String shipmentId) {
        Shipment s = getById(shipmentId);
        s.ship();
        return shipmentRepository.save(s);
    }

    public Shipment deliver(String shipmentId) {
        Shipment s = getById(shipmentId);
        s.deliver();
        return shipmentRepository.save(s);
    }

    /** Danh sách toàn bộ vận đơn (đọc). */
    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }

    public Shipment getById(String id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vận đơn: " + id));
    }
}