package cache;

import org.junit.Before;
import org.junit.Test;
import org.voltdb.client.ProcedureCallback;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/7/7.
 */
public class VoltCallbackManagerTest {

    VoltCallbackManager voltCallbackManager;

    @Before
    public void setUp() throws Exception {
        voltCallbackManager = new VoltCallbackManager();
    }

    @Test
    public void getCallback() throws Exception {
        ProcedureCallback callback = voltCallbackManager.getCallback("TestCallback");
        callback.clientCallback(null);
        voltCallbackManager.getCallback("kiki");
    }

    @Test
    public void register() throws Exception {
        boolean success = voltCallbackManager.register("TestCallback",
                new VoltCallback("TestCallback",false,TestCallback.class));
        assertFalse(success);
    }

    @Test
    public void remove() throws Exception {
        VoltCallback callback = voltCallbackManager.remove("TestCallback");
        assertNotNull(callback);
    }

}