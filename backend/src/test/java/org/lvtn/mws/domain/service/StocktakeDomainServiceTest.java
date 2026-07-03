package org.lvtn.mws.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IStocktakeDetailRepository;
import org.lvtn.mws.domain.repository.IStocktakeSessionRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test TẦNG 2 (Mockito) cho phiên kiểm kê:
 * - chỉ hoàn tất khi mọi dòng đã kiểm đếm;
 * - chỉ nhập số đếm khi phiên còn FREEZED.
 */
@ExtendWith(MockitoExtension.class)
class StocktakeDomainServiceTest {

    @Mock IStocktakeSessionRepository sessionRepository;
    @Mock IStocktakeDetailRepository detailRepository;
    @Mock IInventoryBatchRepository batchRepository;
    @Mock IIdGenerator idGenerator;

    @InjectMocks StocktakeDomainService service;

    private StocktakeSession session(StocktakeSession.Status status) {
        return StocktakeSession.builder()
                .id("ST1").warehouseId("W1").status(status).createdBy("u1").build();
    }

    private StocktakeDetail detail(String id, Integer counted) {
        return StocktakeDetail.builder()
                .id(id).sessionId("ST1").productId("P1").binLocationId("BIN1").batchId("BATCH1").systemQuantity(10)
                .countedQuantity(counted).build();
    }

    @Test
    void completeWhenAllLinesCounted() {
        StocktakeSession s = session(StocktakeSession.Status.FREEZED);
        when(sessionRepository.findById("ST1")).thenReturn(Optional.of(s));
        when(detailRepository.findBySessionId("ST1"))
                .thenReturn(List.of(detail("D1", 8), detail("D2", 10)));
        when(sessionRepository.save(any(StocktakeSession.class))).thenAnswer(i -> i.getArgument(0));

        StocktakeSession result = service.completeStocktakeSession("ST1");

        assertThat(result.getStatus()).isEqualTo(StocktakeSession.Status.ADJUSTED);
    }

    @Test
    void completeRejectedWhenSomeLineUncounted() {
        StocktakeSession s = session(StocktakeSession.Status.FREEZED);
        when(sessionRepository.findById("ST1")).thenReturn(Optional.of(s));
        when(detailRepository.findBySessionId("ST1"))
                .thenReturn(List.of(detail("D1", 8), detail("D2", null))); // D2 chưa đếm

        assertThatThrownBy(() -> service.completeStocktakeSession("ST1"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(s.getStatus()).isEqualTo(StocktakeSession.Status.FREEZED);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void submitCountRejectedWhenSessionNotFrozen() {
        StocktakeDetail d = detail("D1", null);
        when(detailRepository.findById("D1")).thenReturn(Optional.of(d));
        when(sessionRepository.findById("ST1"))
                .thenReturn(Optional.of(session(StocktakeSession.Status.ADJUSTED))); // đã đóng

        assertThatThrownBy(() -> service.submitCountedQuantity("D1", 7, "u2"))
                .isInstanceOf(IllegalStateException.class);
        verify(detailRepository, never()).save(any());
    }

    @Test
    void submitCountPersistsWhenFrozen() {
        StocktakeDetail d = detail("D1", null);
        when(detailRepository.findById("D1")).thenReturn(Optional.of(d));
        when(sessionRepository.findById("ST1"))
                .thenReturn(Optional.of(session(StocktakeSession.Status.FREEZED)));
        when(detailRepository.save(any(StocktakeDetail.class))).thenAnswer(i -> i.getArgument(0));

        service.submitCountedQuantity("D1", 7, "u2");

        verify(detailRepository).save(d);
    }
}
