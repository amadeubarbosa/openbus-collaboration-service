package tecgraf.openbus.services.collaboration.v1_0;

import static java.lang.Byte.parseByte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import scs.core.ComponentContext;
import scs.core.ComponentId;
import scs.core.IComponent;
import scs.core.IComponentHelper;
import scs.core.exception.SCSException;
import tecgraf.collaboration.demo.util.Utils;
import tecgraf.openbus.assistant.Assistant;
import tecgraf.openbus.assistant.AssistantParams;
import tecgraf.openbus.core.v2_0.services.ServiceFailure;
import tecgraf.openbus.core.v2_0.services.access_control.InvalidRemoteCode;
import tecgraf.openbus.core.v2_0.services.access_control.NoLoginCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnknownBusCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnverifiedLoginCode;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceOfferDesc;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceProperty;
import tecgraf.openbus.exception.AlreadyLoggedIn;

/**
 * Adiciona um membro � sess�o do servi�o de colabora��o. Caso a sess�o n�o
 * exista, ela � criada.
 * 
 * @author Tecgraf
 */
public final class SessionMember {

  private static String host;
  private static int port;
  private static String entity;
  private static String password;
  private static String file;
  private static int interval = 1;
  private static int retries = 10;

  // sess�o do servi�o de colabora��o
  private static CollaborationSession collaborationSession;

  // nome do componente do membro da sess�o de colabora��o
  private static String componentName;

  // nova sess�o de colabora��o
  private static boolean newSession = false;

  private static ORB orb;
  private static POA poa;
  private static Assistant assist;

  /**
   * Fun��o principal.
   * 
   * @param args argumentos.
   * @throws InvalidName
   * @throws AdapterInactive
   * @throws SCSException
   * @throws AlreadyLoggedIn
   * @throws ServiceFailure
   */
  public static void main(String[] args) throws InvalidName, AdapterInactive,
    SCSException, AlreadyLoggedIn, ServiceFailure {
    // verificando parametros de entrada
    if (args.length < 3) {
      String params = "[file] [interval] [retries]";
      String desc =
        "\n  - [file] = � o arquivo contendo o ID da sess�o de colabora��o"
          + "\n  - [interval] = tempo de espera entre tentativas de acesso ao barramento."
          + " Valor padr�o � '1'"
          + "\n  - [retries] = n�mero m�ximo de tentativas de acesso ao"
          + " barramento em virtude de falhas. Valor padr�o � '10'";
      System.out.println(String.format(Utils.clientUsage, params, desc));
      System.exit(1);
      return;
    }
    // - host
    host = args[0];
    // - porta
    try {
      port = Integer.parseInt(args[1]);
    }
    catch (NumberFormatException e) {
      System.out.println(Utils.port);
      System.exit(1);
      return;
    }
    // - entidade
    entity = args[2];
    // - senha (opcional)
    password = entity;
    if (args.length > 3) {
      password = args[3];
    }
    // - arquivo contendo o ID da sess�o de colabora��o (opcional)
    file = "hello_session.dat";
    if (args.length > 4) {
      file = args[4];
    }
    // - intervalo entre falhas (opcional)
    if (args.length > 5) {
      try {
        interval = Integer.parseInt(args[5]);
      }
      catch (NumberFormatException e) {
        System.out.println("Valor de [interval] deve ser um n�mero");
        System.exit(1);
        return;
      }
    }
    // - n�mero m�ximo de tentativas
    if (args.length > 6) {
      try {
        retries = Integer.parseInt(args[6]);
      }
      catch (NumberFormatException e) {
        System.out.println("Valor de [retries] deve ser um n�mero");
        System.exit(1);
        return;
      }
    }

    // get process id
    RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    String jvmName = runtimeBean.getName();
    String pid = jvmName.split("@")[0];
    componentName = "session_member_" + pid;

    // recuperando o assistente
    AssistantParams params = new AssistantParams();
    params.interval = interval;
    assist =
      Assistant.createWithPassword(host, port, entity, password.getBytes(),
        params);
    orb = assist.orb();
    // - disparando a thread para que o ORB atenda requisi��es
    Thread run = new Thread() {
      @Override
      public void run() {
        orb.run();
      }
    };
    run.start();

    // - criando thread para parar e destruir o ORB ao fim da execu��o do processo
    final Thread shutdown = new Thread() {
      @Override
      public void run() {
        if (collaborationSession != null) {
          try {
            collaborationSession.removeMember(componentName);
          }
          // bus core
          catch (ServiceFailure e) {
            System.err.println(String
              .format("falha severa no barramento em %s:%s : %s", host, port,
                e.message));
          }
          catch (TRANSIENT e) {
            System.err.println(String.format(
              "o barramento em %s:%s esta inacess�vel no momento", host, port));
          }
          catch (COMM_FAILURE e) {
            System.err
              .println("falha de comunica��o ao acessar servi�os n�cleo do barramento");
          }
          catch (NO_PERMISSION e) {
            if (e.minor == NoLoginCode.value) {
              System.err.println(String.format(
                "n�o h� um login de '%s' v�lido no momento", entity));
            }
          }
        }
        assist.shutdown();
        if (newSession) {
          // remove o arquivo com o ID da sess�o de colabora��o
          File f = new File(file);
          f.delete();
          orb.shutdown(true);
          orb.destroy();
        }
      }
    };
    Runtime.getRuntime().addShutdownHook(shutdown);

    // - ativando o POA
    poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    poa.the_POAManager().activate();

    Thread enterCollaborationSession = new Thread() {
      @Override
      public void run() {
        if (enterCollaborationSession()) {
          try {
            createCollaborationSession();
          }
          catch (FileNotFoundException e) {
            System.err.println(String.format(
              "erro ao escrever no arquivo '%s'", file));
            System.exit(1);
            return;
          }
        }
      }
    };
    enterCollaborationSession.start();
  }

