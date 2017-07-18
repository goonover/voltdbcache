package cache.aop;

import cache.AspectUtils;
import cache.VoltCacheManager;
import cache.annotations.VoltGet;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.voltdb.VoltTable;
import org.voltdb.VoltTableRow;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * TODO:加一个类似VoltAbort之类的异常，否则有可能陷入死循环
 * {@link cache.annotations.VoltGet}功能的实现
 * Created by swqsh on 2017/7/17.
 */
@Aspect
public class VoltGetAspect {

    private Logger logger = Logger.getLogger(VoltGetAspect.class);

    @Around("execution(@VoltGet * *(..))")
    public Object catchGet(ProceedingJoinPoint proceedingJoinPoint){
        Object[] args = proceedingJoinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        String[] argsNames = methodSignature.getParameterNames();
        VoltGet voltGet = methodSignature.getMethod().getAnnotation(VoltGet.class);

        Map mapper = AspectUtils.generateNamesToObjMapper(argsNames,args,null);
        //判断是否需要到voltdb中获取数据
        String condition = voltGet.condition();
        Boolean isValid = AspectUtils.isConditionValid(condition,mapper);

        if(!isValid)
            try {
               return proceedingJoinPoint.proceed(args);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        ExecutionResult res = new ExecutionResult();
        CountDownLatch latch = new CountDownLatch(1);
        VoltGetCallback callback = new VoltGetCallback(proceedingJoinPoint,res,latch);

        //解析调用存储过程的参数
        String paramStr = voltGet.params();
        Object[] params = AspectUtils.resolveParams(paramStr,mapper);
        String clientName = voltGet.clientName();
        String procedureName = voltGet.procedureName();
        VoltCacheManager.cache(clientName,procedureName,params,callback);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res.getResult();
    }

}

class VoltGetCallback implements ProcedureCallback{
    //VoltGet的切点
    ProceedingJoinPoint proceedingJoinPoint;
    MethodSignature methodSignature;

    //函数运行的结果
    ExecutionResult res;
    CountDownLatch latch;
    Logger logger = Logger.getLogger(VoltGetCallback.class);

    public VoltGetCallback(ProceedingJoinPoint joinPoint,ExecutionResult result,CountDownLatch latch){
        this.proceedingJoinPoint = joinPoint;
        this.res = result;
        this.latch = latch;
        methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
    }

    @Override
    public void clientCallback(ClientResponse clientResponse) throws Exception {
        if(clientResponse.getStatus()!=ClientResponse.SUCCESS){
            logger.info(clientResponse.getStatusString());
            callOriginalMethod();
        }else{
            Object[] newArgs = injectParams(clientResponse);
            callModifiedMethod(newArgs);
        }
    }

    /**
     * 把调用存储过程得到的结果写到方法运行参数当中
     * @return  新的方法参数
     */
    private Object[] injectParams(ClientResponse clientResponse){
        VoltTable[] voltResult = clientResponse.getResults();
        String targetStr = getTargetStr();
        Object[] newArgs = proceedingJoinPoint.getArgs();
        String[] targets = targetStr.split(";");
        resolveTargets(targets,newArgs,voltResult);
        return  newArgs;
    }

    private void resolveTargets(String[] targets,Object[] newArgs,VoltTable[] voltResult){
        for(String target:targets){
            try {
                resolveTarget(target, newArgs, voltResult);
            }catch (Exception e){
                logger.info(e);
            }
        }
    }

