package com.smolka;

import com.smolka.latin.square.impl.VariantsFinder;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VariantsTest {

    @Test
    public void testEndIsReachableNegative() {
        Map<Integer, Set<Integer>> data = Map.of(
                1, Set.of(1, 2),
                2, Set.of(1, 2),
                3, Set.of(1)
        );

        boolean isReachable = new VariantsFinder<Integer, Integer>().endIsReachable(data);
        assert !isReachable;
    }

    @Test
    public void testEndIsReachablePositive() {
        Map<Integer, Set<Integer>> data = Map.of(
                1, Set.of(1, 2, 3),
                2, Set.of(1, 4),
                3, Set.of(5, 6, 7)
        );

        boolean isReachable = new VariantsFinder<Integer, Integer>().endIsReachable(data);
        assert isReachable;
    }

    @Test
    public void testVariants() {
        Map<Integer, Set<Integer>> data = Map.of(
                1, Set.of(1, 2, 3),
                2, Set.of(1, 4),
                3, Set.of(5, 6, 7)
        );

        int countOfVariants = 15;
        Set<Map<Integer, Integer>> resultSet = new HashSet<>();

        new VariantsFinder<Integer, Integer>().findVariantsWithCallback(data, (variant) -> {
            resultSet.add(variant);
            return resultSet.size() == countOfVariants;
        });

        assert resultSet.size() == countOfVariants;
    }
}
