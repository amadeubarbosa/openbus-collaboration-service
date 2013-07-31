A demo Hello Observer tenta demonstrar o uso das facetas espec�ficas dos membros da
sess�o de colabora��o atrav�s da troca de messagens entre cada um dos membros da sess�o.
A cria��o da sess�o e o monitoramento da entrada e sa�da de membros da sess�o � feita
pelo observador. O observador s� funciona ap�s conseguir se conectar no barramento,
realizar o login, encontrar a oferta do servi�o de colabora��o, e criar uma sess�o
no servi�o de colabora��o. Caso o login seja perdido, sua callback de login inv�lido
tenta refazer esse processo por um n�mero m�ximo de tentativas.

Cada um dos membros da sess�o, por sua vez, ap�s conseguir se conectar no barramento,
realizar o login, e encontrar a oferta do servi�o de colabora��o, tenta acessar a
sess�o criada pelo observador da sess�o utilizando um arquivo com o ID da sess�o de
colabora��o. Se n�o conseguir ap�s um n�mero de tentativas, falha com uma mensagem
de erro.

------------------------------
-------- DEPEND�NCIAS---------
------------------------------

As depend�ncias de software s�o fornecidas j� compiladas, em conjunto com a demo:

ant-1.8.2.jar
ant-launcher-1.8.2.jar
jacorb-3.1.jar
openbus-sdk-core-2.0.0.0.jar
openbus-sdk-demo-util-2.0.0.0.jar
openbus-sdk-legacy-2.0.0.0.jar
scs-core-1.2.1.1.jar
slf4j-api-1.6.4.jar
slf4j-jdk14-1.6.4.jar

------------------------------
--------- ARGUMENTOS ---------
------------------------------

SessionObserver
1) host do barramento
2) porta do barramento
3) nome de entidade
4) senha (opcional - se n�o for fornecida, ser� utilizado o nome de entidade)
5) arquivo onde ser� gravado o ID da sess�o de colabora��o (opcional - se n�o for fornecido, ser� utilizado 'hello_session.dat')
6) tempo de espera entre cada tentativa de acesso ao barramento (em segundos e opcional - se n�o for fornecido, ser� 1)
7) n�mero m�ximo de tentativas de acesso ao barramento (opcional - se n�o for fornecido, ser� 10)

SessionMember
1) host do barramento
2) porta do barramento
3) nome de entidade
4) senha (opcional - se n�o for fornecida, ser� usado o nome de entidade)
5) arquivo contendo o ID da sess�o de colabora��o (opcional - se n�o for fornecido, ser� utilizado 'hello_session.dat')
6) tempo de espera entre cada tentativa de acesso ao barramento (em segundos e opcional - se n�o for fornecido, ser� 1)
7) n�mero m�ximo de tentativas de acesso ao barramento (opcional - se n�o for fornecido, ser� 10)


------------------------------
---------- EXECU��O ----------
------------------------------

A demo deve ser executada na seguinte ordem:

1) SessionObserver
2) SessionMember


-------------------------------
----------- EXEMPLO -----------
-------------------------------
Supondo que os jars que a demo depende est�o em um diret�rio chamado '/openbus-sdk-java/lib':

1) java -Djava.endorsed.dirs=/openbus-sdk-java/lib/ -cp $(echo lib/*.jar | tr ' ' ':'):openbus-collaboration-demo-java-hello-observer-1.0.0.jar -Djacorb.isLocalHistoricalInterceptors=true tecgraf.openbus.services.collaboration.v1_0.SessionObserver localhost 2089 CollaborationService

2) java -Djava.endorsed.dirs=/openbus-sdk-java/lib/ -cp $(echo lib/*.jar | tr ' ' ':'):openbus-collaboration-demo-java-hello-observer-1.0.0.jar tecgraf.openbus.services.collaboration.v1_0.SessionMember localhost 2089 CollaborationService
