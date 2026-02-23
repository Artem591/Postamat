import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Postamat Tests")
public class PostamatTest {

    @Mock
    private UserNotificationApi notificationApi;

    private Postamat postamat;

    @BeforeEach
    void setUp() {
        postamat = new Postamat("TEST-PM", 5, notificationApi);
    }

    @Test
    @DisplayName("Constructor creates postamat with correct ID")
    void constructor_setsCorrectId() {
        assertEquals("TEST-PM", postamat.getId());
    }

    @Test
    @DisplayName("placeOrder returns result when successful")
    void placeOrder_returnsResultWhenSuccessful() {
        Postamat.PlacementResult result = postamat.placeOrder("ORDER-1");
        assertNotNull(result);
        assertEquals(0, result.cellIndex);
    }

    @Test
    @DisplayName("pickupOrder returns message for correct code")
    void pickupOrder_returnsMessageForCorrectCode() {
        // Размещаем заказ
        Postamat.PlacementResult placement = postamat.placeOrder("ORDER-123");

        // Получаем код выдачи из мок-объекта
        String pickupCode = null;
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationApi).sendPickupCode(eq("ORDER-123"), codeCaptor.capture());
        pickupCode = codeCaptor.getValue();

        String message = postamat.pickupOrder(pickupCode);
        assertNotNull(message);
        assertTrue(message.contains("ORDER-123"));
        assertTrue(message.contains("ячейке 0"));
    }

    @Test
    @DisplayName("Integration test: full cycle - place and pickup")
    void integration_fullCycle_placeAndPickup() {
        // Курьер приносит заказ
        Postamat.PlacementResult placement = postamat.placeOrder("ORDER-999");
        assertNotNull(placement);
        assertEquals(0, placement.cellIndex);

        // Получаем код выдачи
        String pickupCode = null;
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationApi).sendPickupCode(eq("ORDER-999"), codeCaptor.capture());
        pickupCode = codeCaptor.getValue();

        // Пользователь получает заказ
        String message = postamat.pickupOrder(pickupCode);
        assertNotNull(message);
        assertTrue(message.contains("ORDER-999"));
    }
}