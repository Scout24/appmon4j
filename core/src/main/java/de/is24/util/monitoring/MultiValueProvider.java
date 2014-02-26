/*
 * Created on 12.02.2005
 */
package de.is24.util.monitoring;

import java.util.Collection;


/**
 * MultiValueProviders exposes a list of State instances on demand,
 * @author OSchmitz
 */
public interface MultiValueProvider extends Reportable {
  Collection<State> getValues();

  String getName();
}
