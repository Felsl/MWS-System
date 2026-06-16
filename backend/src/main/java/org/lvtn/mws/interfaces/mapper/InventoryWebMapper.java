package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.BatchSuggestion;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.interfaces.dto.response.inventory.BatchSuggestionResponse;
import org.lvtn.mws.interfaces.dto.response.inventory.InventoryBatchResponse;
import org.lvtn.mws.interfaces.dto.response.inventory.InventoryResponse;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryWebMapper {

    default InventoryResponse toResponse(Inventory domain) {
        if (domain == null) return null;
        return InventoryResponse.builder()
                .productId(domain.getProductId())
                .warehouseId(domain.getWarehouseId())
                .quantity(domain.getQuantity())
                .reservedQuantity(domain.getReservedQuantity())
                .availableQuantity(domain.availableQuantity())
                .build();
    }

    default List<InventoryResponse> toResponseList(List<Inventory> domains) {
        if (domains == null) return List.of();
        return domains.stream().map(this::toResponse).toList();
    }

    default InventoryBatchResponse toBatchResponse(InventoryBatch domain) {
        if (domain == null) return null;
        return InventoryBatchResponse.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .warehouseId(domain.getWarehouseId())
                .binLocationId(domain.getBinLocationId())
                .batchNumber(domain.getBatchNumber())
                .quantity(domain.getQuantity())
                .expiryDate(domain.getExpiryDate())
                .manufacturedDate(domain.getManufacturedDate())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    default List<InventoryBatchResponse> toBatchResponseList(List<InventoryBatch> domains) {
        if (domains == null) return List.of();
        return domains.stream().map(this::toBatchResponse).toList();
    }

    default BatchSuggestionResponse toSuggestionResponse(BatchSuggestion domain) {
        if (domain == null) return null;
        return BatchSuggestionResponse.builder()
                .batchId(domain.getBatchId())
                .batchNumber(domain.getBatchNumber())
                .binLocationId(domain.getBinLocationId())
                .suggestedQuantity(domain.getSuggestedQuantity())
                .build();
    }

    default List<BatchSuggestionResponse> toSuggestionResponseList(List<BatchSuggestion> domains) {
        if (domains == null) return List.of();
        return domains.stream().map(this::toSuggestionResponse).toList();
    }
}
