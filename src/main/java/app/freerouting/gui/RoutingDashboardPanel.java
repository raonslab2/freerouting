package app.freerouting.gui;

import app.freerouting.core.RouterCounters;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * Compact routing dashboard that visualizes pass progress and per-pass work while routing is running.
 */
public class RoutingDashboardPanel extends JPanel
{
  private final JProgressBar passProgressBar;
  private final JProgressBar workProgressBar;
  private final JLabel stageLabel;
  private final JLabel timerLabel;
  private final JLabel statsLabel;
  private final NumberFormat integerFormat;
  private Instant startedAt = null;
  private int configuredMaxPasses = 1;

  public RoutingDashboardPanel(Locale locale)
  {
    super(new BorderLayout(6, 4));
    setBorder(new EmptyBorder(6, 8, 6, 8));

    this.integerFormat = NumberFormat.getInstance(locale);
    this.integerFormat.setGroupingUsed(true);
    this.integerFormat.setMaximumFractionDigits(0);

    stageLabel = new JLabel("Routing");
    timerLabel = new JLabel("00:00 elapsed", SwingConstants.RIGHT);

    passProgressBar = new JProgressBar(0, 100);
    passProgressBar.setStringPainted(true);

    workProgressBar = new JProgressBar(0, 100);
    workProgressBar.setStringPainted(true);

    statsLabel = new JLabel("Queued 0 • Routed 0 • Ripped 0 • Failed 0 • Incomplete 0");

    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);
    header.add(stageLabel, BorderLayout.WEST);
    header.add(timerLabel, BorderLayout.EAST);

    JPanel bars = new JPanel(new GridLayout(2, 1, 6, 4));
    bars.setOpaque(false);
    bars.add(passProgressBar);
    bars.add(workProgressBar);

    add(header, BorderLayout.NORTH);
    add(bars, BorderLayout.CENTER);
    add(statsLabel, BorderLayout.SOUTH);

    // Ensure the panel has a minimum preferred size so it's visible
    setPreferredSize(new Dimension(400, 80));
    setMinimumSize(new Dimension(200, 80));

    reset();
  }

  public void reset()
  {
    updateOnEdt(() ->
    {
      startedAt = null;
      configuredMaxPasses = 1;
      stageLabel.setText("Waiting to route");
      passProgressBar.setValue(0);
      passProgressBar.setString("Pass 0 / 0");
      workProgressBar.setValue(0);
      workProgressBar.setString("Current pass 0%");
      statsLabel.setText("Queued 0 • Routed 0 • Ripped 0 • Failed 0 • Incomplete 0");
      timerLabel.setText("00:00 elapsed");
    });
  }

  public void startRun(String stageText, int maxPasses)
  {
    updateOnEdt(() ->
    {
      startedAt = Instant.now();
      configuredMaxPasses = Math.max(1, maxPasses);
      stageLabel.setText(stageText);
      passProgressBar.setValue(0);
      passProgressBar.setString("Pass 1 / " + configuredMaxPasses);
      workProgressBar.setValue(0);
      workProgressBar.setString("Current pass 0%");
      timerLabel.setText("00:00 elapsed");
    });
  }

  public void markPass(String stageText, int passNumber, int startPass, int maxPasses)
  {
    updateOnEdt(() ->
    {
      ensureStarted();
      configuredMaxPasses = Math.max(1, maxPasses);
      stageLabel.setText(stageText);
      updatePassProgress(passNumber, startPass);
      updateTimer();
    });
  }

  public void updateFromCounters(String stageText, RouterCounters counters, int startPass, int maxPasses)
  {
    updateOnEdt(() ->
    {
      ensureStarted();
      configuredMaxPasses = Math.max(1, maxPasses);
      if (stageText != null && !stageText.isEmpty())
      {
        stageLabel.setText(stageText);
      }

      int passNumber = counters != null && counters.passCount != null ? counters.passCount : startPass;
      updatePassProgress(passNumber, startPass);

      if (counters != null)
      {
        int routed = safe(counters.routedCount);
        int ripped = safe(counters.rippedCount);
        int skipped = safe(counters.skippedCount);
        int failed = safe(counters.failedToBeRoutedCount);
        int queued = safe(counters.queuedToBeRoutedCount);
        int incomplete = safe(counters.incompleteCount);

        int completedThisPass = routed + ripped + skipped + failed;
        int totalThisPass = queued + completedThisPass;
        int passPercent = totalThisPass > 0 ? Math.min(100, (int) Math.round((completedThisPass * 100.0) / totalThisPass)) : 0;
        workProgressBar.setValue(passPercent);
        workProgressBar.setString("Current pass " + passPercent + "%");

        statsLabel.setText("Queued " + integerFormat.format(queued) + " • Routed " + integerFormat.format(routed) + " • Ripped " + integerFormat.format(ripped) + " • Failed " + integerFormat.format(failed) + " • Incomplete " + integerFormat.format(incomplete));
      }

      updateTimer();
    });
  }

  public void finish(boolean cancelled, String summary)
  {
    updateOnEdt(() ->
    {
      ensureStarted();
      passProgressBar.setValue(100);
      passProgressBar.setString(summary);
      workProgressBar.setValue(100);
      workProgressBar.setString(cancelled ? "Stopped" : "Completed");
      updateTimer();
    });
  }

  private void updatePassProgress(int passNumber, int startPass)
  {
    int effectivePass = Math.max(1, passNumber - startPass + 1);
    effectivePass = Math.min(effectivePass, configuredMaxPasses);
    int percent = Math.min(100, (int) Math.round((effectivePass * 100.0) / configuredMaxPasses));
    passProgressBar.setValue(percent);
    passProgressBar.setString("Pass " + effectivePass + " / " + configuredMaxPasses);
  }

  private void updateTimer()
  {
    if (startedAt == null)
    {
      return;
    }
    Duration elapsed = Duration.between(startedAt, Instant.now());
    long minutes = elapsed.toMinutes();
    long seconds = elapsed.minusMinutes(minutes).getSeconds();
    timerLabel.setText(String.format("%02d:%02d elapsed", minutes, seconds));
  }

  private int safe(Integer value)
  {
    return value == null ? 0 : value;
  }

  private void ensureStarted()
  {
    if (startedAt == null)
    {
      startedAt = Instant.now();
    }
  }

  private void updateOnEdt(Runnable runnable)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      runnable.run();
    }
    else
    {
      SwingUtilities.invokeLater(runnable);
    }
  }
}
