package cache.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;

/**
 * 使用voltdb进行缓存，默认调用procedureName的存储过程，默认参数为方法返回值，回调需要用户自己指定
 * 事实上{@link VoltCache#procedureName()}支持调用任何存储过程以及回调
 * Created by swqsh on 2017/7/4.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface VoltCache {

    //存储过程名字
    String procedureName();

    String clientName() default "default";

    String callbackName() default "NullCallback";

    String condition() default "true";

    String params()  default "result";

}
