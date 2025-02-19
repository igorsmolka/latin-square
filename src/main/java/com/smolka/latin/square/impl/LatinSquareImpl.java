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
            Set<Integer> elements = getSetOfNotNullElementsByColumn(column, square);
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

        SelectionMatrix<Integer> selectionMatrix = new SelectionMatrix<>(square, allElements, Integer.class);
        Step step = findingStep(new Step(selectionMatrix, 1));
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

        SelectionMatrix<Integer> selectionMatrix = new SelectionMatrix<>(square, allElements, Integer.class);
        Step resultStep = findingStep(new Step(selectionMatrix, limit));

        return resultStep.getResult();
    }

    private Step findingStep(Step step) {
        if (step.isLast()) {
            return step;
        }
        SelectionMatrix<Integer> currentSelectionMatrix = step.getCurrentMatrix();

        Pair<Integer, List<SelectionMatrixElement<Integer>>> minRowPair = currentSelectionMatrix.getUnfilledRowAndIndexWithMinimumVariants();
        Integer rowIndex = minRowPair.getKey();
        List<SelectionMatrixElement<Integer>> row = minRowPair.getValue();

        if (rowIndex == null) {
            step.addToResult(currentSelectionMatrix);
            return step;
        }

        Map<Integer, Set<Integer>> variantsMap = new HashMap<>();
        for (int i = 0; i < row.size(); i++) {
            SelectionMatrixElement<Integer> element = row.get(i);
            variantsMap.put(i, element.getVariants());
        }

        VariantsFinder<Integer, Integer> variantsFinder = new VariantsFinder<>();
        variantsFinder.findVariantsWithCallback(variantsMap, (newVariant) -> {
            SelectionMatrix<Integer> copySelectionMatrix = currentSelectionMatrix.getCopy();
            boolean validity = copySelectionMatrix.setRowToMatrixAndReturnValidity(rowIndex, newVariant);
            if (!validity) {
                return step.isLast();
            }

            Step newStep = step.newStep(copySelectionMatrix);
            Step result = findingStep(newStep);
            return result.isLast();
        });

        return step;
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

        for (int row = 0; row < square.length; row++) {
            int count = getNotNullElementsCountByRow(row, square);
            Set<Integer> notNullElementsSet = getSetOfNotNullElementsByRow(row, square);
            if (count != notNullElementsSet.size()) {
                return true;
            }
        }

        for (int column = 0; column < square.length; column++) {
            int count = getNotNullElementsCountByColumn(column, square);
            Set<Integer> notNullElementsSet = getSetOfNotNullElementsByColumn(column, square);
            if (count != notNullElementsSet.size()) {
                return true;
            }
        }

        return false;
    }

    private int getNotNullElementsCountByColumn(int column, Integer[][] square) {
        int counter = 0;
        for (Integer[] element : square) {
            if (element[column] != null) {
                counter++;
            }
        }
        return counter;
    }

    private int getNotNullElementsCountByRow(int row, Integer[][] square) {
        int counter = 0;
        for (int column = 0; column < square.length; column++) {
            if (square[row][column] != null) {
                counter++;
            }
        }
        return counter;
    }

    private Set<Integer> getSetOfNotNullElementsByColumn(int column, Integer[][] square) {
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

    private static class Step {

        private final SelectionMatrix<Integer> currentSelectionMatrix;

        private final int limit;

        private final List<Integer[][]> result;

        public Step(SelectionMatrix<Integer> currentSelectionMatrix, int limit) {
            this.currentSelectionMatrix = currentSelectionMatrix;
            this.limit = limit;
            this.result = new ArrayList<>();
        }

        private Step(SelectionMatrix<Integer> currentSelectionMatrix, int limit, List<Integer[][]> result) {
            this.currentSelectionMatrix = currentSelectionMatrix;
            this.limit = limit;
            this.result = result;
        }

        public void addToResult(SelectionMatrix<Integer> selectionMatrix) {
            result.add(selectionMatrix.toArray());
        }

        public Step newStep(SelectionMatrix<Integer> newSelectionMatrix) {
            return new Step(newSelectionMatrix, limit, result);
        }

        public boolean isLast() {
            return result.size() >= limit;
        }

        public SelectionMatrix<Integer> getCurrentMatrix() {
            return currentSelectionMatrix;
        }

        public List<Integer[][]> getResult() {
            return result;
        }
    }
}
