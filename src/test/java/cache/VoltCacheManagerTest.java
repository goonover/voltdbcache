package cache;

import org.junit.Test;
import org.voltdb.client.ProcedureCallback;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/7/10.
 */
public class VoltCacheManagerTest {
    @Test
    public void cache() throws Exception {
        VoltCacheManager.cache("hello","helloProcedure",
                new Object[]{"helloParams"},"TestCallback");
    }

    @Test
    public void getCallbackManager() throws Exception {
        VoltCallbackManager manager = VoltCacheManager.getCallbackManager();
        assertNotNull(manager);
        ProcedureCallback callback=manager.getCallback("TestCallback");
        callback.clientCallback(null);
    }

}