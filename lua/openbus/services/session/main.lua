-- $Id$

local _G = require "_G"
local assert = _G.assert
local select = _G.select

local io = require "io"
local stderr = io.stderr

local os = require "os"
local getenv = os.getenv

local oil = require "oil"
local oillog = require "oil.verbose"

local log = require "openbus.util.logger"
local database = require "openbus.util.database"
local opendb = database.open
local server = require "openbus.util.server"
local ConfigArgs = server.ConfigArgs
local newSCS = server.newSCS
local setuplog = server.setuplog
local readprivatekey = server.readprivatekey

local openbus = require "openbus"

local msg = require "openbus.services.session.messages"
local idl = require "openbus.services.session.idl"
local SessionServiceName = idl.const.SessionServiceName
local loadidl = idl.loadto
local SessionRegistry = require "openbus.services.session.SessionRegistry"

return function(...)
	-- configuration parameters parser
	local Configs = ConfigArgs{
		host = "*",
		port = 2090,
		
		bushost = "localhost",
		busport = 2089,
	
		database = "session.db",
		privatekey = "session.key",
	
		loglevel = 3,
		logfile = "",
		oilloglevel = 0,
		oillogfile = "",
	}

	-- parse configuration file
	Configs:configs("configs", getenv("OPENBUS_SESSION_CONFIG") or "session.cfg")

	-- parse command line parameters
	do
		io.write(msg.CopyrightNotice, "\n")
		local argidx, errmsg = Configs(...)
		if not argidx or argidx <= select("#", ...) then
			if errmsg ~= nil then
				stderr:write(errmsg,"\n")
			end
			stderr:write([[
Usage:  ]],OPENBUS_PROGNAME,[[ [options]
Options:

  -host <address>            endere�o de rede usado pelo servi�o de sess�o
  -port <number>             n�mero da porta usada pelo servi�o de sess�o

  -bushost <address>         endere�o de rede de acesso ao barramento
  -busport <number>          n�mero da porta de acesso ao barramento

  -database <path>           arquivo de dados do servi�o de sess�o
  -privatekey <path>         arquivo com chave privada do servi�o de sess�o

  -loglevel <number>         n�vel de log gerado pelo servi�o de sess�o
  -logfile <path>            arquivo de log gerado pelo servi�o de sess�o
  -oilloglevel <number>      n�vel de log gerado pelo OiL (debug)
  -oillogfile <path>         arquivo de log gerado pelo OiL (debug)

  -configs <path>            arquivo de configura��es do servi�o de sess�o

]])
			return 1 -- program's exit code
		end
	end

	-- setup log files
	setuplog(log, Configs.loglevel, Configs.logfile)
	setuplog(oillog, Configs.oilloglevel, Configs.oillogfile)

	-- connect to the bus
	local conn = openbus.connectByAddress(Configs.bushost, Configs.busport)

	-- create SCS component
	local orb = conn.orb
	loadidl(orb)
	newSCS{
		orb = orb,
		objkey = SessionServiceName,
		name = SessionServiceName,
		facets = SessionRegistry,
		init = function()
			SessionRegistry.SessionRegistry:__init({
				connection = conn,
				database = assert(opendb(Configs.database)),
				privateKey = assert(readprivatekey(Configs.privatekey)),
			})
		end,
	}

	-- start ORB
	log:uptime(msg.ServiceSuccessfullyStarted)
	orb:run()
end
