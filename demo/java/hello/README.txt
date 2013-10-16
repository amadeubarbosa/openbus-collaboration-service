A demo Hello tenta demonstrar o uso das facetas espec�ficas dos membros da sess�o
de colabora��o atrav�s da troca de messagens entre cada um dos membros. A cria��o
da sess�o de colabora��o � feita pela primeira inst�ncia do processo que se conecta
no barramento. Cada um dos membros subsequentes, por sua vez, ap�s conseguir se
conectar no barramento, realizar o login, e tenta acessar a sess�o criada pela
primeira inst�ncia do processo utilizando um arquivo com o ID da sess�o de colabora��o.
Se n�o conseguir ap�s um n�mero de tentativas, falha com uma mensagem de erro.

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

SessionMember
1) host do barramento
2) porta do barramento
3) nome de entidade

------------------------------
---------- EXECU��O ----------
------------------------------

A demo deve ser executada da seguinte forma:

1) Provider
2) Client

-------------------------------
----------- EXEMPLO -----------
-------------------------------
Supondo que os jars que a demo depende est�o em um diret�rio chamado '/openbus-sdk-java/lib':

2) java -Djava.endorsed.dirs=/openbus-sdk-java/lib/ -cp $(echo lib/*.jar | tr ' ' ':'):openbus-collaboration-demo-java-hello-1.0.0.jar tecgraf.openbus.services.collaboration.v1_0.SessionMember localhost 2089 SessionMember
