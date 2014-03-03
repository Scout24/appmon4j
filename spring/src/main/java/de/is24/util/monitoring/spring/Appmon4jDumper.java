package de.is24.util.monitoring.spring;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.visitors.AbstractSortedReportVisitor;
import de.is24.util.monitoring.visitors.HierarchyReportVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import static de.is24.util.monitoring.visitors.HierarchyReportVisitor.Tree.TreeNode;


/**
 * A spring <code>ApplicationListener</code> which dumps the content of the InApplicationMonitor to log4j when the
 * context is closed.
 *
 * @author <a href="mailto:sebastian.kirsch@immobilienscout24.de">Sebastian Kirsch</a>
 */
public class Appmon4jDumper implements ApplicationListener<ContextClosedEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Appmon4jDumper.class);
  private final InApplicationMonitor inApplicationMonitor;

  public Appmon4jDumper(InApplicationMonitor inApplicationMonitor) {
    this.inApplicationMonitor = inApplicationMonitor;
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    if (LOGGER.isInfoEnabled()) {
      HierarchyReportVisitor reportVisitor = new HierarchyReportVisitor();
      inApplicationMonitor.getCorePlugin().reportInto(reportVisitor);
      LOGGER.info("Dumping InApplicationMonitor content on ContextClosedEvent:\n {}", dumpReporting((reportVisitor)));
    }
  }

  private String dumpReporting(HierarchyReportVisitor hierarchyReportVisitor) {
    StringBuilder builder = new StringBuilder(4 * 1024);
    for (TreeNode element : hierarchyReportVisitor.getTree().getAllNodesWithEntries()) {
      for (AbstractSortedReportVisitor.Entry entry : element.entries()) {
        builder.append(entry.getValue()).append("\n");
      }
    }
    removeNewLineCharacterAtTheEnd(builder);
    return builder.toString();
  }

  private void removeNewLineCharacterAtTheEnd(StringBuilder builder) {
    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }
  }

}
