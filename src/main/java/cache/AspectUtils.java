package cache;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 切面公用方法
 * Created by swqsh on 2017/7/11.
 */
public class AspectUtils {

    /**
     * 产生函数参数名与其参数实例的映射表
     * @param argNames  参数名数组
     * @param args      参数实例
     * @param res       函数运行返回结果
     * @return
     * @throws RuntimeException 参数名数组长度与实例数组长度不一致时，会出现该异常。按理说应该永不出现
     */
     public static Map generateNamesToObjMapper(String[] argNames,Object[] args,Object res)
            throws RuntimeException{
        if(argNames.length!=argNames.length)
            throw new RuntimeException("error happens, the length of argNames is not equal with args");
        Map<String,Object> mapper = new HashMap<String,Object>();
        for(int i=0;i<argNames.length;i++){
            mapper.put(argNames[i].trim(),args[i]);
        }
        mapper.put("result",res);
        return  mapper;
    }

    /**
     * 判断condition语句是否有效，只有当其有效时，才向voltdb中写入缓存
     * @param condition     注解中的condition语句
     * @param mapper        {@link AspectUtils#generateNamesToObjMapper(String[], Object[], Object)}产生的映射表
     * @return
     */
    public static Boolean isConditionValid(String condition,Map mapper){
        if(condition.isEmpty()||condition.equals(""))
            return true;

        VariableResolverFactory resolverFactory = new MapVariableResolverFactory(mapper);
        Boolean isValid;
        try {
            isValid = (Boolean) MVEL.eval(condition, resolverFactory);
        }catch (Exception e){
            //任何无法解析的语句都是false
            return false;
        }
        return isValid;
    }

    /**
     * 通过注解得到的参数String，解析调用voltdb存储过程所需要的参数
     * @param paramStr
     * @param mapper
     * @return
     */
    public static Object[] resolveParams(String paramStr,Map mapper){
        LinkedList paramsList = new LinkedList();
        String[] params = paramStr.split(",");
        for(int i=0;i<params.length;i++){
            //跳过所有空值
            if(params[i].equalsIgnoreCase("null"))
                continue;

            if(!mapper.containsKey(params[i])){
                paramsList.add(params[i]);
            }else {
                //参数或者返回值
                Object paramVal = mapper.get(params[i]);
                if(paramVal.getClass().isArray()){
                    if(paramVal instanceof Object[]){
                        //对于普通的Object数组，直接向列表添加即可
                        for(Object obj:(Object[]) paramVal){
                            paramsList.add(obj);
                        }
                    }else{
                        //处理参数为primitive类型数组
                        int arrLength = Array.getLength(paramVal);
                        for(int loc=0;loc<arrLength;loc++){
                            paramsList.add(Array.get(paramVal,loc));
                        }
                    }

                }else{
                    paramsList.add(paramVal);
                }
            }
        }
        Object[] res = new Object[paramsList.size()];
        for(int i=0;i<paramsList.size();i++)
            res[i] = paramsList.get(i);
        return res;
    }
}