  /**
   * Cria o componente do membro de uma sess�o de colabora��o. O componente
   * possui a faceta CollaborationSessionMember que permite que os outros
   * membros da sess�o possam interagir com ele atrav�s dessa faceta.
   * 
   * @return O contexto do componente SCS.
   * @throws SCSException Falha na cria��o do servi�o
   */
  private static IComponent createCollaborationSessionMember()
    throws SCSException {
    ComponentContext component = createComponentContext(componentName, "1.0.0");
    component.addFacet(CollaborationSessionMemberFacet.value,
      CollaborationSessionMemberHelper.id(),
      new CollaborationSessionMemberImpl(componentName));
    org.omg.CORBA.Object obj = component.getIComponent();
    return IComponentHelper.narrow(obj);
  }

  /**
   * Cria um contexto de componente SCS.
   * 
   * @param componentName o nome do componente
   * @param componentVersion a vers�o do componente
   * @return O contexto do componente SCS.
   * @throws SCSException Erro no SCS
   */
  private static ComponentContext createComponentContext(String componentName,
    String componentVersion) throws SCSException {
    final String[] tmp = componentVersion.split("[\\.]");
    byte major = parseByte(tmp[0]);
    byte minor = tmp.length >= 2 ? parseByte(tmp[1]) : 0;
    byte patch = tmp.length >= 3 ? parseByte(tmp[2]) : 0;
    ComponentId componentId =
      new ComponentId(componentName, major, minor, patch, "Java");
    ComponentContext component = new ComponentContext(orb, poa, componentId);
    return component;
  }

