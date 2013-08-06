-- -*- coding: iso-8859-1-unix -*-

local msg = require "openbus.util.messages"

msg.ServiceVersion = "1.0.0.0"

-- openbus.core.bin.openbus
msg.CopyrightNotice = "OpenBus Collaboration Service "..
   msg.ServiceVersion.."  Copyright (C) 2013 Tecgraf, PUC-Rio"

msg.ServiceSuccessfullyStarted = "Servico de colaboracao "..
   msg.ServiceVersion.." iniciado com sucesso"

msg.CommandLineOptions = [[ [options]
Options:

  -host <address>            endere�o de rede usado pelo servi�o de colabora��o
  -port <number>             n�mero da porta usada pelo servi�o de colabora��o

  -bushost <address>         endere�o de rede de acesso ao barramento
  -busport <number>          n�mero da porta de acesso ao barramento

  -database <path>           arquivo de dados do servi�o de colabora��o
  -privatekey <path>         arquivo com chave privada do servi�o de colabora��o

  -loglevel <number>         n�vel de log gerado pelo servi�o de colabora��o
  -logfile <path>            arquivo de log gerado pelo servi�o de colabora��o
  -oilloglevel <number>      n�vel de log gerado pelo OiL (debug)
  -oillogfile <path>         arquivo de log gerado pelo OiL (debug)

  -configs <path>            arquivo de configura��es do servi�o de colabora��o
]]

msg.createCollaborationSession = "criacao da sessao {$sessionId} pelo login "
  .."{$creator}"
msg.recoverySession = "recuperacao da sessao {$sessionId} criada pelo login "
  .."{$creator}"
msg.recoveryMember = "recuperacao do membro {$name} registrado na sessao "
  .."{$sessionId} pelo login {$owner}"
msg.recoveryMemberIOR = "membro {$name} com IOR=$ior"
msg.recoveryConsumer = "recuperacao do consumidor {cookie=$cookie, ior=$ior} "..
  "registrado na sessao {$sessionId}"
msg.recoveryObserver = "recuperacao do observador {cookie=$cookie, ior=$ior} "..
  "registrado na sessao {$sessionId}"

msg.delMember = "remocao do membro {$name} registrado na sessao {$sessionId}"
msg.addMember = "registro do membro {$name} na sessao {$sessionId} pelo login "
  .."{$owner}"
msg.getMember = "retorno do membro {$name} registrado na sessao {$sessionId}"
msg.addSession = "registro da sessao {$objkey} criada pelo login {$creator}"
msg.delSession = "destruicao da sessao {$sessionId}"

msg.subscribeObserver = "registro do observador {$ior} da sessao {$sessionId}"
msg.unsubscribeObserver = "remocao do observador {cookie=$cookie} da sessao "
 .."{$sessionId}"
msg.subscribeConsumer = "registro do consumidor {$ior} na sessao {$sessionId}"
msg.unsubscribeConsumer = "remocao do consumidor {cookie=$cookie} da sessao "
 .."{$sessionId}"

msg.openDB = "sqlite.open($filename): errCode=$errCode"
msg.prepareDB = "sqlite_conn.prepare($sql): errCode=$errCode"
msg.finalizeDB = "sqlite_stmt.finalize($stmt): errCode=$errCode"

return msg
