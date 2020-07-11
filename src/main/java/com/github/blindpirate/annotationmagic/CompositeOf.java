package com.github.blindpirate.annotationmagic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create a "composite" annotation to simplify annotation piling up.
 * See <a href="https://github.com/blindpirate/annotation-magic">the documentation on GitHub</a> for more details.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CompositeOf {
    Class<?>[] value();
}
