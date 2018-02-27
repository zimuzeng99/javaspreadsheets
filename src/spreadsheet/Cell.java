package spreadsheet;

import common.api.CellLocation;
import common.api.ExpressionUtils;
import common.api.monitor.Tracker;
import common.api.value.DoubleValue;
import common.api.value.InvalidValue;
import common.api.value.StringValue;
import common.api.value.Value;

import java.util.HashSet;
import java.util.Set;

class Cell implements Tracker<Cell> {
  private final CellLocation location;
  private final Spreadsheet spreadsheet;
  private Value value = new DoubleValue(0.0);
  private String expression = "0.0";

  private Set<Cell> dependsOn = new HashSet<>();
  private Set<Tracker<Cell>> dependedBy = new HashSet<>();

  public Cell(CellLocation location, Spreadsheet spreadsheet) {
    this.location = location;
    this.spreadsheet = spreadsheet;
  }

  private void removeTracker(Tracker<Cell> tracker) {
    dependedBy.remove(tracker);
  }

  private void addTracker(Tracker<Cell> tracker) {
    dependedBy.add(tracker);
  }

  public CellLocation getLocation() {
    return location;
  }

  public String getExpression() {
    return expression;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }

  public void setExpression(String expression) {
    System.out.println(expression);
    for (Cell cell : dependsOn) {
      cell.removeTracker(this);
    }
    dependsOn.clear();

    this.expression = expression;
    value = new InvalidValue(expression);
    spreadsheet.invalidateCell(this);

    for (CellLocation location : ExpressionUtils.getReferencedLocations(expression)) {
      Cell cell = spreadsheet.getCell(location);
      dependsOn.add(cell);
      System.out.println("adding dependenci");
      cell.addTracker(this);
    }

    for (Tracker<Cell> tracker : dependedBy) {
      tracker.update(this);
    }
  }

  public void update(Cell cell) {
    if (!spreadsheet.needsRecomputing(this)) {
      spreadsheet.invalidateCell(this);
      value = new InvalidValue(expression);
      for (Tracker<Cell> tracker : dependedBy) {
        tracker.update(this);
      }
    }
  }

  public Set<Cell> getDependencies() {
    return new HashSet<>(dependsOn);
  }
}
