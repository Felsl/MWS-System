package org.lvtn.mws.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StocktakeSessionTest {

    private StocktakeSession session(StocktakeSession.Status status) {
        return StocktakeSession.builder()
                .id("ST1").warehouseId("W1").status(status).createdBy("u1")
                .build();
    }

    @Test
    void completeFromFrozenSetsAdjusted() {
        StocktakeSession s = session(StocktakeSession.Status.FREEZED);
        s.complete();
        assertThat(s.getStatus()).isEqualTo(StocktakeSession.Status.ADJUSTED);
        assertThat(s.getFreezeEndedAt()).isNotNull();
    }

    @Test
    void completeFromNonFrozenThrows() {
        assertThatThrownBy(() -> session(StocktakeSession.Status.OPEN).complete())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isFrozen() {
        assertThat(session(StocktakeSession.Status.FREEZED).isFrozen()).isTrue();
        assertThat(session(StocktakeSession.Status.OPEN).isFrozen()).isFalse();
    }
}
