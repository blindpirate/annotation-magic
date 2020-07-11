# Annotation Magic - the magic of Java annotations

![annotation-magic](https://github.com/blindpirate/annotation-magic/workflows/annotation-magic/badge.svg)

Java annotation is a great feature, but it doesn't support inheritance or composition. This
library enables annotation inheritance and composition for you, aka. "the magic way", which
can greatly improve your API design.

## Annotation Inheritance

In the following example, you can see how `@Cat` extends `@Pet`, which extends `@Animal`.

You declare the inheritance relationship via `@Extends` annotation.

```
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
```

## Annotation composition

Annotation composition can simplify the annotation piling up, for example:

```
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
```

In this case, `@GetResource` is equivalent to `@GET`/`@Path`/`@Produces` present together. You can get any of them as if they exist.

## Import

This library is published to maven central:

```
Gradle:

implmentation("com.github.blindpirate:annotation-magic:0.1")

Maven:
<dependency>
    <groupId>comm.github.blindpirate</groupId>
    <artifactId>annotation-magic</artifactId>
    <version>0.1</version>
</dependency>
```