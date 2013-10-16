package tecgraf.openbus.services.collaboration.v1_0;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.omg.CORBA.Any;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import scs.core.exception.SCSException;
import tecgraf.openbus.Connection;
import tecgraf.openbus.InvalidLoginCallback;
import tecgraf.openbus.OpenBusContext;
import tecgraf.openbus.core.ORBInitializer;
import tecgraf.openbus.core.v2_0.services.ServiceFailure;
import tecgraf.openbus.core.v2_0.services.access_control.AccessDenied;
import tecgraf.openbus.core.v2_0.services.access_control.InvalidRemoteCode;
import tecgraf.openbus.core.v2_0.services.access_control.LoginInfo;
import tecgraf.openbus.core.v2_0.services.access_control.NoLoginCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnknownBusCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnverifiedLoginCode;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceOfferDesc;
import tecgraf.openbus.core.v2_0.services.offer_registry.ServiceProperty;
import tecgraf.openbus.demo.util.Utils;
import tecgraf.openbus.exception.AlreadyLoggedIn;

/**
 * Produtor de eventos do demo Clock
 *
 * @author Tecgraf
 */
public final class ClockPublisher {

  private static String host;
  private static int port;
  private static String entity;
  private static String password;
  private static String file;
  private static int interval = 1;
  private static int retries = 10;

  // sess�o do servi�o de colabora��o
  private static CollaborationSession collaborationSession;

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
        "\n  - [file] = � o arquivo onde ser� gravado o ID da sess�o de colabora��o"
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
    // - arquivo onde ser� gravado o ID da sess�o de colabora��o (opcional)
    file = "clock_session.dat";
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

    // inicializando e configurando o ORB
    final ORB orb = ORBInitializer.initORB();
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
        // remove o arquivo com o ID da sess�o de colabora��o
        File f = new File(file);
        f.delete();
        orb.shutdown(true);
        orb.destroy();
      }
    };
    Runtime.getRuntime().addShutdownHook(shutdown);

    // recuperando o gerente de contexto de chamadas � barramentos
    final OpenBusContext context =
      (OpenBusContext) orb.resolve_initial_references("OpenBusContext");

    // - ativando o POA
    final POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    poa.the_POAManager().activate();

    // conectando ao barramento.
    final Connection conn = context.createConnection(host, port);
    context.setDefaultConnection(conn);
    conn.onInvalidLoginCallback(new InvalidLoginCallback() {

      /** Vari�veis de controle para garantir que n�o registre r�plicas */
      ConcurrencyControl options = new ConcurrencyControl();

      @Override
      public void invalidLogin(Connection conn, LoginInfo login) {
        synchronized (options.lock) {
          options.disabled = false;
        }
        // autentica-se no barramento
        login(conn, entity, password, host, port);
        // entrando na sess�o de colabora��o
        synchronized (options.lock) {
          if (!options.disabled && !options.active) {
            options.active = true;
            Thread createCollaborationSession = new Thread() {
              @Override
              public void run() {
                try {
                  createCollaborationSession();
                }
                catch (FileNotFoundException e) {
                  System.err.println(String.format(
                    "erro ao escrever no arquivo '%s'", file));
                  System.exit(1);
                  return;
                }
                do {
                  // enviando eventos para o canal
                  sendEvent();
                  try {
                    Thread.sleep(interval * 1000);
                  }
                  catch (InterruptedException e) {
                    // n�o faz nada
                  }
                } while (true);
              }
            };
            createCollaborationSession.start();
          }
        }
      }

      private void login(Connection conn, String entity, String password,
        Object host, Object port) {
        // autentica-se no barramento
        boolean failed;
        do {
          failed = true;
          try {
            conn.loginByPassword(entity, password.getBytes());
            failed = false;
          }
          catch (AlreadyLoggedIn e) {
            // ignorando exce��o
            failed = false;
          }
          // login by certificate
          catch (AccessDenied e) {
            System.err.println(String.format(
              "a senha fornecida para a entidade '%s' foi negada", entity));
            System.exit(1);
            return;
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
        } while (failed && retry());
      }

      private void createCollaborationSession() throws FileNotFoundException {
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
            services = context.getOfferRegistry().findServices(properties);

            // analiza as ofertas encontradas
            for (ServiceOfferDesc offerDesc : services) {
              org.omg.CORBA.Object collaborationRegistryObj =
                offerDesc.service_ref
                  .getFacet(CollaborationRegistryHelper.id());
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

              // grava o ID da sess�o de colabora��o
              PrintWriter out = new PrintWriter(file);
              out.println(orb.object_to_string(collaborationSession));
              out.close();
            }
            failed = false;
            synchronized (options.lock) {
              options.disabled = true;
            }
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
        synchronized (options.lock) {
          options.active = false;
        }
      }

      public void sendEvent() {
        Any event = orb.create_any();

        // cria um evento com data e hora atuais
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        event.insert_string(formatter.format(calendar.getTime()));

        boolean failed;
        do {
          failed = true;
          try {
            if (collaborationSession != null) {
              // envia o evento para o canal
              collaborationSession.channel().push(event);
            }
            failed = false;
            synchronized (options.lock) {
              options.disabled = true;
            }
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
        } while (failed && retry());
        synchronized (options.lock) {
          options.active = false;
        }
      }

    });

    // autentica-se no barramento
    conn.onInvalidLoginCallback().invalidLogin(conn, null);
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

  public static class ConcurrencyControl {
    public volatile boolean active = false;
    public volatile boolean disabled = false;
    public Object lock = new Object();
  }
}
