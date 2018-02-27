package spreadsheet;

import common.api.CellLocation;
import common.api.ExpressionUtils;
import common.api.Tabular;
import common.api.value.LoopValue;
import common.api.value.StringValue;
import common.api.value.Value;
import common.api.value.ValueEvaluator;

import java.util.*;

public class Spreadsheet implements Tabular {
  private final Map<CellLocation, Cell> cells = new HashMap<>();
  private final Deque<Cell> invalidCells = new ArrayDeque<>();

  public void setExpression(CellLocation location, String expression) {
    Cell cell = null;
    if (cells.containsKey(location)) {
      cell = cells.get(location);
    }
    else {
      cell = new Cell(location, this);
      cells.put(location, cell);
    }

    for (CellLocation cl : ExpressionUtils.getReferencedLocations(expression)) {
      if (!cells.containsKey(cl)) {
        cells.put(cl, new Cell(cl, this));
      }
    }

    cell.setExpression(expression);
  }

  public Cell getCell(CellLocation location) {
    return cells.get(location);
  }

  public void invalidateCell(Cell cell) {
    if (!invalidCells.contains(cell)) {
      invalidCells.addLast(cell);
    }
  }

  public boolean needsRecomputing(Cell cell) {
    return invalidCells.contains(cell);
  }

  public String getExpression(CellLocation location) {
    if (cells.containsKey(location)) {
      return cells.get(location).getExpression();
    }
    else {
      return "";
    }
  }

  public Value getValue(CellLocation location) {
    if (cells.containsKey(location)) {
      return cells.get(location).getValue();
    }
    else {
      return null;
    }
  }

  public void recompute() {
    while (!invalidCells.isEmpty()) {
      Cell cell = invalidCells.getFirst();
      recomputeCell(cell);
    }
  }

  private void recomputeCell(Cell c) {
    checkLoops(c, new LinkedHashSet<>());
    if (invalidCells.contains(c)) {
      Deque<Cell> queue = new ArrayDeque<>();
      queue.add(c);
      while (!queue.isEmpty()) {
        Cell current = queue.removeFirst();
        for (Cell dependentCell : current.getDependencies()) {
          if (invalidCells.contains(dependentCell)) {
            queue.addFirst(dependentCell);
            queue.addLast(current);
          }
        }
        if (!queue.contains(current)) {
          calculateCellValue(current);
          invalidCells.remove(current);
        }
      }
    }
  }

  private void checkLoops(Cell c, LinkedHashSet<Cell> cellsSeen) {
    if (cellsSeen.contains(c)) {
      markAsValidatedLoop(c, cellsSeen);
    }
    else {
      cellsSeen.add(c);
      for (Cell cell : c.getDependencies()) {
        checkLoops(cell, cellsSeen);
      }
      cellsSeen.remove(c);
    }
  }

  private void markAsValidatedLoop(Cell startCell, LinkedHashSet<Cell> rememberedCells) {
    boolean setLoop = false;
    for (Cell cell : rememberedCells) {
      if (cell.equals(startCell)) {
        setLoop = true;
      }
      invalidCells.remove(cell);
      if (setLoop) {
        cell.setValue(LoopValue.INSTANCE);
      }
    }
  }

  private void calculateCellValue(Cell cell) {
    Map<CellLocation, Double> dependentValues = new HashMap<>();

    for (Cell c : cell.getDependencies()) {
      ValueEvaluator evaluator = new ValueEvaluator() {
        @Override
        public void evaluateDouble(double value) {
          dependentValues.put(c.getLocation(), value);
        }

        @Override
        public void evaluateLoop() {

        }

        @Override
        public void evaluateString(String expression) {

        }

        @Override
        public void evaluateInvalid(String expression) {

        }
      };

      c.getValue().evaluate(evaluator);
    }

    cell.setValue(ExpressionUtils.computeValue(cell.getExpression(), dependentValues));
    System.out.println(ExpressionUtils.computeValue(cell.getExpression(), dependentValues));
  }
}
