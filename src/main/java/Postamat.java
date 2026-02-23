import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Postamat {

    private final String id;
    private final Cell[] cells;
    private final Map<String, Integer> pickupCodeToCellIndex = new HashMap<>();
    private final Random random = new Random();
    private final UserNotificationApi notificationApi;

    private static class Cell {
        public final String orderId;
        public final String pickupCode;

        private Cell(String orderId, String pickupCode) {
            this.orderId = orderId;
            this.pickupCode = pickupCode;
        }

        boolean isEmpty() {
            return orderId == null;
        }

        static Cell createEmpty() {
            return new Cell(null, null);
        }

        static Cell createFilled(String orderId, String pickupCode) {
            return new Cell(orderId, pickupCode);
        }
    }

    public static class PlacementResult {
        public final int cellIndex;

        public PlacementResult(int cellIndex) {
            this.cellIndex = cellIndex;
        }
    }

    public Postamat(String id, int numCells, UserNotificationApi notificationApi) {
        if (numCells <= 0) {
            throw new IllegalArgumentException("Number of cells must be positive");
        }
        this.id = id;
        this.notificationApi = notificationApi;
        this.cells = new Cell[numCells];
        for (int i = 0; i < numCells; i++) {
            cells[i] = Cell.createEmpty();
        }
    }

    public String getId() {
        return id;
    }

    public PlacementResult placeOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID cannot be null or empty");
        }

        if (isOrderAlreadyPlaced(orderId)) {
            throw new OrderAlreadyPlacedException("Order with ID " + orderId + " is already placed");
        }

        for (int i = 0; i < cells.length; i++) {
            if (cells[i].isEmpty()) {
                String code = generatePickupCode();
                cells[i] = Cell.createFilled(orderId, code);
                pickupCodeToCellIndex.put(code, i);
                notificationApi.sendPickupCode(orderId, code);
                return new PlacementResult(i);
            }
        }

        throw new NoFreeCellsException("No free cells available");
    }

    private String generatePickupCode() {
        int code = random.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    public String pickupOrder(String pickupCode) {
        if (pickupCode == null || pickupCode.trim().isEmpty()) {
            throw new InvalidPickupCodeException("Pickup code cannot be null or empty");
        }

        Integer cellIndex = pickupCodeToCellIndex.get(pickupCode);
        if (cellIndex == null) {
            throw new InvalidPickupCodeException("Invalid pickup code: " + pickupCode);
        }

        Cell cell = cells[cellIndex];
        if (cell == null || cell.isEmpty()) {
            pickupCodeToCellIndex.remove(pickupCode);
            throw new InvalidPickupCodeException("Cell is empty for code: " + pickupCode);
        }

        String message = "ваш заказ " + cell.orderId + " в ячейке " + cellIndex;
        openCell(cellIndex);

        cells[cellIndex] = Cell.createEmpty();
        pickupCodeToCellIndex.remove(pickupCode);

        return message;
    }

    private void openCell(int cellIndex) {
    }

    private boolean isOrderAlreadyPlaced(String orderId) {
        for (Cell cell : cells) {
            if (!cell.isEmpty() && cell.orderId.equals(orderId)) {
                return true;
            }
        }
        return false;
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public static class OrderAlreadyPlacedException extends RuntimeException {
        public OrderAlreadyPlacedException(String message) {
            super(message);
        }
    }

    public static class NoFreeCellsException extends RuntimeException {
        public NoFreeCellsException(String message) {
            super(message);
        }
    }

    public static class InvalidPickupCodeException extends RuntimeException {
        public InvalidPickupCodeException(String message) {
            super(message);
        }
    }
}