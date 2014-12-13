package com.athaydes.sparkws;

/**
 *
 */
class State {

    final SettableOnce<String> rootPath = new SettableOnce( "Root path", "/" );
    final SettableOnce<Integer> port = new SettableOnce( "Port", 8025 );
    final SettableOnce<Boolean> errorToOverride = new SettableOnce( "Error to override properties", true );

}
