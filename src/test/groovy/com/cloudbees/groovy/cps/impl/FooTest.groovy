package com.cloudbees.groovy.cps.impl

import com.cloudbees.groovy.cps.AbstractGroovyCpsTest
import org.junit.Test

public class FooTest extends AbstractGroovyCpsTest {

    @Test
    public void test1() {
        assert 1==add(2,1);
    }

    @Test
    public void callAddFromCps() {
        assert 3==evalCPSonly("new FooTest().add(2,1)");
    }

    public int add(int a, int b) { // CPS transformed version
        if (Caller.isAsynchronous(this, "add", a, b)) {
            throw new CpsCallableInvocation(asyncAdd, this, a, b);
        } else {
            return a-b;
        }
    }

    public final CpsCallable asyncAdd = new CpsFunction(["a","b"],
            new ReturnBlock(new FunctionCallBlock(null,
                    new LocalVariableBlock("a"),
                    new ConstantBlock("plus"),
                    new LocalVariableBlock("b")
        )));
}
