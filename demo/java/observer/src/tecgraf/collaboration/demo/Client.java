package tecgraf.collaboration.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import scs.core.ComponentContext;
import scs.core.ComponentId;
import scs.core.IComponent;
import tecgraf.collaboration.demo.util.DemoExceptionHandler;
import tecgraf.collaboration.demo.util.DemoHandlingException;
import tecgraf.collaboration.demo.util.Utils;
import tecgraf.collaboration.demo.util.Utils.DemoParams;
import tecgraf.exception.handling.ExceptionContext;
import tecgraf.exception.handling.ExceptionType;
import tecgraf.openbus.Connection;
import tecgraf.openbus.OpenBusContext;
import tecgraf.openbus.core.ORBInitializer;
import tecgraf.openbus.services.collaboration.v1_0.CollaborationSession;
import tecgraf.openbus.services.collaboration.v1_0.CollaborationSessionHelper;

/**
 * Cliente do demo Observer.
 * <p>
 * Adiciona membros � sess�o do servi�o de colabora��o
 * 
 * @author Tecgraf
 */
public final class Client {

  /** Tratador de exce��o do demo */
  private static DemoExceptionHandler handler;

  /**
   * Inicializa o ORB e realiza algumas configura��es do demo.
   * 
   * @return o ORB
   */
  private static ORB initORB() {
    final ORB orb = ORBInitializer.initORB();
    // - criando thread para parar e destruir o ORB ao fim da execu��o do processo 
    Thread shutdown = new Thread() {
      @Override
      public void run() {
        try {
          OpenBusContext context =
            (OpenBusContext) orb.resolve_initial_references("OpenBusContext");
          context.getCurrentConnection().logout();
        }
        catch (Exception e) {
          handler.process(e, ExceptionContext.BusCore);
        }
        orb.shutdown(true);
        orb.destroy();
      }
    };
    Runtime.getRuntime().addShutdownHook(shutdown);

    return orb;
  }

  /**
   * Constr�i um membro simples para ser inclu�do na sess�o de colabora��o.
   * 
   * @param orb o ORB ao qual o membro deve estar associado.
   * @return o membro.
   */
  private static IComponent buildSessionMember(ORB orb) {
    try {
      POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      poa.the_POAManager().activate();
      ComponentContext component =
        new ComponentContext(orb, poa, new ComponentId("DummyMember", (byte) 1,
          (byte) 0, (byte) 0, "Java"));
      return component.getIComponent();
    }
    catch (Exception e) {
      handler.process(e, ExceptionContext.Local);
      System.exit(1);
      return null;
    }
  }

  /**
   * A fun��o principal.
   * <p>
   * Nesta fun��o, o cliente ir�:
   * <ol>
   * <li>inicializar o ORB</li>
   * <li>construir uma conex�o com o barramento OpenBus</li>
   * <li>autenticar-se junto ao barramento</li>
   * <li>recuperar a sess�o de colabora��o atrav�s do IOR lido do arquivo</li>
   * <li>verificar se encontrou uma refer�ncia v�lida da sess�o</li>
   * <li>cadastrar um conjunto de membros na sess�o de colabora��o</li>
   * <li>descadastrar o mesmo conjunto de membros da sess�o de colabora��o</li>
   * <li>desconectar do barramento</li>
   * </ol>
   * 
   * @param args argumentos de linha de comando
   * @throws InvalidName
   */
  public static void main(String[] args) throws InvalidName {
    DemoParams params = Utils.retrieveParams(args);
    handler = new DemoExceptionHandler(params);

    ORB orb = initORB();
    OpenBusContext context =
      (OpenBusContext) orb.resolve_initial_references("OpenBusContext");
    Connection conn = context.createConnection(params.host, params.port);
    context.setDefaultConnection(conn);

    // autentica-se no barramento
    try {
      conn.loginByPassword(params.entity, params.password.getBytes());
    }
    catch (Exception e) {
      DemoHandlingException exp =
        handler.process(e, ExceptionContext.LoginByPassword);
      // ignora a exce��o se o erro foi de AlreadyLoggedIn
      if (exp.getType() != ExceptionType.AlreadyLoggedIn) {
        System.exit(1);
        return;
      }
    }

    try {
      // recupera a sess�o de colabora��o que foi compartilhada
      BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(
          "session.ior")));
      String ior = reader.readLine();
      reader.close();
      CollaborationSession session =
        CollaborationSessionHelper.narrow(orb.string_to_object(ior));

      // verificando se sess�o � v�lida
      if (!session._non_existent()) {
        String prefix = "dummy member-";
        int nMembers = 3;
        for (int i = 0; i < nMembers; i++) {
          // inclui membros na sess�o
          session.addMember(prefix + i, buildSessionMember(orb));
        }
        for (int i = nMembers; i > 0;) {
          i--;
          // remove os membros da sess�o
          session.removeMember(prefix + i);
        }
      }
      else {
        System.out.println("Sess�o recuperada n�o � v�lida.");
        System.exit(1);
      }
    }
    catch (Exception e) {
      handler.process(e, ExceptionContext.Service);
      System.exit(1);
      return;
    }

  }
}
