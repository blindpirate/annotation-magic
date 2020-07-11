package com.github.blindpirate.annotationmagic;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@interface Animal {
    boolean fluffy() default false;

    String name() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Extends(Animal.class)
@Animal(fluffy = true)
@interface Pet {
    String name();
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Extends(Pet.class)
@interface Cat {
    @AliasFor("name")
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Extends(Pet.class)
@interface Dog {
    String name();
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Extends(Animal.class)
@interface Rat {
    @AliasFor(target = Animal.class, value = "name")
    String value();
}

@Cat("Tom")
class MyClass {
    @Dog(name = "Spike")
    @Rat("Jerry")
    public void foo() {
    }
}


public class ReadMeInheritanceSampleTest {
    @Test
    public void test() throws NoSuchMethodException {
        Pet petAnnotation = AnnotationMagic.getOneAnnotationOnClassOrNull(MyClass.class, Pet.class);
        assertEquals("Tom", petAnnotation.name());
        assertTrue(AnnotationMagic.instanceOf(petAnnotation, Animal.class));

        Animal animalAnnotation = AnnotationMagic.getOneAnnotationOnClassOrNull(MyClass.class, Animal.class);
        assertTrue(animalAnnotation.fluffy());

        Method fooMethod = MyClass.class.getMethod("foo");
        List<Animal> animalAnnotations = AnnotationMagic.getAnnotationsOnMethod(fooMethod, Animal.class);
        assertEquals(Arrays.asList("Spike", "Jerry"), animalAnnotations.stream().map(Animal::name).collect(toList()));
    }
}
