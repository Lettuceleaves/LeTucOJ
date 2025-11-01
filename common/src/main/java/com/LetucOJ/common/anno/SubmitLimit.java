package com.LetucOJ.common.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubmitLimit {
    String keyPrefix() default "SUBMIT_LIMIT_";
    int limit() default 10;
    int delayLevel() default 3;
}