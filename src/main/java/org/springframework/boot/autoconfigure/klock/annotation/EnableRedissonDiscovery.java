package org.springframework.boot.autoconfigure.klock.annotation;


import org.springframework.boot.autoconfigure.klock.config.KlockAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by shaowenxing on 2017/11/23.
 * 用于启动redisson程序
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
@Import({ KlockAutoConfiguration.class})
public @interface EnableRedissonDiscovery {
}
