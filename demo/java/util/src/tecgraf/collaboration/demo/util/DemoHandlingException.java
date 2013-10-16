package tecgraf.collaboration.demo.util;

import tecgraf.exception.handling.ExceptionContext;
import tecgraf.exception.handling.ExceptionType;
import tecgraf.exception.handling.HandlingException;

/**
 * Wrapper de exce��es espec�ficos para os demos de colabora��o. Esta classe
 * esta associada a enumera��o {@link ExceptionType}.
 * 
 * @author Tecgraf
 */
public class DemoHandlingException extends HandlingException<ExceptionType> {

  /**
   * Construtor.
   * 
   * @param exception a exce��o a ser tratada
   * @param context o contexto no qual a exce��o ocorreu.
   */
  public DemoHandlingException(Exception exception, ExceptionContext context) {
    super(exception, context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ExceptionType getTypeFromException(Exception exception) {
    return ExceptionType.getType(exception);
  }

}