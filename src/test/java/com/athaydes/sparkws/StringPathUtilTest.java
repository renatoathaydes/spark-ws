package com.athaydes.sparkws;

import com.athaydes.sparkws.StringPathUtil;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class StringPathUtilTest {

    @Test
    public void testParametersPaths1() {
        String[] result = StringPathUtil.parametersPaths( 1 );
        assertArrayEquals( result, new String[]{ "/{p1}" } );
    }

    @Test
    public void testParametersPaths2() {
        String[] result = StringPathUtil.parametersPaths( 2 );
        assertArrayEquals( result, new String[]{ "/{p1}", "/{p1}/{p2}" } );
    }

    @Test
    public void testParametersPaths5() {
        String[] result = StringPathUtil.parametersPaths( 5 );
        assertArrayEquals( result, new String[]{
                "/{p1}", "/{p1}/{p2}", "/{p1}/{p2}/{p3}", "/{p1}/{p2}/{p3}/{p4}", "/{p1}/{p2}/{p3}/{p4}/{p5}" } );
    }

}
