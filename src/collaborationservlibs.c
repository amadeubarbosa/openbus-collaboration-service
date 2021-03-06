#include "extralibraries.h"

#include "lua.h"
#include "lauxlib.h"

#include "luuid.h"
#include "lce.h"
#include "lfs.h"
#include "luavararg.h"
#include "luastruct.h"
#include "luasocket.h"
#include "loop.h"
#include "luatuple.h"
#include "luacothread.h"
#include "luaidl.h"
#include "oil.h"
#include "luascs.h"
#include "luaopenbus.h"
#include "lsqlite3.h"
#include <collaborationService.h>

char const* OPENBUS_MAIN = "openbus.services.collaboration.main";
char const* OPENBUS_PROGNAME = "collaborationService";

void luapreload_extralibraries(lua_State *L)
{
	/* preload binded C libraries */
  luaL_getsubtable(L, LUA_REGISTRYINDEX, "_PRELOAD");
	lua_pushcfunction(L,luaopen_uuid);lua_setfield(L,-2,"uuid");
	lua_pushcfunction(L,luaopen_lfs);lua_setfield(L,-2,"lfs");
	lua_pushcfunction(L,luaopen_vararg);lua_setfield(L,-2,"vararg");
	lua_pushcfunction(L,luaopen_struct);lua_setfield(L,-2,"struct");
	lua_pushcfunction(L,luaopen_socket_core);lua_setfield(L,-2,"socket.core");
	lua_pushcfunction(L,luaopen_lsqlite3);lua_setfield(L,-2,"lsqlite3");
	lua_pop(L, 1);  /* pop 'package.preload' table */
	/* preload other C libraries */
	luapreload_lce(L);
	/* preload script libraries */
	luapreload_loop(L);
	luapreload_luatuple(L);
	luapreload_luacothread(L);
	luapreload_luaidl(L);
	luapreload_oil(L);
	luapreload_luascs(L);
	luapreload_luaopenbus(L);
	luapreload_collaborationService(L);
}
