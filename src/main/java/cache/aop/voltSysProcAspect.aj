package cache.aop;

import cache.AspectUtils;
import cache.VoltCacheManager;
import cache.annotations.VoltRemove;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Map;

/**
 * 所有的系统存储过程切面均由该在此定义处理
 * Created by swqsh on 2017/7/5.
 */
public aspect voltSysProcAspect {

    public pointcut removeCall(VoltRemove voltRemove):execution(* *(..))&&@annotation(voltRemove);

    after(VoltRemove remove) returning (Object res):removeCall(remove){
        Object[] args = thisJoinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
        String[] argNames = methodSignature.getParameterNames();
        Map mapper = AspectUtils.generateNamesToObjMapper(argNames,args,res);
        String condition = remove.condition();
        Boolean isValid = AspectUtils.isConditionValid(condition,mapper);
        if(isValid==false)
            return;

        String clientName=remove.clientName().trim();
        String tableName=remove.tableName().trim().toUpperCase();
        String key=remove.key().trim();
        String value=remove.value().trim();
        String callbackName = remove.callbackName().trim();
        if(key.equals("primary_key")){
            String procedureName=tableName+".delete";
            Object[] params = AspectUtils.resolveParams(value,mapper);
            VoltCacheManager.cache(clientName,procedureName,params,callbackName);
        }else{
            String statement="delete from "+tableName+" where "+value;
            VoltCacheManager.cache(clientName,"@AdHoc",new Object[]{statement},callbackName);
        }
    }
}
