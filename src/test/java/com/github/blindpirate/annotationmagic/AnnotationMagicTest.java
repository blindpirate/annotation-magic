package com.github.blindpirate.annotationmagic;


import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnotationMagicTest {
    @Test
    public void canGetAllAnnotationsOfSameBaseAnnotation() {
        assertEquals("Base", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithBase.class, Base.class).value());
        assertEquals("Mid", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithMid.class, Base.class).value());
        assertEquals("Sub", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithSub.class, Base.class).value());
    }

    @Test
    public void realWorldAnnotationInheritanceTest() {
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithRoute.class, Route.class).method());
        assertEquals("test", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithRoute.class, Route.class).path());

        assertEquals(HttpMethod.GET, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGet.class, Route.class).method());
        assertEquals("get", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGet.class, Route.class).path());

        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithPost.class, Route.class).method());
        assertEquals("post", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithPost.class, Route.class).path());

        assertEquals("socketjs", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithSocketJS.class, Route.class).path());

        assertEquals("intercept", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithIntercept.class, Intercept.class).path());
        assertEquals("intercept", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithIntercept.class, Route.class).path());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithIntercept.class, Intercept.class).method());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithIntercept.class, Route.class).method());
        assertEquals(InterceptType.AFTER_SUCCESS, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithIntercept.class, Intercept.class).type());

        assertEquals("prehandler", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithPreHandler.class, Intercept.class).path());
        assertEquals("prehandler", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithPreHandler.class, Route.class).path());
        assertEquals(HttpMethod.GET, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithPreHandler.class, Intercept.class).method());
        assertEquals(HttpMethod.GET, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithPreHandler.class, Route.class).method());
        assertEquals(InterceptType.PRE_HANDLER, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithPreHandler.class, Intercept.class).type());

        assertEquals("aftersuccess", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, Intercept.class).path());
        assertEquals("aftersuccess", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, Route.class).path());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, Intercept.class).method());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, Route.class).method());
        assertEquals(InterceptType.AFTER_SUCCESS, AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, Intercept.class).type());
    }

    @Test
    public void reportErrorsWhenCircularInheritanceDetected() {
        Exception exception = assertThrows(Exception.class, () -> AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithCircularAnnotation.class, CircularBase.class));
        assertTrue(exception.getMessage().contains("circular inheritance detected:"));
        assertTrue(exception.getMessage().contains("CircularMid"));
    }

    @Test
    public void reportErrorWhenMultipleAnnotationsWithSameBaseTypeFound() {
        Exception exception = assertThrows(Exception.class, () -> AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithSameBaseType.class, Base.class));
        assertTrue(exception.getMessage().contains("Found more than one annotation on class"));

        AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithSameBaseType.class, Sub.class);
    }

    @Test
    public void instanceOfTest() {
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithRoute.class, Route.class), Route.class));

        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGet.class, Route.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGet.class, Gett.class), Route.class));

        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithIntercept.class, Route.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithIntercept.class, Intercept.class), Route.class));

        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, Route.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, Intercept.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, AfterSuccess.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithAfterSuccess.class, AfterSuccess.class), Intercept.class));
    }

    @Test
    public void castTest() {
        assertEquals("get", AnnotationMagic.cast(TestClassWithGet.class.getAnnotation(Gett.class), Route.class).path());
        assertEquals("get", AnnotationMagic.cast(TestClassWithGet.class.getAnnotation(Gett.class), Route.class).path());
        assertThrows(ClassCastException.class, () ->
                AnnotationMagic.cast(TestClassWithGetJson.class.getAnnotation(GetJson.class), Route.class)
        );
    }

    @Test
    public void consecutiveCastTest() {
        List<Route> superAnnotations = Stream.of(TestClassWithGet.class.getAnnotations())
                .filter(it -> AnnotationMagic.instanceOf(it, Route.class))
                .map(it -> AnnotationMagic.cast(it, Route.class))
                .collect(Collectors.toList());

        assertEquals(Collections.singletonList("get"),
                superAnnotations.stream()
                        .filter(it -> AnnotationMagic.instanceOf(it, Gett.class))
                        .map(it -> AnnotationMagic.cast(it, Gett.class).path())
                        .collect(Collectors.toList())
        );
    }

    @Test
    public void compositeOfTest() {
        assertEquals("test", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGetJson.class, Gett.class).value());
        assertEquals("test", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGetJson.class, Gett.class).path());
        assertEquals("test", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGetJson.class, Route.class).path());
        assertEquals("", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGetJson.class, Gett.class).regex());
        assertEquals("", AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGetJson.class, Route.class).regex());
        assertTrue(AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithGetJson.class, Json.class).pretty());
        Exception exception = assertThrows(Exception.class, () -> AnnotationMagic.getOneAnnotationOnClassOrNull(TestClassWithInvalidGetJson.class, Gett.class).path());

        System.out.println(exception.getCause().getCause().getMessage());
    }
}

