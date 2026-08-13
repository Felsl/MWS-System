package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.salesorder.GetOpenDemandsUseCase;
import org.lvtn.mws.interfaces.dto.response.salesorder.StockDemandResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [Bán vượt tồn] Nhu cầu nhập (backorder) — dành cho bộ phận mua hàng (INBOUND_CREATE_PO).
 * Dùng ở card "Nhu cầu chưa đủ hàng" trên Dashboard.
 */
@RestController
@RequestMapping("/api/v1/stock-demands")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INBOUND_CREATE_PO')")
public class StockDemandController {

    private final GetOpenDemandsUseCase getOpenDemandsUseCase;

    @GetMapping
    public List<StockDemandResponse> open() {
        return getOpenDemandsUseCase.execute();
    }
}
