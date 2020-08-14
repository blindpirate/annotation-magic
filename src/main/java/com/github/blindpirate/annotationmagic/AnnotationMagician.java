package com.github.blindpirate.annotationmagic;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * The main implementation class of {@link AnnotationMagic}. In most cases, the static
 * methods in {@link AnnotationMagic} would be enough, but in case you need more fine-grained
 * control over the cacheability, you can use this class.
 */
public class AnnotationMagician {
    /**
     * The annotation-magic-lookup is a relatively expensive operation, so we'd better
     * cache the result as much as possible. In case you don't want the cached value to
     * stay in memory forever, you can pass a customized cache implementation, like LRU
     * cache, to the constructor.
     */
    private final Map<Object, Optional<Object>> cache;

    public AnnotationMagician() {
        this(new ConcurrentHashMap<>());
    }

    public AnnotationMagician(Map<Object, Optional<Object>> cache) {
        this.cache = cache;
    }

    public <A extends Annotation> A getOneAnnotationOnClassOrNull(Class<?> targetClass, Class<A> annotationClass) {
        return assertZeroOrOne(getAnnotationsOnClass(targetClass, annotationClass), targetClass);
    }

    public <A extends Annotation> A getOneAnnotationOnMethodOrNull(Method method, Class<A> targetAnnotationClass) {
        return assertZeroOrOne(getAnnotationsOnMethod(method, targetAnnotationClass), method);
    }

    public <A extends Annotation> List<A> getAnnotationsOnMethod(Method method, Class<A> targetAnnotationClass) {
        return getCached(Arrays.asList(1, method, targetAnnotationClass),
                () -> getAnnotations(method.getAnnotations(), targetAnnotationClass));
    }

    public <A extends Annotation> List<A> getAnnotationsOnClass(Class<?> targetClass, Class<A> targetAnnotationClass) {
        return getCached(Arrays.asList(2, targetClass, targetAnnotationClass),
                () -> getAnnotations(targetClass.getAnnotations(), targetAnnotationClass));
    }

    public <A extends Annotation> A getOneAnnotationOnMethodParameterOrNull(Method method, int index, Class<A> targetAnnotation) {
        return getCached(Arrays.asList(3, method, index, targetAnnotation),
                () -> assertZeroOrOne(getAnnotations(method.getParameterAnnotations()[index], targetAnnotation), method));
    }

