package de.is24.util.monitoring;

import java.util.Date;


/**
 * Represents a single Value of a historizable entity.
 * Historizables are used to report the state of entities or values who
 * are not directly comparable between each other.<br><br>
 *
 * For example the total runtime of an EmailSender run will differ
 * between a mailing with 4 emails and a mailing with 100000 emails.
 * Thus using a {@link Timer} will not give reasonable results.
 * A Historizable value may contain a String like:
 *  <pre> sending of 4 mails took 303 ms </pre>
 *
 * @author OSchmitz
 */
public interface Historizable {
  /**
   * This string represents whatever your applications wants to tell
   * the rest of the world.
   * @return the historized value, which can actually be any String
   */
  String getValue();

  /**
   * name of this historizable
   * @return name of this historizable
   */
  String getName();

  /**
   * timestamp associated with this Historizable
   * @return a Date instance
   */
  Date getTimestamp();
}
