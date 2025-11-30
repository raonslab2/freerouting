package app.freerouting.gui;

import app.freerouting.interactive.GuiBoardManager;
import app.freerouting.rules.Net;
import app.freerouting.rules.Nets;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple net inspector with filter for unrouted nets and click-to-highlight/zoom behavior.
 */
public class NetInspectorPanel extends JPanel
{
  private final GuiBoardManager boardManager;
  private final JTable table;
  private final NetTableModel model;
  private final JCheckBox unroutedOnly;
  private final JTextField searchField;

  public NetInspectorPanel(GuiBoardManager boardManager)
  {
    super(new BorderLayout(6, 6));
    this.boardManager = boardManager;
    setBorder(new EmptyBorder(8, 8, 8, 8));

    unroutedOnly = new JCheckBox("Unrouted only");
    searchField = new JTextField();
    searchField.setToolTipText("Filter nets by name");

    JPanel controls = new JPanel(new BorderLayout(6, 0));
    controls.add(unroutedOnly, BorderLayout.WEST);
    controls.add(searchField, BorderLayout.CENTER);
    add(controls, BorderLayout.NORTH);

    model = new NetTableModel();
    table = new JTable(model);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setFillsViewportHeight(true);
    table.getSelectionModel().addListSelectionListener(e ->
    {
      int row = table.getSelectedRow();
      if (row >= 0)
      {
        NetRow netRow = model.getRow(table.convertRowIndexToModel(row));
        focusNet(netRow.netNumber);
      }
    });

    // Refresh model when toggles change
    unroutedOnly.addActionListener(e -> refresh());
    searchField.addActionListener(e -> refresh());

    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  public void showUnroutedOnly()
  {
    unroutedOnly.setSelected(true);
    refresh();
  }

  public void refresh()
  {
    List<NetRow> rows = boardManager.get_routing_board() != null ? buildRows() : Collections.emptyList();
    model.setRows(rows);
  }

  private List<NetRow> buildRows()
  {
    boolean filterUnrouted = unroutedOnly.isSelected();
    String query = searchField.getText() != null ? searchField
        .getText()
        .trim()
        .toLowerCase() : "";

    Nets nets = boardManager.get_routing_board().rules.nets;
    List<NetRow> rows = new ArrayList<>();
    for (int i = 1; i <= nets.max_net_no(); i++)
    {
      Net net = nets.get(i);
      if (net == null)
      {
        continue;
      }
      boolean unrouted = boardManager.get_ratsnest() != null && boardManager.get_ratsnest().incomplete_count(net.net_number) > 0;
      rows.add(new NetRow(net, unrouted));
    }

    return rows.stream()
        .filter(r -> query.isEmpty() || r.name.toLowerCase().contains(query))
        .filter(r -> !filterUnrouted || r.unrouted)
        .sorted(Comparator.comparing((NetRow r) -> !r.unrouted) // unrouted first
            .thenComparing(r -> r.name))
        .collect(Collectors.toList());
  }

  private void focusNet(int netNumber)
  {
    // Apply ratsnest filter for the net and refresh view.
    boardManager.set_incompletes_filter(netNumber, true);
    boardManager.repaint();
  }

  private static class NetRow
  {
    final int netNumber;
    final String name;
    final boolean unrouted;

    NetRow(Net net, boolean unrouted)
    {
      this.netNumber = net.net_number;
      this.name = net.name;
      this.unrouted = unrouted;
    }
  }

  private static class NetTableModel extends AbstractTableModel
  {
    private final String[] columns = new String[] { "Name", "Status" };
    private List<NetRow> rows = Collections.emptyList();

    @Override
    public int getRowCount()
    {
      return rows.size();
    }

    @Override
    public int getColumnCount()
    {
      return columns.length;
    }

    @Override
    public String getColumnName(int column)
    {
      return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      NetRow row = rows.get(rowIndex);
      switch (columnIndex)
      {
        case 0:
          return row.name;
        case 1:
          return row.unrouted ? "Unrouted" : "Routed";
        default:
          return "";
      }
    }

    NetRow getRow(int modelRow)
    {
      return rows.get(modelRow);
    }

    void setRows(List<NetRow> rows)
    {
      this.rows = rows;
      fireTableDataChanged();
    }
  }
}
