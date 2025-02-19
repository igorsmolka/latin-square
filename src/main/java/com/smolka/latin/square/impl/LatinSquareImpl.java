package com.smolka.latin.square.impl;

import com.smolka.latin.square.LatinSquare;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LatinSquareImpl implements LatinSquare {

    @Override
    public boolean check(Integer[][] square) {
        Set<Integer> allElements = IntStream.range(1, square.length + 1).boxed().collect(Collectors.toSet());

        if (isInvalid(square, allElements)) {
            return false;
        }

        for (int row = 0; row < square.length; row++) {
            Set<Integer> elements = getSetOfNotNullElementsByRow(row, square);
            if (elements.size() != allElements.size()) {
                return false;
            }
        }

        for (int column = 0; column < square.length; column++) {
            Set<Integer> elements = getSetOfNotNUllElementsByColumn(column, square);
            if (elements.size() != allElements.size()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Integer[][] getFirstVariant(Integer[][] square) {
        Set<Integer> allElements = IntStream.range(1, square.length + 1).boxed().collect(Collectors.toSet());
        if (isInvalid(square, allElements)) {
            throw new RuntimeException("Square is invalid");
        }

        SearchMatrix<Integer> searchMatrix = new SearchMatrix<>(square, allElements, Integer.class);
        Step step = findingStep(new Step(searchMatrix, 1));
        if (!step.isLast()) {
            return null;
        }

        return step.getResult().getFirst();
    }

    @Override
    public List<Integer[][]> getVariantsWithLimit(Integer[][] square, int limit) {
        Set<Integer> allElements = IntStream.range(1, square.length + 1).boxed().collect(Collectors.toSet());
        if (isInvalid(square, allElements)) {
            throw new RuntimeException("Square is invalid");
        }

        SearchMatrix<Integer> searchMatrix = new SearchMatrix<>(square, allElements, Integer.class);
        Step resultStep = findingStep(new Step(searchMatrix, limit));

        return resultStep.getResult();
    }

    private Step findingStep(Step step) {
        if (step.isLast()) {
            return step;
        }
        SearchMatrix<Integer> currentSearchMatrix = step.getCurrentMatrix();

        Pair<Integer, List<SearchMatrixElement<Integer>>> minRowPair = currentSearchMatrix.getUnfilledRowAndIndexWithMinimumVariants();
        Integer rowIndex = minRowPair.getKey();
        List<SearchMatrixElement<Integer>> row = minRowPair.getValue();

        if (rowIndex == null) {
            step.addToResult(currentSearchMatrix);
            return step;
        }

        Map<Integer, Set<Integer>> variantsMap = new HashMap<>();
        for (int i = 0; i < row.size(); i++) {
            SearchMatrixElement<Integer> element = row.get(i);
            variantsMap.put(i, element.getVariants());
        }

        VariantsFinder<Integer, Integer> variantsFinder = new VariantsFinder<>();
        variantsFinder.findVariantsWithCallback(variantsMap, (newVariant) -> {
            SearchMatrix<Integer> copySearchMatrix = currentSearchMatrix.getCopy();
            boolean validity = copySearchMatrix.setRowToMatrixAndReturnValidity(rowIndex, newVariant);
            if (!validity) {
                return step.isLast();
            }

            Step newStep = step.newStep(copySearchMatrix);
            Step result = findingStep(newStep);
            return result.isLast();
        });

        return step;
    }

    private static class Step {

        private final SearchMatrix<Integer> currentSearchMatrix;

        private final int limit;

        private final List<Integer[][]> result;

        public Step(SearchMatrix<Integer> currentSearchMatrix, int limit) {
            this.currentSearchMatrix = currentSearchMatrix;
            this.limit = limit;
            this.result = new ArrayList<>();
        }

        private Step(SearchMatrix<Integer> currentSearchMatrix, int limit, List<Integer[][]> result) {
            this.currentSearchMatrix = currentSearchMatrix;
            this.limit = limit;
            this.result = result;
        }

        public void addToResult(SearchMatrix<Integer> searchMatrix) {
            result.add(searchMatrix.toArray());
        }

        public Step newStep(SearchMatrix<Integer> newSearchMatrix) {
            return new Step(newSearchMatrix, limit, result);
        }

        public boolean isLast() {
            return result.size() >= limit;
        }

        public SearchMatrix<Integer> getCurrentMatrix() {
            return currentSearchMatrix;
        }

        public List<Integer[][]> getResult() {
            return result;
        }
    }

    private boolean isInvalid(Integer[][] square, Set<Integer> allElements) {
        if (square.length == 0) {
            return true;
        }

        for (Integer[] row : square) {
            if (row.length != square.length) {
                return true;
            }
            for (Integer element : row) {
                if (element == null) {
                    continue;
                }
                if (!allElements.contains(element)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<Integer> getSetOfNotNUllElementsByColumn(int column, Integer[][] square) {
        Set<Integer> result = new HashSet<>();
        for (Integer[] element : square) {
            if (element[column] != null) {
                result.add(element[column]);
            }
        }
        return result;
    }

    private Set<Integer> getSetOfNotNullElementsByRow(int row, Integer[][] square) {
        Set<Integer> result = new HashSet<>();
        for (int column = 0; column < square.length; column++) {
            if (square[row][column] != null) {
                result.add(square[row][column]);
            }
        }
        return result;
    }
}
