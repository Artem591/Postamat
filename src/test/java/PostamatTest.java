import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Postamat Tests")
public class PostamatTest {

    private Postamat postamat;

    @BeforeEach
    void setUp() {
        postamat = new Postamat("TEST-PM", 5);
    }


    @Test
    @DisplayName("Constructor creates postamat with correct ID")
    void constructor_setsCorrectId() {
        assertEquals("TEST-PM", postamat.getId());
    }

    @Test
    @DisplayName("Constructor creates postamat with correct cell count")
    void constructor_setsCorrectCellCount() {
        assertEquals(5, postamat.getTotalCellsCount());
    }

    @Test
    @DisplayName("Constructor: all cells are free initially")
    void constructor_allCellsAreFreeInitially() {
        assertEquals(5, postamat.getFreeCellsCount());
    }

    @Test
    @DisplayName("Constructor throws exception for invalid cell count")
    void constructor_throwsExceptionForInvalidCellCount() {
        assertThrows(IllegalArgumentException.class, () -> new Postamat("BAD", 0));
        assertThrows(IllegalArgumentException.class, () -> new Postamat("BAD", -1));
    }


    @Test
    @DisplayName("placeOrder returns result when successful")
    void placeOrder_returnsResultWhenSuccessful() {
        Postamat.PlacementResult result = postamat.placeOrder("ORDER-1");

        assertNotNull(result);
        assertEquals(0, result.cellIndex);
        assertNotNull(result.pickupCode);
    }

    @Test
    @DisplayName("placeOrder decreases free cells count")
    void placeOrder_decreasesFreeCellsCount() {
        postamat.placeOrder("ORDER-1");

        assertEquals(4, postamat.getFreeCellsCount());
    }

    @Test
    @DisplayName("placeOrder returns null when no free cells")
    void placeOrder_returnsNullWhenNoFreeCells() {
        for (int i = 0; i < 5; i++) {
            assertNotNull(postamat.placeOrder("ORDER-" + i));
        }

        Postamat.PlacementResult result = postamat.placeOrder("ORDER-EXTRA");

        assertNull(result);
        assertEquals(0, postamat.getFreeCellsCount());
    }

    @Test
    @DisplayName("placeOrder returns null for null orderId")
    void placeOrder_returnsNullForNullOrderId() {
        Postamat.PlacementResult result = postamat.placeOrder(null);
        assertNull(result);
    }

    @Test
    @DisplayName("placeOrder returns null for empty orderId")
    void placeOrder_returnsNullForEmptyOrderId() {
        Postamat.PlacementResult result = postamat.placeOrder("");
        assertNull(result);

        result = postamat.placeOrder("   ");
        assertNull(result);
    }

    @Test
    @DisplayName("placeOrder places orders in different cells")
    void placeOrder_placesOrdersInDifferentCells() {
        Postamat.PlacementResult r1 = postamat.placeOrder("ORDER-1");
        Postamat.PlacementResult r2 = postamat.placeOrder("ORDER-2");
        Postamat.PlacementResult r3 = postamat.placeOrder("ORDER-3");

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);

