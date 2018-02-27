package spreadsheet;

import common.api.Tabular;
import common.gui.SpreadsheetGUI;

public class Main {

  private static final int DEFAULT_NUM_ROWS = 5000;
  private static final int DEFAULT_NUM_COLUMNS = 5000;

  public static void main(String[] args) {
    int noOfRows = DEFAULT_NUM_ROWS;
    int noOfColumns = DEFAULT_NUM_COLUMNS;

    if (args.length == 2) {
      noOfRows = Integer.parseInt(args[0]);
      noOfColumns = Integer.parseInt(args[1]);
    }

    Tabular spreadsheet = new Spreadsheet();
    SpreadsheetGUI gui = new SpreadsheetGUI(spreadsheet, noOfRows, noOfColumns);
    gui.start();
  }

}
