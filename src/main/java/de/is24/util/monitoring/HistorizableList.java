package de.is24.util.monitoring;

import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;


/**
 * This list manages ordering and discarding of registered Historizables
 * with the same name.
 *
 * @author OSchmitz
 */
public class HistorizableList implements Reportable, Iterable<Historizable> {
  private static final long serialVersionUID = -3419108076548380477L;

  private String name;
  private final int fMaxEntriesToKeep;
  private LinkedList<Historizable> historizables = new LinkedList<Historizable>();

  private static final Logger LOGGER = Logger.getLogger(HistorizableList.class);

  /**
   * This class is only constructed by {@link InApplicationMonitor}
   * @param name name of this Counter
   */
  HistorizableList(String name, int aMaxEntriesToKeep) {
    this.name = name;
    fMaxEntriesToKeep = aMaxEntriesToKeep;
  }

  /**
   * Implements the visitor pattern to read this Counter
   */
  public synchronized void accept(ReportVisitor aVisitor) {
    LOGGER.debug("+++ entering HistorizableList.accept +++");
    aVisitor.reportHistorizableList(this);
  }

  /**
   * @return name of the managed Historizables
   */
  public String getName() {
    return name;
  }

  /**
   * add a {@link Historizable} instance to this list and remove the oldest
   * instance if necessary.
   *
   * @param historizable the Historizable to add
   */
  public void add(Historizable historizable) {
    LOGGER.debug("+++ entering HistorizableList.add +++");
    synchronized (historizables) {
      if (historizables.size() >= fMaxEntriesToKeep) {
        historizables.removeLast();
      }
      historizables.addFirst(historizable);
    }
  }

  /**
   * @return an iterator over the {@link Historizable} entries.
   * The iterator actually iterates over a clone of the internal list,
   * to prevent concorrent modification problems.
   */
  public Iterator<Historizable> iterator() {
    synchronized (historizables) {
      return ((LinkedList<Historizable>) historizables.clone()).iterator();
    }
  }

  /**
   * @return the maximum number of entries that can be contained in this list.
   */
  public int getMaxEntriesToKeep() {
    return fMaxEntriesToKeep;
  }

  /**
   * @return the actual amount of contained {@link Historizable}s.
   */
  public int size() {
    synchronized (historizables) {
      return historizables.size();
    }
  }

  /**
   * This method is thread-safe in combination with size().
   * The size returned is always the least available size.
   *
   * @param index the index of the {@link Historizable} to be returned. Can never be greater or equal than getMaxEntriesToKeep().
   * @return the {@link Historizable} at the index position.
   * @throws IndexOutOfBoundsException if the index argument is greater or equal the size of this list.
   */
  public Historizable get(int index) {
    synchronized (historizables) {
      return historizables.get(index);
    }
  }
}