    /**
     * 解析注解中的目标，并将符合条件的更新到新参数之中
     * @param target
     */
    private void resolveTarget(String target,Object[] newArgs,VoltTable[] voltResult){
        String[] nameAndPos = target.split(":");
        //参数数量不合法
        if(nameAndPos.length!=2)
            return;
        String name = nameAndPos[0].trim();
        String position = nameAndPos[1].trim();

        //排除所有注解中的参数名称非调用参数名称的情况
        int indexOfParam = findIndexOfParamByName(name);
        if(indexOfParam == -1)
            return;

        String[] coordinate = positionToCoordinate(position);
        if(coordinate==null)
            return;
        //得出表、行、列的坐标
        String indexOfTable = coordinate[0].trim();
        String indexOfRow = coordinate[1].trim();
        String indexOfColumn = coordinate[2].trim();

        Class[] parameterTypes = methodSignature.getParameterTypes();
        //参数为全部表且类型为VoltTable[]
        if(indexOfTable.equals("*")){
            if(indexOfRow.equals("*")&&indexOfColumn.equals("*")
                    &&parameterTypes[indexOfParam].equals(VoltTable[].class)){
                newArgs[indexOfParam] = voltResult;
            }
            return;
        }

        //表的坐标非通配符*
        int iot = Integer.parseInt(indexOfTable);
        VoltTable targetTable = voltResult[iot];

        //行的统配符为*
        if(indexOfRow.equals("*")){
            if(indexOfColumn.equals("*")){
                if(parameterTypes[indexOfParam].equals(VoltTable.class))
                    newArgs[indexOfParam] = targetTable;
                return;
            }else {
                int ioc = parseIndexOfCol(indexOfColumn,targetTable);
                if(ioc<0)
                    return;
                VoltType type = targetTable.getColumnType(ioc);
                List col = new ArrayList();
                for (int rowNum = 0; rowNum < targetTable.getRowCount(); rowNum++) {
                    targetTable.advanceToRow(rowNum);
                    col.add(targetTable.get(ioc, type));
                }
                newArgs[indexOfParam] = col;
                return;
            }
        }

        int ior = Integer.parseInt(indexOfRow);
        VoltTableRow row = targetTable.fetchRow(ior);
        if(indexOfColumn.equals("*")){
            if(parameterTypes[indexOfParam].equals(VoltTableRow.class)){
                newArgs[indexOfParam] = row;
            }
        }else{
            int ioc = parseIndexOfCol(indexOfColumn,targetTable);
            if(ioc<0)
                return;
            VoltType type = VoltType.typeFromClass(parameterTypes[indexOfParam]);
            Object arg = row.get(ioc,type);
            if(arg.getClass().equals(parameterTypes[indexOfParam]))
                newArgs[indexOfParam] = arg;
        }

    }

    private String getTargetStr(){
        VoltGet voltGet = methodSignature.getMethod().getAnnotation(VoltGet.class);
        return voltGet.target();
    }

    /**
     * 用注入后的新参数调用原有方法，如在调用过程中出现任何异常，都应该用原参数调用方法
     * @param newArgs
     */
    private void callModifiedMethod(Object[] newArgs){
        try {
           res.setResult(proceedingJoinPoint.proceed(newArgs));
        } catch (Throwable throwable) {
            logger.info(throwable);
            callOriginalMethod();
        }
        if(latch.getCount()>0)
            latch.countDown();
    }

    /**
     * 获取切面原有的参数调用原有方法
     */
    private void callOriginalMethod(){
        Object[] originalArgs = proceedingJoinPoint.getArgs();
        try {
             res.setResult(proceedingJoinPoint.proceed(originalArgs));
        } catch (Throwable throwable) {
            logger.info("callOriginalMethod failed:"+throwable);
        }finally {
            latch.countDown();
        }
    }

    /**
     * 找出参数所在的位置
     * @param name
     * @return  返回参数位置，-1表示没有找到对应参数
     */
    private int findIndexOfParamByName(String name){
        String[] parameterNames = methodSignature.getParameterNames();
        int res = -1;
        for(int pos=0;pos<parameterNames.length;pos++){
            if(parameterNames[pos].trim().equals(name))
                return pos;
        }
        return res;
    }

    /**
     * 把用String表示的位置信息解析得到三维数组
     * @param position
     * @return  如果解析失败，将返回null
     */
    private String[] positionToCoordinate(String position){
        //去除()
        int indexOfHead = position.indexOf("(")+1;
        int indexOfEnd = position.indexOf(")");
        position = position.substring(indexOfHead,indexOfEnd);
        String[] coordinate = position.split(",");
        //所有VoltTable[]的坐标系都应该是三维数组
        if(coordinate.length!=3)
            return null;
        return coordinate;
    }

    //解析表中列的位置
    private int parseIndexOfCol(String index,VoltTable table){
        int res = -1;
        try{
            res = Integer.parseInt(index);
        }catch (Exception e){
            res = table.getColumnIndex(index);
        }finally {
            return res;
        }
    }

}