@Base
@Sub
class TestClassWithSameBaseType {
}

enum HttpMethod {
    GET, POST
}

enum InterceptType {
    PRE_HANDLER,
    AFTER_SUCCESS
}

@Retention(RetentionPolicy.RUNTIME)
@interface Route {
    HttpMethod method() default HttpMethod.GET;

    String path() default "";

    String regex() default "";
}


@Route(method = HttpMethod.POST, path = "test")
class TestClassWithRoute {

}

// This is not a typo. On case-insensitive OS, Get and GET in same compiler output directory might cause issues
// NoClassDefFoundError: com/github/blindpirate/annotationmagic/Get (wrong name: com/github/blindpirate/annotationmagic/GET)
@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@interface Gett {
    @AliasFor("path")
    String value() default "";

    String regex() default "";

    String path() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@interface Json {
    boolean pretty() default false;
}

@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Gett.class, Json.class})
@interface GetJson {
    @AliasFor(value = "path", target = Gett.class)
    String path() default "";

    @AliasFor(value = "regex", target = Gett.class)
    String regex() default "";

    @AliasFor(value = "pretty", target = Json.class)
    boolean pretty() default false;
}

@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Gett.class, Json.class})
@interface InvalidGetJson {
    String path() default "";

    @AliasFor(value = "pretty", target = Json.class)
    boolean pretty() default false;
}

@GetJson(path = "test", pretty = true)
class TestClassWithGetJson {
}

@InvalidGetJson(path = "test", pretty = true)
class TestClassWithInvalidGetJson {
}

@Gett(path = "get")
class TestClassWithGet {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@Route(method = HttpMethod.POST)
@interface Post {
    String path() default "";
}

@Post(path = "post")
class TestClassWithPost {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@interface SocketJS {
    String path() default "";
}

@SocketJS(path = "socketjs")
class TestClassWithSocketJS {
}


@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@Route
@interface Intercept {
    InterceptType type() default InterceptType.PRE_HANDLER;

    HttpMethod method() default HttpMethod.GET;

    String path() default "";
}

@Intercept(type = InterceptType.AFTER_SUCCESS, method = HttpMethod.POST, path = "intercept")
class TestClassWithIntercept {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Intercept.class)
@interface PreHandler {
    String path() default "";

    HttpMethod method() default HttpMethod.GET;
}

@PreHandler(path = "prehandler")
class TestClassWithPreHandler {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Intercept.class)
@Intercept(type = InterceptType.AFTER_SUCCESS)
@interface AfterSuccess {
    String path() default "";

    HttpMethod method() default HttpMethod.GET;
}

@AfterSuccess(path = "aftersuccess", method = HttpMethod.POST)
class TestClassWithAfterSuccess {
}

// +--- Get/Post/Patch/Delete
// +--- StaticResource
// +--- SocketJS
// +--- SocketJSBridge
// +--- Intercept
//   +--- PreHandler
//   +--- AfterSuccess
//   +--- AfterFailure
//   +--- AfterCompletion

@Retention(RetentionPolicy.RUNTIME)
@Extends(CircularMid.class)
@interface CircularBase {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(CircularBase.class)
@interface CircularMid {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(CircularMid.class)
@interface CircularSub {
}

@CircularSub
class TestClassWithCircularAnnotation {
}

@Base
class TestClassWithBase {
}

@Mid
class TestClassWithMid {
}

@Sub
class TestClassWithSub {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Base {
    String value() default "Base";
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Base.class)
@interface Mid {
    String value() default "Mid";
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Mid.class)
@interface Sub {
    String value() default "Sub";
}
