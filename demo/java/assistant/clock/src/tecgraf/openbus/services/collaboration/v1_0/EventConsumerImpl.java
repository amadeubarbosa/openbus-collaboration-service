package tecgraf.openbus.services.collaboration.v1_0;

import org.omg.CORBA.Any;

import tecgraf.openbus.core.v2_0.services.ServiceFailure;

/**
 * Implementa um consumidor de eventos de uma aplica��o. Esse consumidor de
 * eventos � registrado em um canal de eventos de uma sess�o de colabora��o.
 * 
 * @author Tecgraf PUC-Rio
 */
public class EventConsumerImpl extends EventConsumerPOA {

  /**
   * {@inheritDoc}
   */
  @Override
  public void push(final Any event) throws ServiceFailure {
    System.out.println(event.extract_string());
  }
}
