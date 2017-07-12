package cache.aop;

import cache.AspectUtils;
import cache.VoltCacheManager;
import cache.annotations.VoltCache;
import org.apache.log4j.Logger;
import org.aspectj.lang.reflect.MethodSignature;
import org.voltdb.client.ProcedureCallback;

import java.util.Arrays;
import java.util.Map;


/**
 * {@link VoltCache}的注解的功能实现
 * Created by swqsh on 2017/7/4.
 */
public aspect voltCacheAspect {

    private Logger logger = Logger.getLogger("aspect");

    //带有回调的切点
    //pointcut cacheInvokeWithoutCallback(VoltCache cache):execution(* *(..))&&!args(..,ProcedureCallback)&&@annotation(cache);
    public pointcut cacheInvokeWithoutCallback(VoltCache cache):@annotation(cache)&&!args(..,ProcedureCallback)&&execution(* *(..));


    //不带回调的切点
    public pointcut cacheInvokeWithCallback(VoltCache cache, ProcedureCallback callback):execution(* *(..))
    &&args(..,callback)&&@annotation(cache);
    /*pointcut cacheInvokeWithCallback(VoltCache cache,ProcedureCallback callback):args(..,callback)&&@annotation(cache)
   &&execution(* *(..));*/

    /**
     * 在参数中不带有回调函数，即以NullCallback作为存储过程的回调
     */
   after(VoltCache cache) returning (Object res):cacheInvokeWithoutCallback(cache){
       Object[] args = thisJoinPoint.getArgs();
       MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
       String[] argNames = signature.getParameterNames();
       Map nameToObjMapper = AspectUtils.generateNamesToObjMapper(argNames,args,res);

       //是否应该进行缓存
       String condition = cache.condition();
       Boolean isConditionValid = AspectUtils.isConditionValid(condition,nameToObjMapper);
       if(isConditionValid!=true)
           return;

       //获取存储过程名及其参数，并进行调用
       String procedureName = cache.procedureName().trim();
       String clientName = cache.clientName().trim();
       String callbackName = cache.callbackName().trim();
       String paramStr = cache.params().trim();
       Object[] params = AspectUtils.resolveParams(paramStr,nameToObjMapper);
       System.out.println("procedureName: "+procedureName);
       System.out.println("clientName: "+clientName);
       System.out.println("callbackName: "+callbackName);
       System.out.println("params:"+ Arrays.asList(params));
       VoltCacheManager.cache(clientName,procedureName,params,callbackName);
    }

   after(VoltCache cache,ProcedureCallback callback) returning (Object res):cacheInvokeWithCallback(cache,callback){
       Object[] args = thisJoinPoint.getArgs();
       MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
       String[] argNames = signature.getParameterNames();
       Map nameToObjMapper = AspectUtils.generateNamesToObjMapper(argNames,args,res);

       //是否应该进行缓存
       String condition = cache.condition();
       Boolean isConditionValid = AspectUtils.isConditionValid(condition,nameToObjMapper);
       if(isConditionValid!=true)
           return;
       String paramStr = cache.params().trim();
       Object[] params = AspectUtils.resolveParams(paramStr,nameToObjMapper);
       String procedureName=cache.procedureName().trim();
       String clientName=cache.clientName().trim();
       VoltCacheManager.cache(clientName,procedureName,params,callback);
    }



}
