package com.github.blindpirate.annotationmagic;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ExceptionUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.hamcrest.core.StringContains.containsString;

@Router("/test")
@Application
public class CompositeToStringTest {
    @Test
    public void throwExceptionsWhenTwoAnnotationsAreFound() {
        Exception e = Assertions.assertThrows(Exception.class, () -> AnnotationMagic.getOneAnnotationOnClassOrNull(CompositeToStringTest.class, Router.class));
        MatcherAssert.assertThat(ExceptionUtils.readStackTrace(e), containsString("Found more than one annotation on class com.github.blindpirate.annotationmagic.CompositeToStringTest"));
    }

    @Test
    public void canGetTwoRouters() {
        Assertions.assertEquals(2, AnnotationMagic.getAnnotationsOnClass(CompositeToStringTest.class, Router.class).size());
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Component {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Router {
    String value() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Component.class, Router.class})
@interface Application {
}