    public boolean instanceOf(Annotation annotation, Class<? extends Annotation> klass) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return getCached(Arrays.asList(4, annotationType), () -> getAnnotationHierarchy(annotationType)).contains(klass);
    }

    public <A extends Annotation> A cast(Annotation annotation, Class<A> targetAnnotation) {
        A ret = getCached(Arrays.asList(5, annotation, targetAnnotation), () -> examineAnnotation(annotation, targetAnnotation));
        if (ret == null) {
            throw new ClassCastException("Can't cast " + annotation + " to class " + targetAnnotation + "!");
        }
        return ret;
    }

    public boolean isAnnotationPresent(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        return !getAnnotationsOnClass(targetClass, annotationClass).isEmpty();
    }

    public boolean isAnnotationPresent(Method targetMethod, Class<? extends Annotation> annotationClass) {
        return !getAnnotationsOnMethod(targetMethod, annotationClass).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(List<Object> keys, Supplier<T> supplier) {
        Optional<Object> ret = cache.get(keys);
        if (ret == null) {
            ret = Optional.ofNullable(supplier.get());
            cache.put(keys, ret);
        }
        return (T) ret.orElse(null);
    }

    private <A extends Annotation> List<A> getAnnotations(Annotation[] annotations, Class<A> targetClass) {
        return Stream.of(annotations)
                .flatMap(this::expandAnnotation)
                .map(annotation -> examineAnnotation(annotation, targetClass))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private int indexOf(Annotation[] annotations, Class<? extends Annotation> targetAnnotation) {
        for (int i = 0; i < annotations.length; ++i) {
            if (annotations[i].annotationType() == targetAnnotation) {
                return i;
            }
        }
        return -1;
    }

    private Stream<Annotation> expandAnnotation(Annotation annotation) {
        Annotation[] annotationsOnTargetAnnotationType = annotation.annotationType().getAnnotations();

        int compositeOfIndex = indexOf(annotationsOnTargetAnnotationType, CompositeOf.class);
        int extendsIndex = indexOf(annotationsOnTargetAnnotationType, Extends.class);

        if (compositeOfIndex == -1) {
            return Stream.of(annotation);
        }

        LinkedList<Annotation> result = Stream.of(((CompositeOf) annotationsOnTargetAnnotationType[compositeOfIndex]).value())
                .map(klass -> (Annotation) Proxy.newProxyInstance(klass.getClassLoader(), new Class[]{klass}, new InvocationHandler() {
                    Map<String, Object> cache = new HashMap<>();

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("annotationType".equals(method.getName())) {
                            return klass;
                        }
                        if ("toString".equals(method.getName())) {
                            return "@" + klass.toString();
                        }
                        Object result = cache.get(method.getName());
                        if (result != null) {
                            return result;
                        }

                        for (Method methodInCompositeAnnotation : annotation.annotationType().getMethods()) {
                            AliasFor aliasFor = methodInCompositeAnnotation.getAnnotation(AliasFor.class);
                            if (aliasFor != null && (isDirectAlias(aliasFor, method) || isIndirectAlias(aliasFor, method))) {
                                result = methodInCompositeAnnotation.invoke(annotation);
                                cache.put(method.getName(), result);
                                return result;
                            }
                        }

                        try {
                            Object ret = klass.getMethod(method.getName()).getDefaultValue();
                            if (ret == null) {
                                throw new IllegalStateException("Can't invoke " + klass.getName() + "." + method.getName() + "() on composite annotation " + annotation);
                            }
                            return ret;
                        } catch (NoSuchMethodError e) {
                            throw new IllegalStateException("Can't invoke " + klass.getName() + "." + method.getName() + "() on composite annotation " + annotation, e);
                        }
                    }

                    /*
                    @interface Get {
                        @AliasFor("path")
                        String value() default "";

                        String path() default "";
                    }


                    @CompositeOf({Get.class, Json.class})
                    @interface GetJson {
                        @AliasFor(value = "path", target = Get.class)
                        String path() default "";

                        @AliasFor(value = "pretty", target = Json.class)
                        boolean pretty() default false;
                    }

                    GetJson.path() is direct alias for Get.value()
                    */
                    private boolean isIndirectAlias(AliasFor aliasFor, Method methodBeingInvoked) {
                        if (aliasFor.target() != klass) {
                            return false;
                        }
                        AliasFor redirect = methodBeingInvoked.getAnnotation(AliasFor.class);
                        return redirect != null && redirect.target() == AliasFor.DefaultThis.class && redirect.value().equals(aliasFor.value());
                    }

                    /*
                    @CompositeOf({Gett.class, Json.class})
                    @interface GetJson {
                        @AliasFor(value = "path", target = Get.class)
                        String path() default "";

                        @AliasFor(value = "pretty", target = Json.class)
                        boolean pretty() default false;
                    }

                    GetJson.path() is direct alias for Get.path()
                    */
                    private boolean isDirectAlias(AliasFor aliasFor, Method methodBeingInvoked) {
                        return aliasFor.target() == klass && aliasFor.value().equals(methodBeingInvoked.getName());
                    }
                })).collect(Collectors.toCollection(LinkedList::new));
        if (extendsIndex != -1) {
            if (extendsIndex < compositeOfIndex) {
                result.addFirst(annotation);
            } else {
                result.add(annotation);
            }
        }
        return result.stream();
    }

    /*
     * Walk along `@Extends` annotation hierarchy to get all annotations.
     */
    private static LinkedHashSet<Class<? extends Annotation>> getAnnotationHierarchy(Class<? extends Annotation> klass) {
        Class<? extends Annotation> currentClass = klass;
        LinkedHashSet<Class<? extends Annotation>> hierarchy = new LinkedHashSet<>();
        while (currentClass != null) {
            if (!hierarchy.add(currentClass)) {
                throw new IllegalArgumentException("Annotation hierarchy circular inheritance detected: " + currentClass);
            }
            currentClass = getSuperAnnotationOrNull(currentClass);
        }

        return hierarchy;
    }

    private static <A extends Annotation> A assertZeroOrOne(List<A> annotations, Object target) {
        if (annotations.size() > 1) {
            throw new IllegalArgumentException("Found more than one annotation on " + target + ":\n"
                    + annotations.stream().map(Annotation::toString).collect(joining("\n")));
        }

        return annotations.isEmpty() ? null : annotations.get(0);
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A examineAnnotation(Annotation actual, Class<A> targetAnnotationClass) {
        actual = getActualAnnotationBehindProxy(actual);
        // Two passes:
        // 1. scan all annotation hierarchy classes
        // 2. construct a proxy with all information (probably overridden by sub annotations)
        LinkedHashSet<Class<? extends Annotation>> hierarchy = getAnnotationHierarchy(actual.annotationType());

        if (!hierarchy.contains(targetAnnotationClass)) {
            return null;
        }

        return (A) Proxy.newProxyInstance(targetAnnotationClass.getClassLoader(), new Class[]{targetAnnotationClass, AnnotationAdapter.class},
                new AnnotationAdapterProxy<A>(actual, targetAnnotationClass, hierarchy));
    }

    private static Annotation getActualAnnotationBehindProxy(Annotation annotation) {
        if (annotation instanceof AnnotationAdapter) {
            return ((AnnotationAdapter) annotation).getActualAnnotation();
        } else {
            return annotation;
        }
    }

    interface AnnotationAdapter {
        Annotation getActualAnnotation();
    }

    private static class AnnotationAdapterProxy<A extends Annotation> implements InvocationHandler {
        private final Annotation actual;
        private final Class<A> targetAnnotationClass;
        private final LinkedHashSet<Class<? extends Annotation>> actualAnnotationHierarchy;
        private Map<String, Optional<Object>> methodsCache = new ConcurrentHashMap<>();

        AnnotationAdapterProxy(Annotation actual, Class<A> targetAnnotationClass, LinkedHashSet<Class<? extends Annotation>> actualAnnotationHierarchy) {
            this.actual = actual;
            this.targetAnnotationClass = targetAnnotationClass;
            this.actualAnnotationHierarchy = actualAnnotationHierarchy;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == AnnotationAdapter.class) {
                return actual;
            }

            if ("hashCode".equals(method.getName())) {
                return getActualAnnotationBehindProxy(actual).hashCode();
            }

            if ("equals".equals(method.getName()) && method.getParameters().length == 1) {
                if (args[0] instanceof Annotation) {
                    return actual.equals(getActualAnnotationBehindProxy((Annotation) args[0]));
                } else {
                    return actual.equals(args[0]);
                }
            }

            Optional<Object> cachedField = methodsCache.get(method.getName());
            if (cachedField == null) {
                cachedField = searchInHierarchy(actual, targetAnnotationClass, actualAnnotationHierarchy, method.getName());
                methodsCache.put(method.getName(), cachedField);
            }
            return cachedField.orElse(null);
        }
    }

    private static Optional<Object> searchInHierarchy(Annotation actual, Class<? extends Annotation> targetAnnotationClass, LinkedHashSet<Class<? extends Annotation>> hierarchy, String name) {
        try {
            Method method = actual.annotationType().getMethod(name);
            return Optional.of(safeInvokeAnnotationMethod(method, actual));
        } catch (NoSuchMethodException e) {
            // search for AliasFor in same annotation type
            for (Method method : actual.annotationType().getMethods()) {
                AliasFor aliasFor = method.getAnnotation(AliasFor.class);
                if (aliasFor != null && (aliasFor.target() == AliasFor.DefaultThis.class || aliasFor.target() == targetAnnotationClass) && name.equals(aliasFor.value())) {
                    // Bingo! We found it!
                    return Optional.of(safeInvokeAnnotationMethod(method, actual));
                }
            }

            // search in super annotation type
            for (Class<? extends Annotation> klass : hierarchy) {
                Annotation[] annotationsOnCurrentAnnotationClass = klass.getAnnotations();
                for (Annotation annotationOnCurrentAnnotationClass : annotationsOnCurrentAnnotationClass) {
                    if (hierarchy.contains(annotationOnCurrentAnnotationClass.annotationType())) {
                        try {
                            Method method = annotationOnCurrentAnnotationClass.annotationType().getMethod(name);
                            return Optional.of(safeInvokeAnnotationMethod(method, annotationOnCurrentAnnotationClass));
                        } catch (NoSuchMethodException ignored) {
                            break;
                        }
                    }
                }
            }
            try {
                Method method = targetAnnotationClass.getMethod(name);
                return Optional.of(method.getDefaultValue());
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Object safeInvokeAnnotationMethod(Method method, Annotation annotation) {
        try {
            return method.invoke(annotation, new Object[]{});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<? extends Annotation> getSuperAnnotationOrNull(Class<? extends Annotation> currentClass) {
        Extends extendsAnnotation = currentClass.getAnnotation(Extends.class);
        return extendsAnnotation == null ? null : extendsAnnotation.value();
    }

}
