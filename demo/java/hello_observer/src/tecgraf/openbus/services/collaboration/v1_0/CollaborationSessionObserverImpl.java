package tecgraf.openbus.services.collaboration.v1_0;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.TRANSIENT;

import scs.core.IComponent;
import tecgraf.openbus.core.v2_0.services.ServiceFailure;
import tecgraf.openbus.core.v2_0.services.access_control.InvalidRemoteCode;
import tecgraf.openbus.core.v2_0.services.access_control.NoLoginCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnknownBusCode;
import tecgraf.openbus.core.v2_0.services.access_control.UnverifiedLoginCode;

/**
 * Observador de uma sess�o de colabora��o.
 *
 * @author Tecgraf PUC-Rio
 */
public class CollaborationSessionObserverImpl extends CollaborationObserverPOA {
  /**
   * Sess�o do servi�o de colabora��o.
   */
  private CollaborationSession collaborationSession;

  /**
   * Construtor.
   *
   * @param collaborationSession Sess�o do servi�o de colabora��o.
   */
  public CollaborationSessionObserverImpl(
    CollaborationSession collaborationSession) {
    this.collaborationSession = collaborationSession;
  }

  /**
   * Um novo membro entrou na sess�o de colabora��o.
   *
   * {@inheritDoc}
   */
  @Override
  public void memberAdded(String name, IComponent member) {
    System.out.println("Adicionado um novo membro '" + name
      + "' na sess�o de colabora��o.");

    try {
      // diz 'hello' para todos os outros membros da sess�o
      tecgraf.openbus.services.collaboration.v1_0.CollaborationMember[] collaborationMember =
        collaborationSession.getMembers();
      for (CollaborationMember m : collaborationMember) {
        if (!m.name.equals(name)) {
          org.omg.CORBA.Object object =
            m.member.getFacet(CollaborationSessionMemberHelper.id());
          CollaborationSessionMember collaborationSessionMember =
            CollaborationSessionMemberHelper.narrow(object);
          collaborationSessionMember.sayHello(name);
        }
      }
    }
    // bus core
    catch (ServiceFailure e) {
      System.err.println("falha severa no barramento");
    }
    catch (TRANSIENT e) {
      System.err.println("o barramento esta inacess�vel no momento");
    }
    catch (COMM_FAILURE e) {
      System.err
        .println("falha de comunica��o ao acessar servi�os n�cleo do barramento");
    }
    catch (NO_PERMISSION e) {
      switch (e.minor) {
        case NoLoginCode.value:
          System.err.println("n�o h� um login de v�lido no momento");
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
  }

  /**
   * Um membro saiu da sess�o de colabora��o.
   *
   * {@inheritDoc}
   */
  @Override
  public void memberRemoved(String name) {
    System.out.println("Removido o membro '" + name
      + "' da sess�o de colabora��o");
  }

  /**
   * A sess�o de colabora��o deixou de existir.
   *
   * {@inheritDoc}
   */
  @Override
  public void destroyed() {
    System.out.println("A sess�o de colabora��o foi finalizada");
  }
}
