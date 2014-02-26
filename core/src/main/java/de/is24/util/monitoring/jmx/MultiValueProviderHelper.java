package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.State;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.Collection;


public class MultiValueProviderHelper {
  private MultiValueProvider reportable;

  private String[] itemNames;
  private OpenType[] itemTypes;
  private Object[] itemValues;

  public MultiValueProviderHelper(MultiValueProvider reportable) {
    this.reportable = reportable;
    this.loadFields();
  }

  private void loadFields() {
    Collection<State> values = reportable.getValues();

    int stateCount = values.size();
    itemNames = new String[stateCount];
    itemTypes = new OpenType[stateCount];
    itemValues = new Object[stateCount];

    int index = 0;
    for (State state : values) {
      itemNames[index] = state.name;
      itemTypes[index] = SimpleType.LONG;
      itemValues[index] = state.value;
      index++;
    }

  }

  public CompositeData toComposite() {
    try {
      CompositeType compositeType = new CompositeType("testCompositeType", "a text composite", itemNames,
        itemNames,
        itemTypes);

      return new CompositeDataSupport(compositeType, itemNames, itemValues);
    } catch (OpenDataException e) {
      throw new RuntimeException(e);
    }
  }
}
