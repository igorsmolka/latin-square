package com.smolka.latin.square.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SubSegmentVariantsOccurrenceInfo<T> {

    private final Map<Set<T>, Set<Integer>> variantsOccurrenceInfo;

    public SubSegmentVariantsOccurrenceInfo() {
        this.variantsOccurrenceInfo = new HashMap<>();
    }

    public void putOccurrenceInfo(Set<T> variants, Integer index) {
        variantsOccurrenceInfo.putIfAbsent(variants, new HashSet<>());
        variantsOccurrenceInfo.get(variants).add(index);
    }

    public boolean hasError() {
        for (Map.Entry<Set<T>, Set<Integer>> variantOccurrenceEntry : variantsOccurrenceInfo.entrySet()) {
            Set<T> variant = variantOccurrenceEntry.getKey();
            Set<Integer> indices = variantOccurrenceEntry.getValue();

            if (variant.size() < indices.size()) {
                return true;
            }
        }

        return false;
    }

    public Map<Set<T>, Set<Integer>> getVariantsWithSizeOccurrenceEquality() {
        Map<Set<T>, Set<Integer>> result = new HashMap<>();
        for (Map.Entry<Set<T>, Set<Integer>> variantOccurrenceEntry : variantsOccurrenceInfo.entrySet()) {
            Set<T> variant = variantOccurrenceEntry.getKey();
            Set<Integer> indices = variantOccurrenceEntry.getValue();

            if (variant.size() == indices.size()) {
                result.put(variant, indices);
            }
        }

        return result;
    }
}
