package com.athaydes.sparkws;

class StringPathUtil {

    public static String parametersPath( int paths ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = 1; i <= paths; i++ ) {
            sb.append( "/{p" ).append( i ).append( "}" );
        }
        return sb.toString();
    }

    public static String[] parametersPaths( int paths ) {
        String[] result = new String[ paths ];
        for ( int i = 0; i < paths; i++ ) {
            result[ i ] = parametersPath( i + 1 );
        }
        return result;
    }

}
