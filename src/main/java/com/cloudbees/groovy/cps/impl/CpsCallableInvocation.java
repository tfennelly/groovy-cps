package com.cloudbees.groovy.cps.impl;

import com.cloudbees.groovy.cps.Block;
import com.cloudbees.groovy.cps.Continuation;
import com.cloudbees.groovy.cps.Env;
import com.cloudbees.groovy.cps.Next;

import java.util.List;

import static java.util.Arrays.*;

/**
 * Invocation of {@link CpsCallable}.
 *
 * When we invoke CPS-transformed closure or function, this throwable gets thrown.
 *
 * @author Kohsuke Kawaguchi
 */
public class CpsCallableInvocation extends Error/*not really an error but we want something that doesn't change signature*/ {
    public final CpsCallable call;
    public final Object receiver;
    public final List arguments;

    // TODO: try make this private
    public CpsCallableInvocation(CpsCallable call, Object receiver, Object... arguments) {
        this.call = call;
        this.receiver = receiver;
        this.arguments = asList(arguments);
    }

    public Next invoke(Env caller, SourceLocation loc, Continuation k) {
        return call.invoke(caller, loc, receiver,arguments,k);
    }

    /**
     * Creates a {@link Block} that performs this invocation and pass the result to the given {@link Continuation}.
     */
    public Block asBlock() {
        return new Block() {
            public Next eval(Env e, Continuation k) {
                return invoke(e, null, k);
            }
        };
    }

    /**
     * Throw a {@link CpsCallableInvocation} if and only if (iff) the {@link #receiver} object and target method
     * signature match the currently recorder CPS {@link Caller} information. Otherwise, fall through and synchronously
     * execute the transformed method code.
     *
     * <p>
     * Calls {@link Caller#isAsynchronous(Object, String, java.util.List)} to perform the check
     * described above. If the {@link #receiver} object and target method signature match the currently
     * recorder {@link Caller} information, then we can fairly safely assume that the caller of the CPS
     * transformed function (that created this {@link CpsCallableInvocation}) is in fact a CPS
     * {@code com.cloudbees.groovy.cps.sandbox.Invoker} and not some other Groovy code.
     *
     * @see {@link ContinuationGroup#methodCall(com.cloudbees.groovy.cps.Env, SourceLocation, com.cloudbees.groovy.cps.Continuation, Object, String, Object...)}
     * @see {@link Caller#record(Object, String, Object[])}
     */
    public static void throwOnAsync(CpsCallable call, Object receiver, String methodName, Object... arguments) {
        if (Caller.isAsynchronous(receiver, methodName, arguments)) {
            throw new CpsCallableInvocation(call, receiver, arguments);
        }
        // fall through.
    }
}
