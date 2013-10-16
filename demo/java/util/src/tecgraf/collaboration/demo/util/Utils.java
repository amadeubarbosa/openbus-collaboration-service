package tecgraf.collaboration.demo.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilit�ria para os demos Java.
 * 
 * @author Tecgraf
 */
public class Utils {

  /**
   * Texto explicativo de uso do demo.
   */
  public static final String usage =
    "Usage: 'demo' <host> <port> <entity> [password] %s\n"
      + "  - host = � o host do barramento\n"
      + "  - port = � a porta do barramento\n"
      + "  - entity = � a entidade a ser autenticada\n"
      + "  - password = senha (opcional) %s";

  /**
   * Erro a ser apresentado quando ocorrer mau uso do par�metro "port"
   */
  public static final String port = "Valor de <port> deve ser um n�mero";

  /**
   * M�todo auxiliar para extrair os par�metros de entrada do demo.
   * 
   * @param args argumentos passados por linha de comando.
   * @return os par�metros a serem utilizados no demo.
   */
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

  /**
   * M�todo utilit�rio para configurar o n�vel de log da API do OpenBus
   * 
   * @param level o n�vel de log.
   */
  public static void setLogLevel(Level level) {
    Logger logger = Logger.getLogger("tecgraf.openbus");
    logger.setLevel(level);
    logger.setUseParentHandlers(false);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(level);
    logger.addHandler(handler);
  }

  /**
   * M�todo utilit�rio para configurar o n�vel de log do JacORB
   * 
   * @param level o n�vel de log.
   */
  public static void setJacorbLogLevel(Level level) {
    Logger logger = Logger.getLogger("jacorb");
    logger.setLevel(level);
    logger.setUseParentHandlers(false);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(level);
    logger.addHandler(handler);
  }

  /**
   * Classe utilit�ria para estruturar os par�metros de entrada do demo.
   * 
   * @author Tecgraf
   */
  public static class DemoParams {
    /** host */
    public String host = "localhost";
    /** porta */
    public int port = 2089;
    /** entidade */
    public String entity = "entity";
    /** senha */
    public String password = this.entity;
  }

}
