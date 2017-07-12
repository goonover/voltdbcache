package cache.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;

/**
 * 调用voltdb的系统存储过程删除
 * Created by swqsh on 2017/7/5.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface VoltRemove {

    String clientName() default "default";

    //voltdb表名
    String tableName();

    //列名，在不指定列名时默认采用主键,否则直接用value作为条件
    String key() default "primary_key";

    String value();

    String callbackName() default "NullCallback";

    //触发移除数据的条件
    String condition() default "true";

}