  private static boolean enterCollaborationSession() {
    boolean failed;
    do {
      failed = true;
      try {
        // entra na sess�o de colabora��o
        FileReader freader = new FileReader(file);
        BufferedReader breader = new BufferedReader(freader);
        collaborationSession =
          CollaborationSessionHelper.narrow(orb.string_to_object(breader
            .readLine()));
        breader.close();
        if (collaborationSession != null) {
          // se adiciona como membro da sess�o
          collaborationSession.addMember(componentName,
            createCollaborationSessionMember());

          // diz 'hello' para todos os outros membros da sess�o
          tecgraf.openbus.services.collaboration.v1_0.CollaborationMember[] collaborationMember =
            collaborationSession.getMembers();
          for (CollaborationMember m : collaborationMember) {
            if (!m.name.equals(componentName)) {
              org.omg.CORBA.Object object =
                m.member.getFacet(CollaborationSessionMemberHelper.id());
              CollaborationSessionMember collaborationSessionMember =
                CollaborationSessionMemberHelper.narrow(object);
              collaborationSessionMember.sayHello(componentName);
            }
          }
          failed = false;
        }
        else {
          System.err.println("sess�o de colabora��o n�o existe");
        }
      }
      catch (NameInUse e) {
        System.err.println("j� existe um membro com o mesmo nome");
        break;
      }
      catch (SCSException e) {
        System.err.println("falha na cria��o do servi�o");
        break;
      }
      catch (IOException e) {
        // n�o faz nada
        break;
      }
      catch (OBJECT_NOT_EXIST e) {
        // n�o faz nada
        break;
      }
      // bus core
      catch (ServiceFailure e) {
        System.err.println(String.format(
          "falha severa no barramento em %s:%s : %s", host, port, e.message));
      }
      catch (TRANSIENT e) {
        System.err.println(String.format(
          "o barramento em %s:%s esta inacess�vel no momento", host, port));
      }
      catch (COMM_FAILURE e) {
        System.err
          .println("falha de comunica��o ao acessar servi�os n�cleo do barramento");
      }
      catch (NO_PERMISSION e) {
        switch (e.minor) {
          case NoLoginCode.value:
            System.err.println(String.format(
              "n�o h� um login de '%s' v�lido no momento", entity));
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
      }
    } while (failed && retry());
    return failed;
  }

  private static void createCollaborationSession() throws FileNotFoundException {
    boolean failed;
    do {
      ServiceOfferDesc[] services;
      failed = true;
      try {
        // busca pelo servi�o de colabora��o
        ServiceProperty[] properties = new ServiceProperty[1];
        properties[0] =
          new ServiceProperty("openbus.offer.entity",
            CollaborationServiceName.value);
        services = assist.findServices(properties, interval);

        // analiza as ofertas encontradas
        for (ServiceOfferDesc offerDesc : services) {
          org.omg.CORBA.Object collaborationRegistryObj =
            offerDesc.service_ref.getFacet(CollaborationRegistryHelper.id());
          if (collaborationRegistryObj == null) {
            System.out
              .println("o servi�o encontrado n�o prov� a faceta ofertada");
            continue;
          }
          // cria uma sess�o de colabora��o
          CollaborationRegistry collaborationRegistry =
            CollaborationRegistryHelper.narrow(collaborationRegistryObj);
          collaborationSession =
            collaborationRegistry.createCollaborationSession();
          collaborationSession.addMember(componentName,
            createCollaborationSessionMember());

          // grava o ID da sess�o de colabora��o
          PrintWriter out = new PrintWriter(file);
          out.println(orb.object_to_string(collaborationSession));
          out.close();

          newSession = true;
        }
        System.err.println("criada uma nova sess�o de colabora��o");
        failed = false;
      }
      catch (NameInUse e) {
        System.err.println("j� existe um membro com o mesmo nome");
        break;
      }
      catch (SCSException e) {
        System.err.println("falha na cria��o do servi�o");
        break;
      }
      // bus core
      catch (ServiceFailure e) {
        System.err.println(String.format(
          "falha severa no barramento em %s:%s : %s", host, port, e.message));
      }
      catch (TRANSIENT e) {
        System.err.println(String.format(
          "o barramento em %s:%s esta inacess�vel no momento", host, port));
      }
      catch (COMM_FAILURE e) {
        System.err
          .println("falha de comunica��o ao acessar servi�os n�cleo do barramento");
      }
      catch (NO_PERMISSION e) {
        switch (e.minor) {
          case NoLoginCode.value:
            System.err.println(String.format(
              "n�o h� um login de '%s' v�lido no momento", entity));
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
      }
      // erros inesperados
      catch (Throwable e) {
        System.err.println("Erro inesperado durante a busca de servi�os");
        e.printStackTrace();
        System.exit(1);
        return;
      }
    } while (failed && retry());
  }

  public static boolean retry() {
    if (retries > 0) {
      retries--;
      try {
        Thread.sleep(interval * 1000);
      }
      catch (InterruptedException e) {
        // n�o faz nada
      }
      return true;
    }
    System.exit(1);
    return false;
  }
}
