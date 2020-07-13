package com.github.blindpirate.annotationmagic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The main entry point for AnnotationMagic library, which retrieves Java annotations <em>"in the magic way"</em>.
 * It supports annotation inheritance and composition.
 *
 * See <a href="https://github.com/blindpirate/annotation-magic">the documentation on GitHub</a> for more details.
 *
 * All operations in this class is fully cached by default. See {@link AnnotationMagician}.
 */
public class AnnotationMagic {
    private static final AnnotationMagician INSTANCE = new AnnotationMagician();

    /**
     * Returns the annotation of the specified {@code annotationClass} type on {@code targetClass} <em>in the magic way</em>
     * <em>if and only if one annotation can be found</em>, or else null. If more than one annotation is found, an
     * exception will be thrown.
     *
     * @param targetClass the target class to be searched for annotations
     * @param annotationClass the Class object corresponding to the annotation type
     * @param <A> the type of the annotation to query for and return if present
     * @return the target class' annotation for the specified annotation type if and only if one present on this element, else null
     * @since 0.1
     */
    public static <A extends Annotation> A getOneAnnotationOnClassOrNull(Class<?> targetClass, Class<A> annotationClass) {
        return INSTANCE.getOneAnnotationOnClassOrNull(targetClass, annotationClass);
    }

    /**
     * Returns the annotation of the specified {@code annotationClass} type on {@code method} <em>in the magic way</em>
     * <em>if and only if one annotation can be found</em>, or else null. If more than one annotation is found, an
     * exception will be thrown.
     *
     * @param method the target method to be searched for annotations
     * @param annotationClass the Class object corresponding to the annotation type
     * @param <A> the type of the annotation to query for and return if present
     * @return the target method's annotation for the specified annotation type if one present on this element, else null
     * @since 0.1
     */
    public static <A extends Annotation> A getOneAnnotationOnMethodOrNull(Method method, Class<A> annotationClass) {
        return INSTANCE.getOneAnnotationOnMethodOrNull(method, annotationClass);
    }

    /**
     * Returns all annotations of the specified {@code annotationClass} type on {@code method} <em>in the magic way</em>.
     * If no such annotations are found, an empty {@code List} will be returned.
     *
     * @param method the target method to be searched for annotations
     * @param annotationClass the Class object corresponding to the annotation type
     * @param <A> the type of the annotation to query for and return if present
     * @return the target method's annotations for the specified annotation type, empty if no such annotations found
     * @since 0.1
     */
    public static <A extends Annotation> List<A> getAnnotationsOnMethod(Method method, Class<A> annotationClass) {
        return INSTANCE.getAnnotationsOnMethod(method, annotationClass);
    }

    /**
     * Returns the annotation of the specified {@code annotationClass} type on {@code targetClass} <em>in the magic way</em>.
     * If no such annotations are found, an empty {@code List} will be returned.
     *
     * @param targetClass the target class to be searched for annotations
     * @param annotationClass the Class object corresponding to the annotation type
     * @param <A> the type of the annotation to query for and return if present
     * @return the target class' annotations for the specified annotation type, empty if no such annotations found
     * @since 0.1
     */
    public static <A extends Annotation> List<A> getAnnotationsOnClass(Class<?> targetClass, Class<A> annotationClass) {
        return INSTANCE.getAnnotationsOnClass(targetClass, annotationClass);
    }

    /**
     * Returns the annotation of the specified {@code annotationClass} type on {@code method}'s {@code i}th parameter <em>in the magic way</em>
     * <em>if and only if one annotation can be found</em>, or else null. If more than one annotation is found, an
     * exception will be thrown.
     *
     * @param method the target method to be searched for annotations
     * @param i the index of target parameter in target method's parameter list
     * @param annotationClass the Class object corresponding to the annotation type
     * @param <A> the type of the annotation to query for and return if present
     * @return the annotation for the specified annotation type if and only one present on the method's ith parameter, else null
     * @since 0.1
     */
    public static <A extends Annotation> A getOneAnnotationOnMethodParameterOrNull(Method method, int i, Class<A> annotationClass) {
        return INSTANCE.getOneAnnotationOnMethodParameterOrNull(method, i, annotationClass);
    }

    /**
     * Returns {@code true} if an annotation is subtype of specified {@code klass} annotation type <em>in the magic way</em>, {@code false} otherwise.
     * It's similar to normal {@code instanceof} operator, but works for annotations.
     *
     * @param annotation the annotation instance corresponding to the left operand of {@code instanceof}
     * @param klass the Class object corresponding to the right operand of {@code instanceof}
     * @return {@code true} if an annotation is subtype of specified {@code klass} annotation type <em>in the magic way</em>, {@code false} otherwise
     */
    public static boolean instanceOf(Annotation annotation, Class<? extends Annotation> klass) {
        return INSTANCE.instanceOf(annotation, klass);
    }

    /**
     * Returns {@code true} the annotation of the specified {@code annotationClass} type is present on {@code targetClass} <em>in the magic way</em>,
     * {@code false} otherwise.
     *
     * @param targetClass the target class to be searched for annotations
     * @param annotationClass the Class object corresponding to the annotation type
     * @return {@code true} the annotation of the specified {@code annotationClass} type is present on {@code targetClass} <em>in the magic way</em>,
     * {@code false} otherwise
     */
    public static boolean isAnnotationPresent(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        return INSTANCE.isAnnotationPresent(targetClass, annotationClass);
    }

    /**
     * Returns {@code true} the annotation of the specified {@code annotationClass} type is present on the target {@code method} <em>in the magic way</em>,
     * {@code false} otherwise.
     *
     * @param method the target method to be searched for annotations
     * @param annotationClass the Class object corresponding to the annotation type
     * @return {@code true} the annotation of the specified {@code annotationClass} type is present on the target {@code method} <em>in the magic way</em>,
     * {@code false} otherwise
     */
    public static boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationClass) {
        return INSTANCE.isAnnotationPresent(method, annotationClass);
    }
}
