package de.is24.util.monitoring.visitors;

import java.io.StringWriter;


/**
 * @author oschmitz
 */
public class StringWriterReportVisitor extends UnsortedWriterReportVisitor {
  public StringWriterReportVisitor() {
    super(new StringWriter());
    writeStringToWriter("StringWriterReportVisitor result:");
  }

  public String toString() {
    return writer.toString();
  }
}
