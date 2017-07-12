package cache.aop;

import cache.annotations.VoltCallback;
import org.apache.log4j.Logger;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

/**
 * Created by swqsh on 2017/7/12.
 */
@VoltCallback(callbackName = "query")
public class QueryCallback implements ProcedureCallback{

    Logger logger = Logger.getLogger(QueryCallback.class);

    @Override
    public void clientCallback(ClientResponse clientResponse) throws Exception {
        if(clientResponse.getStatus()!=ClientResponse.SUCCESS){
            logger.warn(clientResponse.getStatusString());
        }else{
            VoltTable[] res = clientResponse.getResults();
            for(VoltTable table:res){
                logger.info(table.toJSONString());
            }
        }
    }
}
