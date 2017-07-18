package cache.annotations;

import java.lang.annotation.*;

/**
 * 从voltdb调用存储过程获取数据，并将获取到的指定数据注入到函数参数之中
 * Created by swqsh on 2017/7/17.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface VoltGet {

    String procedureName();

    String clientName() default "default";

    String params() default "";

    String condition() default "true";

    String target();
}
