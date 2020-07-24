package com.github.blindpirate.annotationmagic;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

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
}
