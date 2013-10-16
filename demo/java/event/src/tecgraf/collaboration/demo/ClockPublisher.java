package tecgraf.collaboration.demo;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

import tecgraf.collaboration.demo.util.DemoExceptionHandler;
import tecgraf.collaboration.demo.util.DemoHandlingException;
import tecgraf.collaboration.demo.util.Utils;
import tecgraf.collaboration.demo.util.Utils.DemoParams;
import tecgraf.exception.handling.ExceptionContext;
import tecgraf.exception.handling.ExceptionType;
import tecgraf.openbus.Connection;
import tecgraf.openbus.OpenBusContext;
import tecgraf.openbus.core.ORBInitializer;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceOfferDesc;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceProperty;
import tecgraf.openbus.services.collaboration.v1_0.CollaborationRegistry;
import tecgraf.openbus.services.collaboration.v1_0.CollaborationRegistryHelper;
import tecgraf.openbus.services.collaboration.v1_0.CollaborationSession;

/**
 * Produtor de eventos do demo Event
 * 
 * @author Tecgraf
 */
public final class ClockPublisher {

  /** Tratador de exce��o do demo */
  private static DemoExceptionHandler handler;

  /**
   * Inicializa o ORB e realiza algumas configura��es do demo.
   * 
   * @return o ORB.
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
   * A fun��o principal.
   * <p>
   * Nesta fun��o, o publicador ir�:
   * <ol>
   * <li>inicializar o ORB</li>
   * <li>construir uma conex�o com o barramento OpenBus</li>
   * <li>autenticar-se junto ao barramento</li>
   * <li>buscar pelo servi�o de colabora��o</li>
   * <li>verificar se encontrou uma refer�ncia v�lida do servi�o</li>
   * <li>solicitar a cria��o de uma sess�o de colabora��o</li>
   * <li>salvar o IOR da sess�o em um arquivo local</li>
   * <li>publicar eventos no canal de eventos</li>
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
      DemoHandlingException excep =
        handler.process(e, ExceptionContext.LoginByPassword);
      if (excep.getType() != ExceptionType.AlreadyLoggedIn) {
        System.exit(1);
        return;
      }
    }

    ServiceOfferDesc[] services;
    // busca por servi�o
    try {
      ServiceProperty[] properties = new ServiceProperty[1];
      properties[0] =
        new ServiceProperty("openbus.component.interface",
          CollaborationRegistryHelper.id());
      services = context.getOfferRegistry().findServices(properties);
    }
    catch (Exception e) {
      handler.process(e, ExceptionContext.BusCore);
      System.exit(1);
      return;
    }

    CollaborationRegistry collab = null;
    // analisa as ofertas encontradas
    for (ServiceOfferDesc offerDesc : services) {
      try {
        // testa se a oferta � v�lida
        if (!offerDesc.service_ref._non_existent()) {
          org.omg.CORBA.Object helloObj =
            offerDesc.service_ref.getFacet(CollaborationRegistryHelper.id());
          if (helloObj != null) {
            collab = CollaborationRegistryHelper.narrow(helloObj);
            System.out.println("o servi�o foi encontrado!");
            break;
          }
          else {
            System.out
              .println("o servi�o encontrado n�o prov� a faceta ofertada");
          }
        }
      }
      catch (Exception e) {
        handler.process(e, ExceptionContext.Service);
        System.out.println("descartando oferta inv�lida");
      }
    }

    if (collab == null) {
      System.out.println("nenhum servi�o v�lido foi encontrado.");
      System.exit(1);
      return;
    }

    // cria a sess�o de colabora��o
    CollaborationSession session = null;
    try {
      session = collab.createCollaborationSession();
      // disponibilizando uma refer�ncia para a sess�o criada    
      PrintWriter pw = new PrintWriter(new FileOutputStream("session.ior"));
      pw.println(orb.object_to_string(session));
      pw.close();
    }
    catch (Exception e) {
      handler.process(e, ExceptionContext.Service);
      System.exit(1);
      return;
    }

    System.out.println("Disparando eventos no canal...");
    // nosso demo n�o para de executar
    while (true) {
      // faz uso do canal da sess�o
      try {
        // cria um evento com data e hora atuais
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String time = formatter.format(calendar.getTime());
        // evento � do tipo any
        Any event = orb.create_any();
        event.insert_string(time);
        // envia o evento para o canal
        session.channel().push(event);
        Thread.sleep(3 * 1000);
      }
      catch (Exception e) {
        handler.process(e, ExceptionContext.Service);
        System.exit(1);
        return;
      }
    }
  }

}
