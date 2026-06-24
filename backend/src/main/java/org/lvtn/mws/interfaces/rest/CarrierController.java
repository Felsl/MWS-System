package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.carrier.CreateCarrierUseCase;
import org.lvtn.mws.application.usecases.carrier.GetAllCarriersUseCase;
import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.interfaces.dto.request.carrier.CreateCarrierRequest;
import org.lvtn.mws.interfaces.dto.response.carrier.CarrierResponse;
import org.lvtn.mws.interfaces.mapper.CarrierWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CreateCarrierUseCase createCarrierUseCase;
    private final GetAllCarriersUseCase getAllCarriersUseCase;
    private final CarrierWebMapper webMapper;

    /** Tạo carrier mới (mặc định ACTIVE). */
    @PostMapping
    public ResponseEntity<CarrierResponse> create(@Valid @RequestBody CreateCarrierRequest request) {
        Carrier carrier = createCarrierUseCase.execute(
                request.getCode(),
                request.getName(),
                request.getShippingFeeRule(),
                Carrier.Status.ACTIVE);
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(carrier));
    }

    @GetMapping
    public ResponseEntity<List<CarrierResponse>> getAll() {
        return ResponseEntity.ok(webMapper.toResponseList(getAllCarriersUseCase.execute()));
    }
}
