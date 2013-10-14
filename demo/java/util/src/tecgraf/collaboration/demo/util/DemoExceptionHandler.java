package tecgraf.collaboration.demo.util;

import org.omg.CORBA.NO_PERMISSION;

import tecgraf.collaboration.demo.util.Utils.DemoParams;
import tecgraf.exception.handling.ExceptionContext;
import tecgraf.exception.handling.ExceptionHandler;
import tecgraf.exception.handling.ExceptionType;
import tecgraf.openbus.core.v2_0.services.ServiceFailure;
import tecgraf.openbus.core.v2_0.services.access_control.InvalidRemoteCode;
import tecgraf.openbus.core.v2_0.services.access_control.NoLoginCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnknownBusCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnverifiedLoginCode;

/**
 * Tratador de exce��es padr�o para os demos.
 * 
 * @author Tecgraf
 */
public class DemoExceptionHandler extends
  ExceptionHandler<DemoHandlingException> {

  /** Informa��es de configura��o do demos */
  DemoParams params;

  /**
   * Construtor.
   * 
   * @param params
   */
  public DemoExceptionHandler(DemoParams params) {
    this.params = params;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void handleException(DemoHandlingException exception) {
    Exception theException = exception.getException();
    ExceptionType type = exception.getType();
    ExceptionContext context = exception.getContext();
    switch (type) {
      case AccessDenied:
        switch (context) {
          case LoginByPassword:
            System.err.println(String.format(
              "a senha fornecida para a entidade '%s' foi negada",
              params.entity));
            break;

          case LoginByCertificate:
            System.err.println(String.format(
              "a chave n�o corresponde ao certificado da entidade '%s'",
              params.entity));
            break;

          default:
            System.err.println("autentica��o junto ao barramento falhou.");
            break;
        }
        break;

      case ServiceFailure:
        switch (context) {
          case BusCore:
            System.err.println(String.format(
              "falha severa no barramento em %s:%s : %s", params.host,
              params.port, ((ServiceFailure) theException).message));
            break;

          default:
            System.err.println(String.format("falha severa no servi�o: %s",
              ((ServiceFailure) theException).message));
            break;
        }
        break;

      case OBJECT_NOT_EXIST:
        switch (context) {
          case BusCore:
            System.err.println(String.format(
              "refer�ncia para o barramento em %s:%s n�o existe.", params.host,
              params.port));
            break;

          default:
            System.err.println("refer�ncia para o servi�o n�o existe");
            break;
        }
        break;

      case TRANSIENT:
        switch (context) {
          case BusCore:
            System.err.println(String.format(
              "o barramento em %s:%s esta inacess�vel no momento", params.host,
              params.port));
            break;

          default:
            System.err.println("servi�o est� indispon�vel no momento.");
            break;
        }
        break;

      case COMM_FAILURE:
        switch (context) {
          case BusCore:
            System.err
              .println("falha de comunica��o ao acessar servi�os n�cleo do barramento");
            break;

          default:
            System.err.println("falha de comunica��o ao acessar servi�o.");
        }
        break;

      case NO_PERMISSION:
        NO_PERMISSION noPermission = (NO_PERMISSION) theException;
        switch (context) {
          case Service:
            switch (noPermission.minor) {
              case NoLoginCode.value:
                System.err.println(String.format(
                  "n�o h� um login de '%s' v�lido no momento", params.entity));
                break;

              case UnknownBusCode.value:
                System.err
                  .println("o servi�o encontrado n�o est� mais logado ao barramento");
                break;

              case UnverifiedLoginCode.value:
                System.err
                  .println("o servi�o encontrado n�o foi capaz de validar a chamada");
                break;

              case InvalidRemoteCode.value:
                System.err
                  .println("integra��o do servi�o encontrado com o barramento est� incorreta");
                break;
            }
            break;

          default:
            if (noPermission.minor == NoLoginCode.value) {
              System.err.println(String.format(
                "n�o h� um login de '%s' v�lido no momento", params.entity));
            }
            else {
              System.err.println("Erro NO_PERMISSION inesperado.");
            }
            break;
        }
        break;

      case InvalidName:
        // Este erro nunca deveria ocorrer se o c�digo foi bem escrito
        System.err.println(String.format("CORBA.InvalidName: %s", theException
          .getMessage()));
        System.exit(1);
        break;

      case Unspecified:
      default:
        System.err.println(String.format("Erro n�o categorizado: %s",
          theException.getMessage()));
        break;
    }
    // por fim imprime a pilha de erro para todas as exce��es
    theException.printStackTrace();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DemoHandlingException getHandlingException(Exception exception,
    ExceptionContext context) {
    return new DemoHandlingException(exception, context);
  }

}
