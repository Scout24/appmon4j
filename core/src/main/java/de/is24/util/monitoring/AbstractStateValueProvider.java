package de.is24.util.monitoring;

/**
 * Convenience implementation of {@link StateValueProvider}.
 * Implements the <code>getName()</code> interface method with a constructor argument name.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
public abstract class AbstractStateValueProvider extends StateValueProvider {
  private final String name;

  /**
   * @param name the name of this {@link StateValueProvider}. May not be blank.
   */
  public AbstractStateValueProvider(String name) {
    super();
    this.name = name;
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.StateValueProvider#getName()
   */
  @Override
  public String getName() {
    return name;
  }
}
