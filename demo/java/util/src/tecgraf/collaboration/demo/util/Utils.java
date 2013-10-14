package tecgraf.collaboration.demo.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tecgraf.openbus.CallerChain;
import tecgraf.openbus.core.v2_0.services.access_control.LoginInfo;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceOfferDesc;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceProperty;

/**
 * Classe utilit�ria para os demos Java.
 * 
 * @author Tecgraf
 */
public class Utils {

  public static final String usage =
    "Usage: 'demo' <host> <port> <entity> [password] %s\n"
      + "  - host = � o host do barramento\n"
      + "  - port = � a porta do barramento\n"
      + "  - entity = � a entidade a ser autenticada\n"
      + "  - password = senha (opcional) %s";

  public static final String clientUsage =
    "Usage: 'demo' <host> <port> <entity> [password] %s\n"
      + "  - host = � o host do barramento\n"
      + "  - port = � a porta do barramento\n"
      + "  - entity = � a entidade a ser autenticada\n"
      + "  - password = senha (opcional) %s";

  public static final String serverUsage =
    "Usage: 'demo' <host> <port> <entity> <privatekeypath> %s\n"
      + "  - host = � o host do barramento\n"
      + "  - port = � a porta do barramento\n"
      + "  - entity = � a entidade a ser autenticada\n"
      + "  - privatekeypath = � o caminho da chave privada de autentica��o da entidade %s";

  public static final String port = "Valor de <port> deve ser um n�mero";
  public static final String keypath =
    "<privatekeypath> deve apontar para uma chave v�lida.";

  static public DemoParams retrieveParams(String[] args) {
    DemoParams params = new DemoParams();
    // verificando parametros de entrada
    if (args.length < 3) {
      System.out.println(String.format(Utils.usage, "", ""));
      System.exit(1);
      return null;
    }
    // - host
    params.host = args[0];
    // - porta
    try {
      params.port = Integer.parseInt(args[1]);
    }
    catch (NumberFormatException e) {
      System.out.println(Utils.port);
      System.exit(1);
      return null;
    }
    // - entidade
    params.entity = args[2];
    // - senha (opcional)
    if (args.length > 3) {
      params.password = args[3];
    }
    else {
      params.password = params.entity;
    }
    return params;
  }

  static public String chain2str(CallerChain chain) {
    StringBuffer buffer = new StringBuffer();
    for (LoginInfo loginInfo : chain.originators()) {
      buffer.append(loginInfo.entity);
      buffer.append("->");
    }
    buffer.append(chain.caller().entity);
    return buffer.toString();
  }

  static public String getProperty(ServiceOfferDesc offer, String prop) {
    ServiceProperty[] properties = offer.properties;
    for (int i = 0; i < properties.length; i++) {
      if (properties[i].name.equals(prop)) {
        return properties[i].value;
      }
    }
    return null;
  }

  public static void setLogLevel(Level level) {
    Logger logger = Logger.getLogger("tecgraf.openbus");
    logger.setLevel(level);
    logger.setUseParentHandlers(false);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(level);
    logger.addHandler(handler);
  }

  public static void setJacorbLogLevel(Level level) {
    Logger logger = Logger.getLogger("jacorb");
    logger.setLevel(level);
    logger.setUseParentHandlers(false);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(level);
    logger.addHandler(handler);
  }

  public static class DemoParams {
    public String host = "localhost";
    public int port = 2089;
    public String entity = "entity";
    public String password = this.entity;
  }

}