import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Postamat {

    private final String id;
    private final Cell[] cells;
    private final Map<String, Integer> pickupCodeToCellIndex = new HashMap<>();
    private final Random random = new Random();

    private static class Cell {
        final String orderId;
        final String pickupCode;

        Cell(String orderId, String pickupCode) {
            this.orderId = orderId;
            this.pickupCode = pickupCode;
        }
    }

    public static class PlacementResult {
        public final int cellIndex;
        public final String pickupCode;

        public PlacementResult(int cellIndex, String pickupCode) {
            this.cellIndex = cellIndex;
            this.pickupCode = pickupCode;
        }

        @Override
        public String toString() {
            return "PlacementResult{cellIndex=" + cellIndex + ", pickupCode='" + pickupCode + "'}";
        }
    }

    public Postamat(String id, int numCells) {
        if (numCells <= 0) {
            throw new IllegalArgumentException("Number of cells must be positive");
        }
        this.id = id;
        this.cells = new Cell[numCells];
    }

    public PlacementResult placeOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return null;
        }

        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == null) {
                String code = generatePickupCode();
                cells[i] = new Cell(orderId, code);
                pickupCodeToCellIndex.put(code, i);
                sendSms(orderId, code);
                return new PlacementResult(i, code);
            }
        }
        return null;
    }

    private String generatePickupCode() {
        int code = random.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    private void sendSms(String orderId, String pickupCode) {
        System.out.println("[SMS] Order " + orderId + ": Your pickup code is " + pickupCode);
    }

    public String pickupOrder(String pickupCode) {
        if (pickupCode == null || pickupCode.trim().isEmpty()) {
            return null;
        }

        Integer cellIndex = pickupCodeToCellIndex.get(pickupCode);
        if (cellIndex == null) {
            return null;
        }

        Cell cell = cells[cellIndex];
        if (cell == null) {
            pickupCodeToCellIndex.remove(pickupCode);
            return null;
        }

        String message = "ваш заказ " + cell.orderId + " в ячейке " + cellIndex;
        openCell(cellIndex);

        cells[cellIndex] = null;
        pickupCodeToCellIndex.remove(pickupCode);

        return message;
    }

    private void openCell(int cellIndex) {
        System.out.println("[POSTAMAT " + id + "] Opening cell #" + cellIndex);
    }

    public String getId() {
        return id;
    }

    public int getFreeCellsCount() {
        int count = 0;
        for (Cell cell : cells) {
            if (cell == null) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCellsCount() {
        return cells.length;
    }

    public boolean hasFreeCells() {
        return getFreeCellsCount() > 0;
    }
}