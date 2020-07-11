package com.github.blindpirate.annotationmagic;


import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@interface GET {
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@interface Path {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@interface Produces {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@CompositeOf({GET.class, Path.class, Produces.class})
@interface GetResource {
    @AliasFor(target = Path.class, value = "value")
    String path();

    @AliasFor(target = Produces.class, value = "value")
    String produces();
}

class MyResource {
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public String foo() {
        return "";
    }

    // Equivalent as above
    @GetResource(path = "/{id}", produces = "application/json")
    public String bar() {
        return "";
    }
}

public class ReadMeCompositionSampleTest {
    @Test
    public void test() throws NoSuchMethodException {
        assertTrue(AnnotationMagic.isAnnotationPresent(MyResource.class.getMethod("bar"), GET.class));

        Path pathAnnotation = AnnotationMagic.getOneAnnotationOnMethodOrNull(MyResource.class.getMethod("bar"), Path.class);
        assertEquals("/{id}", pathAnnotation.value());

        Produces producesAnnotation = AnnotationMagic.getOneAnnotationOnMethodOrNull(MyResource.class.getMethod("bar"), Produces.class);
        assertEquals("application/json", producesAnnotation.value());
    }
}
