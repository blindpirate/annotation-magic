package com.github.blindpirate.annotationmagic;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

@Cat("Tom")
class TestClass {
}

public class CacheabilityTest {
    @Test
    public void resultsAreCached() {
        Pet petAnnotation = AnnotationMagic.getOneAnnotationOnClassOrNull(TestClass.class, Pet.class);
        Pet petAnnotation2 = AnnotationMagic.getOneAnnotationOnClassOrNull(TestClass.class, Pet.class);
        assertSame(petAnnotation, petAnnotation2);

        petAnnotation = AnnotationMagic.cast(TestClass.class.getAnnotation(Cat.class), Pet.class);
        petAnnotation2 = AnnotationMagic.cast(TestClass.class.getAnnotation(Cat.class), Pet.class);
        assertSame(petAnnotation, petAnnotation2);

    }

    @Test
    public void canUseLruCache() {
        // A LRU cache with size 1, i.e. clean all old keys before put
        AnnotationMagician magicianWithLruCache = new AnnotationMagician(new HashMap<Object, Optional<Object>>() {
            @Override
            public Optional<Object> put(Object key, Optional<Object> value) {
                if (!containsKey(key)) {
                    clear();
                }
                return super.put(key, value);
            }
        });

        Pet petAnnotation = magicianWithLruCache.getOneAnnotationOnClassOrNull(TestClass.class, Pet.class);
        Pet petAnnotation2 = magicianWithLruCache.getOneAnnotationOnClassOrNull(TestClass.class, Pet.class);
        assertSame(petAnnotation, petAnnotation2);

        // evict
        magicianWithLruCache.getOneAnnotationOnClassOrNull(TestClass.class, Animal.class);
        // evict
        Pet petAnnotation3 = magicianWithLruCache.getOneAnnotationOnClassOrNull(TestClass.class, Pet.class);

        assertNotSame(petAnnotation, petAnnotation3);
    }

    @Test
    public void consecutiveCast() {
        List<Pet> superAnnotations = Stream.of(TestClass.class.getAnnotations())
                .filter(it -> AnnotationMagic.instanceOf(it, Pet.class))
                .map(it -> AnnotationMagic.cast(it, Pet.class))
                .collect(Collectors.toList());

        assertEquals(Collections.singletonList("Tom"),
                superAnnotations.stream()
                        .filter(it -> AnnotationMagic.instanceOf(it, Cat.class))
                        .map(it -> AnnotationMagic.cast(it, Cat.class).value())
                        .collect(Collectors.toList())
        );

        assertSame(
                AnnotationMagic.cast(superAnnotations.get(0), Cat.class),
                AnnotationMagic.cast(superAnnotations.get(0), Cat.class)
        );
    }
}