        assertEquals(0, r1.cellIndex);
        assertEquals(1, r2.cellIndex);
        assertEquals(2, r3.cellIndex);
    }

    @Test
    @DisplayName("placeOrder generates different codes for different orders")
    void placeOrder_generatesDifferentCodes() {
        Postamat.PlacementResult r1 = postamat.placeOrder("ORDER-1");
        Postamat.PlacementResult r2 = postamat.placeOrder("ORDER-2");

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotEquals(r1.pickupCode, r2.pickupCode);
    }

    @Test
    @DisplayName("placeOrder pickup code has length 6")
    void placeOrder_pickupCodeHasLength6() {
        Postamat.PlacementResult result = postamat.placeOrder("ORDER-1");

        assertNotNull(result);
        assertEquals(6, result.pickupCode.length());
    }

    @Test
    @DisplayName("placeOrder pickup code contains only digits")
    void placeOrder_pickupCodeContainsOnlyDigits() {
        Postamat.PlacementResult result = postamat.placeOrder("ORDER-1");

        assertNotNull(result);
        assertTrue(result.pickupCode.matches("\\d{6}"));
    }

    @Test
    @DisplayName("pickupOrder returns message for correct code")
    void pickupOrder_returnsMessageForCorrectCode() {
        Postamat.PlacementResult placement = postamat.placeOrder("ORDER-123");

        String message = postamat.pickupOrder(placement.pickupCode);

        assertNotNull(message);
        assertTrue(message.contains("ORDER-123"));
        assertTrue(message.contains("ячейке 0"));
    }

    @Test
    @DisplayName("pickupOrder increases free cells count after pickup")
    void pickupOrder_increasesFreeCellsCountAfterPickup() {
        Postamat.PlacementResult placement = postamat.placeOrder("ORDER-1");

        assertEquals(4, postamat.getFreeCellsCount());

        postamat.pickupOrder(placement.pickupCode);

        assertEquals(5, postamat.getFreeCellsCount());
    }

    @Test
    @DisplayName("pickupOrder returns null for invalid code")
    void pickupOrder_returnsNullForInvalidCode() {
        postamat.placeOrder("ORDER-1");

        String message = postamat.pickupOrder("999999");

        assertNull(message);
    }

    @Test
    @DisplayName("pickupOrder returns null for null code")
    void pickupOrder_returnsNullForNullCode() {
        postamat.placeOrder("ORDER-1");

        String message = postamat.pickupOrder(null);

        assertNull(message);
    }

    @Test
    @DisplayName("pickupOrder returns null for empty code")
    void pickupOrder_returnsNullForEmptyCode() {
        postamat.placeOrder("ORDER-1");

        assertNull(postamat.pickupOrder(""));
        assertNull(postamat.pickupOrder("   "));
    }

    @Test
    @DisplayName("pickupOrder: order can be picked up only once")
    void pickupOrder_orderCanBePickedUpOnlyOnce() {
        Postamat.PlacementResult placement = postamat.placeOrder("ORDER-1");

        String message1 = postamat.pickupOrder(placement.pickupCode);
        assertNotNull(message1);

        String message2 = postamat.pickupOrder(placement.pickupCode);
        assertNull(message2);
    }

    @Test
    @DisplayName("pickupOrder: different orders can be picked up independently")
    void pickupOrder_differentOrdersCanBePickedUpIndependently() {
        Postamat.PlacementResult r1 = postamat.placeOrder("ORDER-1");
        Postamat.PlacementResult r2 = postamat.placeOrder("ORDER-2");

        String msg1 = postamat.pickupOrder(r1.pickupCode);
        assertNotNull(msg1);

        String msg2 = postamat.pickupOrder(r2.pickupCode);
        assertNotNull(msg2);

        assertNull(postamat.pickupOrder(r1.pickupCode));
    }

    @Test
    @DisplayName("hasFreeCells returns true when cells available")
    void hasFreeCells_returnsTrueWhenCellsAvailable() {
        assertTrue(postamat.hasFreeCells());

        postamat.placeOrder("ORDER-1");

        assertTrue(postamat.hasFreeCells());
    }

    @Test
    @DisplayName("hasFreeCells returns false when no cells available")
    void hasFreeCells_returnsFalseWhenNoCellsAvailable() {
        for (int i = 0; i < 5; i++) {
            postamat.placeOrder("ORDER-" + i);
        }

        assertFalse(postamat.hasFreeCells());
    }

    @Test
    @DisplayName("Integration test: full cycle - place and pickup")
    void integration_fullCycle_placeAndPickup() {
        Postamat.PlacementResult placement = postamat.placeOrder("ORDER-999");

        assertNotNull(placement);
        assertEquals(0, placement.cellIndex);
        assertEquals(4, postamat.getFreeCellsCount());

        String message = postamat.pickupOrder(placement.pickupCode);

        assertNotNull(message);
        assertTrue(message.contains("ORDER-999"));
        assertEquals(5, postamat.getFreeCellsCount());
    }

    @Test
    @DisplayName("Integration test: multiple orders placed and picked up")
    void integration_multipleOrders() {
        Postamat.PlacementResult r1 = postamat.placeOrder("ORDER-A");
        Postamat.PlacementResult r2 = postamat.placeOrder("ORDER-B");
        Postamat.PlacementResult r3 = postamat.placeOrder("ORDER-C");

        assertEquals(2, postamat.getFreeCellsCount());

        assertNotNull(postamat.pickupOrder(r2.pickupCode));
        assertEquals(3, postamat.getFreeCellsCount());

        assertNotNull(postamat.pickupOrder(r1.pickupCode));
        assertEquals(4, postamat.getFreeCellsCount());

        assertNotNull(postamat.pickupOrder(r3.pickupCode));
        assertEquals(5, postamat.getFreeCellsCount());
    }
}