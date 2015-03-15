package com.cloudbees.groovy.cps.impl

import com.cloudbees.groovy.cps.AbstractGroovyCpsTest
import org.junit.Test

public class AsyncCPSCallstackCheckTest extends AbstractGroovyCpsTest {

    @Test
    public void test1() {
        assert 1==compute(2,1);
    }

    @Test
    public void callAddFromCps() {
        assert 3==evalCPSonly("new AsyncCPSCallstackCheckTest().compute(2,1)");
    }

    public int compute(int a, int b) { // CPS transformed version
        new CpsCallableInvocation(asyncAdd, this, a, b).throwOnAsync("compute");
        return a-b;
    }

    public final CpsCallable asyncAdd = new CpsFunction(["a","b"],
            new ReturnBlock(new FunctionCallBlock(null,
                    new LocalVariableBlock("a"),
                    new ConstantBlock("plus"),
                    new LocalVariableBlock("b")
        )));

    @Test
    public void testNestedCPSCall() {
        evalCPSonly('''

        ''');
    }

    public static class Ping {
        int callCount = 0;
    }

    public static void ping(Ping p) {
        p.pong();
        p.pong();
    }

    @Test
    public void testNonEach() {
        def endCount = evalCPS('''
            def innerPing = new AsyncCPSCallstackCheckTest.Ping() {
                  void pong() {
                    callCount++;
                  }
                };
            AsyncCPSCallstackCheckTest.ping(innerPing);
            return innerPing.callCount;
        ''');

        assertEquals(2, endCount);
    }


    @Test
    public void testEach() {

    }
}
