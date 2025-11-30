package app.freerouting.gui;

import app.freerouting.management.TextManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The `BoardPanelStatus` class represents a status bar at the lower border of the board frame.
 * It contains components such as message lines, current layer indicator, and cursor position.
 */
class BoardPanelStatus extends JPanel
{
  public final JLabel errorLabel;
  public final JLabel warningLabel;
  public final JLabel statusMessage;
  public final JLabel additionalMessage;
  public final JLabel currentLayer;
  public final JLabel currentBoardScore;
  public final JLabel mousePosition;
  public final JLabel unitLabel;
  public final JLabel modeLabel;
  public final JLabel selectionLabel;
  public final JLabel drcLabel;
  // An icon for errors and warnings
  private final JPanel errorsWarningsPanel;
  private final JLabel errorIcon;
  private final JLabel warningIcon;
  // List to hold the listeners for error or warning label clicks
  private final List<ErrorOrWarningLabelClickedListener> errorOrWarningLabelClickedListeners = new ArrayList<>();

  /**
   * Creates a new instance of the `BoardPanelStatus` class.
   *
   * @param locale the locale to use for resource bundles
   */
  BoardPanelStatus(Locale locale)
  {
    TextManager tm = new TextManager(this.getClass(), locale);

    setLayout(new BorderLayout(6, 0));

    // Left cluster: mode + errors/warnings + status
    errorsWarningsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));

    modeLabel = new JLabel("Mode: Select");
    modeLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
    errorsWarningsPanel.add(modeLabel);

    warningIcon = new JLabel();
    tm.setText(warningIcon, "{{icon:alert}}");
    errorIcon = new JLabel();
    tm.setText(errorIcon, "{{icon:close-octagon}}");
    warningLabel = new JLabel("0", SwingConstants.LEADING);
    errorLabel = new JLabel("0", SwingConstants.LEADING);
    warningLabel.setBorder(new EmptyBorder(0, 0, 0, 6));
    errorLabel.setBorder(new EmptyBorder(0, 0, 0, 6));
    errorsWarningsPanel.add(errorIcon);
    errorsWarningsPanel.add(errorLabel);
    errorsWarningsPanel.add(warningIcon);
    errorsWarningsPanel.add(warningLabel);

    statusMessage = new JLabel();
    statusMessage.setHorizontalAlignment(SwingConstants.LEFT);
    tm.setText(statusMessage, "status_line");
    errorsWarningsPanel.add(statusMessage);

    additionalMessage = new JLabel();
    tm.setText(additionalMessage, "additional_text_field");
    additionalMessage.setMaximumSize(new Dimension(300, 14));
    additionalMessage.setMinimumSize(new Dimension(140, 14));
    additionalMessage.setPreferredSize(new Dimension(180, 14));
    errorsWarningsPanel.add(additionalMessage);

    add(errorsWarningsPanel, BorderLayout.WEST);

    // Center cluster: selection + DRC + score/unrouted
    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    selectionLabel = new JLabel("Selection: None");
    drcLabel = new JLabel("DRC: 0");
    currentBoardScore = new JLabel("Score: -");
    centerPanel.add(selectionLabel);
    centerPanel.add(drcLabel);
    centerPanel.add(currentBoardScore);
    add(centerPanel, BorderLayout.CENTER);

    // Right cluster: layer + coords + unit
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    currentLayer = new JLabel();
    tm.setText(currentLayer, "current_layer");
    rightPanel.add(currentLayer);

    mousePosition = new JLabel();
    mousePosition.setText("X 0.00   Y 0.00");
    mousePosition.setMaximumSize(new Dimension(170, 14));
    mousePosition.setPreferredSize(new Dimension(170, 14));
    rightPanel.add(mousePosition);

    unitLabel = new JLabel();
    unitLabel.setHorizontalAlignment(SwingConstants.CENTER);
    unitLabel.setText("unit");
    unitLabel.setMaximumSize(new Dimension(100, 14));
    unitLabel.setMinimumSize(new Dimension(50, 14));
    unitLabel.setPreferredSize(new Dimension(50, 14));
    rightPanel.add(unitLabel);

    add(rightPanel, BorderLayout.EAST);

    // Register click handlers after labels are constructed.
    addErrorOrWarningLabelClickedListener();
  }

  /**
   * Adds mouse listeners for error and warning labels to handle click events.
   */
  private void addErrorOrWarningLabelClickedListener()
  {
    // Raise an event if the user clicks on the error or warning label
    MouseAdapter clickHandler = new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        raiseErrorOrWarningLabelClickedEvent();
      }
    };
    errorsWarningsPanel.addMouseListener(clickHandler);
    errorLabel.addMouseListener(clickHandler);
    warningLabel.addMouseListener(clickHandler);
    drcLabel.addMouseListener(clickHandler);

    // Change the mouse cursor to a hand when hovering over these labels
    errorsWarningsPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    errorLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    warningLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    drcLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  /**
   * Raises the `ErrorOrWarningLabelClicked` event for all registered listeners.
   */
  private void raiseErrorOrWarningLabelClickedEvent()
  {
    for (ErrorOrWarningLabelClickedListener listener : errorOrWarningLabelClickedListeners)
    {
      listener.errorOrWarningLabelClicked();
    }
  }

  /**
   * Adds an `ErrorOrWarningLabelClickedListener` to the list of listeners.
   *
   * @param listener the listener to be added
   */
  public void addErrorOrWarningLabelClickedListener(ErrorOrWarningLabelClickedListener listener)
  {
    errorOrWarningLabelClickedListeners.add(listener);
  }

  /**
   * The `ErrorOrWarningLabelClickedListener` interface defines a method to handle
   * the click event on the error or warning labels.
   */
  @FunctionalInterface
  public interface ErrorOrWarningLabelClickedListener
  {
    /**
     * Invoked when the error or warning label is clicked.
     */
    void errorOrWarningLabelClicked();
  }
}
