package com.example.inventory_service.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryItemTest {
    @Test
    void reserveConfirmAndReleaseKeepStockInvariant() {
        InventoryItem item = InventoryItem.builder().onHand(10).reserved(0).build();

        item.reserve(4);
        assertEquals(6, item.available());

        item.confirm(3);
        assertEquals(7, item.getOnHand());
        assertEquals(1, item.getReserved());

        item.release(1);
        assertEquals(0, item.getReserved());
        assertEquals(7, item.available());
    }

    @Test
    void cannotReserveMoreThanAvailable() {
        InventoryItem item = InventoryItem.builder().onHand(1).reserved(0).build();
        assertThrows(IllegalStateException.class, () -> item.reserve(2));
    }
}
