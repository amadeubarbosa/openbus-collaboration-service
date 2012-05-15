local _G = require "_G"
local error = _G.error
local ipairs = _G.ipairs

local array = require "table"
local unpack = array.unpack or _G.unpack

local makeaux = require "openbus.core.idl.makeaux"
local parsed = require "openbus.services.collaboration.idl.parsed"



local types, const, throw = {}, {}, {}
for _, parsed in ipairs(parsed) do
	if parsed.name == "tecgraf" then
		makeaux(parsed, types, const, throw)
	end
end

local idl = {
	types = types.tecgraf.openbus.services.collaboration.v1_00,
	const = const.tecgraf.openbus.services.collaboration.v1_00,
	throw = throw.tecgraf.openbus.services.collaboration.v1_00,
}

function idl.loadto(orb)
	orb.TypeRepository.registry:register(unpack(parsed))
end

return idl