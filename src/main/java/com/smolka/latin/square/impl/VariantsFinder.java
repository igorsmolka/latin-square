package com.smolka.latin.square.impl;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class VariantsFinder<K, V> {

    public boolean endIsReachable(Map<K, Set<V>> keyValuesMap) {
        // пусть просто будет
        List<Pair<K, Set<V>>> content = keyValuesMap.entrySet().stream()
                .map(es -> Pair.of(es.getKey(), es.getValue()))
                .toList();

        for (int i = 0; i < content.size() - 1; i++) {
            Pair<K, Set<V>> pair = content.get(i);
            Set<V> currentValues = pair.getValue();

            for (int j = i + 1; j < content.size(); j++) {
                Pair<K, Set<V>> otherPair = content.get(j);
                Set<V> otherValues = otherPair.getValue();

                if (currentValues.containsAll(otherValues)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void findVariantsWithCallback(Map<K, Set<V>> keyValuesMap, Function<Map<K, V>, Boolean> callbackFunction) {
        List<Pair<K, Set<V>>> content = keyValuesMap.entrySet().stream()
                .map(es -> Pair.of(es.getKey(), es.getValue()))
                .toList();

        findCartesianProductWithoutDupesWithCallback(content, 0, new HashMap<>(), callbackFunction);
    }

    public static <K, V> List<Map<K, V>> getAllVariants(Map<K, Set<V>> keyValuesMap) {
        List<Map<K, V>> result = new ArrayList<>();

        List<Pair<K, Set<V>>> content = keyValuesMap.entrySet().stream()
                .map(es -> Pair.of(es.getKey(), es.getValue()))
                .toList();

        getCartesianProductWithoutDupes(content, 0, new HashMap<>(), result);
        return result;
    }

    private static <K, V> void getCartesianProductWithoutDupes(List<Pair<K, Set<V>>> content, int index, Map<K, V> current, List<Map<K, V>> result) {
        if (index == content.size()) {
            Set<V> values = new HashSet<>(current.values());
            if (values.size() == content.size()) {
                result.add(new HashMap<>(current));
            }
            return;
        }
        Pair<K, Set<V>> currentPair = content.get(index);
        K key = currentPair.getKey();
        Set<V> values = currentPair.getValue();

        for (V value : values) {
            current.put(key, value);
            getCartesianProductWithoutDupes(content, index + 1, current, result);
            current.remove(key);
        }
    }

    private boolean findCartesianProductWithoutDupesWithCallback(List<Pair<K, Set<V>>> content, int index, Map<K, V> current, Function<Map<K, V>, Boolean> callbackFunction) {
        if (index == content.size()) {
            Set<V> values = new HashSet<>(current.values());
            if (values.size() == content.size()) {
                return callbackFunction.apply(new HashMap<>(current));
            }
            return false;
        }

        Set<V> addedValuesSet = new HashSet<>(current.values());
        for (int i = index + 1; i < content.size(); i++) {
            if (addedValuesSet.containsAll(content.get(i).getValue())) {
                return false;
            }
        }

        Pair<K, Set<V>> currentPair = content.get(index);
        K key = currentPair.getKey();
        Set<V> values = currentPair.getValue();

        for (V value : values) {
            if (addedValuesSet.contains(value)) {
                continue;
            }
            current.put(key, value);
            if (findCartesianProductWithoutDupesWithCallback(content, index + 1, current, callbackFunction)) {
                return true;
            }
            current.remove(key);
        }

        return false;
    }
}
