package com.github.blindpirate.annotationmagic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be alias for another method in AnnotationMagic annotation hierarchy to avoid
 * potential name conflict.
 *
 * See <a href="https://github.com/blindpirate/annotation-magic">the documentation on GitHub</a> for more details.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AliasFor {
    String value();

    Class<?> target() default DefaultThis.class;

    final class DefaultThis {
    }
}
