package com.smolka.latin.square.impl;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchMatrix<T> {

    private final List<List<SearchMatrixElement<T>>> matrix;

    private final Set<Integer> unfilledRows;

    private final Set<T> allElements;

    private final int size;

    private final Class<T> clazz;

    private SearchMatrix(List<List<SearchMatrixElement<T>>> matrix,
                         Set<T> allElements,
                         Set<Integer> unfilledRows,
                         int size,
                         Class<T> clazz) {
        this.matrix = matrix;
        this.allElements = allElements;
        this.unfilledRows = unfilledRows;
        this.size = size;
        this.clazz = clazz;
    }

    public SearchMatrix(T[][] field,
                        Set<T> allElements,
                        Class<T> clazz) {
        this.clazz = clazz;

        if (field.length == 0) {
            throw new RuntimeException();
        }
        for (T[] row : field) {
            if (row.length != field.length) {
                throw new RuntimeException();
            }
        }

        this.size = field.length;
        this.matrix = new ArrayList<>();
        this.allElements = allElements;
        this.unfilledRows = new HashSet<>();

        for (int row = 0; row < field.length; row++) {
            List<SearchMatrixElement<T>> rowMatrix = new ArrayList<>();
            matrix.add(rowMatrix);
            for (int column = 0; column < field[row].length; column++) {
                if (field[row][column] == null) {
                    rowMatrix.add(new SearchMatrixElement<>(row, column));
                } else {
                    if (!allElements.contains(field[row][column])) {
                        throw new RuntimeException("");
                    }
                    rowMatrix.add(new SearchMatrixElement<>(row, column, new HashSet<>(Set.of(field[row][column]))));
                }
            }
        }

        for (int rowIndex = 0; rowIndex < matrix.size(); rowIndex++) {
            List<SearchMatrixElement<T>> row = matrix.get(rowIndex);
            for (SearchMatrixElement<T> elem : row) {
                if (!elem.isStrong()) {
                    unfilledRows.add(rowIndex);
                    break;
                }
            }
        }

        prepareMatrix();
    }

    public T[][] toArray() {
        if (!isFilled()) {
            throw new RuntimeException();
        }

        @SuppressWarnings("unchecked")
        T[][] result = (T[][]) Array.newInstance(clazz, size, size);;
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                result[row][column] = getElement(row, column).getStrongValue();
            }
        }

        return result;
    }

    public SearchMatrixElement<T> getElement(int row, int column) {
        return matrix.get(row).get(column);
    }

    public boolean isFilled() {
        return unfilledRows.isEmpty();
    }

    public Pair<Integer, List<SearchMatrixElement<T>>> getUnfilledRowAndIndexWithMinimumVariants() {
        if (unfilledRows.isEmpty()) {
            return Pair.of(null, null);
        }

        int resultIndex = unfilledRows.stream().findFirst().orElseThrow();
        int min = Integer.MAX_VALUE;

        for (int unfilledRowIndex : unfilledRows) {
            int allVariantsSize = matrix.get(unfilledRowIndex)
                    .stream()
                    .map(SearchMatrixElement::getVariantsSize)
                    .reduce(Integer::sum)
                    .orElseThrow();

            int avgSize = (allVariantsSize / size);

            if (avgSize < min) {
                resultIndex = unfilledRowIndex;
                min = avgSize;
            }
        }

        return Pair.of(resultIndex, matrix.get(resultIndex));
    }

    public SearchMatrix<T> getCopy() {
        List<List<SearchMatrixElement<T>>> newMatrix = new ArrayList<>();
        Set<Integer> unfilledRows = new HashSet<>(this.unfilledRows);
        for (List<SearchMatrixElement<T>> row : matrix) {
            List<SearchMatrixElement<T>> newRow = new ArrayList<>();
            newMatrix.add(newRow);
            for (SearchMatrixElement<T> searchMatrixElement : row) {
                newRow.add(searchMatrixElement.copy());
            }
        }

        return new SearchMatrix<>(newMatrix, allElements, unfilledRows, size, clazz);
    }

    public boolean setRowToMatrixAndReturnValidity(int rowIndex, Map<Integer, T> rowToSet) {
        if (unfilledRows.isEmpty()) {
            throw new RuntimeException("");
        }

        if (rowToSet.size() == size) {
            unfilledRows.remove(rowIndex);
        }
        List<SearchMatrixElement<T>> rowInMatrix = matrix.get(rowIndex);

        List<RowInfo<T>> unfilledMatrix = getUnfilledRowsInOrder();

        for (int valueIndex = 0; valueIndex < rowToSet.size(); valueIndex++) {
            T value = rowToSet.get(valueIndex);
            rowInMatrix.get(valueIndex).resetVariants(value);

            for (RowInfo<T> unfilledRow : unfilledMatrix) {
                int unfilledRowIndex = unfilledRow.rowIndex();
                if (unfilledRowIndex == rowIndex) {
                    continue;
                }

                List<SearchMatrixElement<T>> unfilledRowValues = unfilledRow.rowValues();
                unfilledRowValues.get(valueIndex).removeVariant(value);
            }
        }

        boolean wasChanges = false;
        do {
            SubSegmentValidityResult rowProcessResult = postProcessRowsSubSegmentsAndReturnValidity(unfilledMatrix);
            if (!rowProcessResult.isValid()) {
                return false;
            }
            wasChanges = rowProcessResult.wasChanges();

            SubSegmentValidityResult columnProcessResult = postProcessColumnsSubSegmentsAndReturnValidity(unfilledMatrix);
            if (!columnProcessResult.isValid()) {
                return false;
            }
            if (columnProcessResult.wasChanges()) {
                wasChanges = true;
            }
        } while (wasChanges);

        for (RowInfo<T> unfilledRow : unfilledMatrix) {
            boolean reallyUnfilled = unfilledRow.rowValues().stream().anyMatch(e -> !e.isStrong());
            if (!reallyUnfilled) {
                unfilledRows.remove(unfilledRow.rowIndex());
            }
        }

        return true;
    }

    private SubSegmentValidityResult postProcessColumnsSubSegmentsAndReturnValidity(List<RowInfo<T>> matrix) {
        boolean wasChanges = false;
        for (int columnIndex = 0; columnIndex < size; columnIndex++) {
            List<SearchMatrixElement<T>> column = new ArrayList<>();
            for (RowInfo<T> row : matrix) {
                column.add(row.rowValues().get(columnIndex));
            }

            SubSegmentValidityResult processResult = postProcessSubSegmentAndReturnValidity(column);

            if (!processResult.isValid) {
                return new SubSegmentValidityResult(false, false);
            }

            if (processResult.wasChanges) {
                wasChanges = true;
            }
        }

        return new SubSegmentValidityResult(true, wasChanges);
    }

    private SubSegmentValidityResult postProcessRowsSubSegmentsAndReturnValidity(List<RowInfo<T>> matrix) {
        boolean wasChanges = false;
        for (RowInfo<T> row : matrix) {
            SubSegmentValidityResult processResult = postProcessSubSegmentAndReturnValidity(row.rowValues());
            if (!processResult.isValid) {
                return new SubSegmentValidityResult(false, false);
            }

            if (processResult.wasChanges) {
                wasChanges = true;
            }
        }

        return new SubSegmentValidityResult(true, wasChanges);
    }

    private SubSegmentValidityResult postProcessSubSegmentAndReturnValidity(List<SearchMatrixElement<T>> subSegment) {
        SubSegmentVariantsOccurrenceInfo<T> subSegmentVariantsOccurrenceInfo = new SubSegmentVariantsOccurrenceInfo<>();
        for (int index = 0; index < subSegment.size(); index++) {
            subSegmentVariantsOccurrenceInfo.putOccurrenceInfo(subSegment.get(index).getVariants(), index);
        }

        if (subSegmentVariantsOccurrenceInfo.hasError()) {
            return new SubSegmentValidityResult(false, false);
        }

        Map<Set<T>, Set<Integer>> variantsWithSizeOccurrenceEquality = subSegmentVariantsOccurrenceInfo.getVariantsWithSizeOccurrenceEquality();
        boolean wasChanges = false;

        for (Map.Entry<Set<T>, Set<Integer>> variantWithSizeOccurrenceEqualityEntry : variantsWithSizeOccurrenceEquality.entrySet()) {
            Set<T> variant = variantWithSizeOccurrenceEqualityEntry.getKey();
            Set<Integer> onIndexes = variantWithSizeOccurrenceEqualityEntry.getValue();

            for (int otherIndex = 0; otherIndex < subSegment.size(); otherIndex++) {
                if (onIndexes.contains(otherIndex)) {
                    continue;
                }

                SearchMatrixElement<T> otherElement = subSegment.get(otherIndex);
                if (otherElement.removeVariants(variant)) {
                    wasChanges = true;
                }
            }
        }

        if (wasChanges) {
            SubSegmentValidityResult newResult = postProcessSubSegmentAndReturnValidity(subSegment);
            if (!newResult.isValid()) {
                return new SubSegmentValidityResult(false, false);
            }
            return new SubSegmentValidityResult(true, wasChanges);
        }

        return new SubSegmentValidityResult(true, wasChanges);
    }

    private List<RowInfo<T>> getUnfilledRowsInOrder() {
        List<RowInfo<T>> result = new ArrayList<>();
        List<Integer> unfilledRowsIndicesInOrder = unfilledRows.stream().sorted().toList();

        for (Integer unfilledRowIndex : unfilledRowsIndicesInOrder) {
            result.add(new RowInfo<>(unfilledRowIndex, matrix.get(unfilledRowIndex)));
        }

        return result;
    }

    private void prepareMatrix() {
        for (int row = 0; row < matrix.size(); row++) {
            List<SearchMatrixElement<T>> emptyElementsByRow = getEmptyElementsByRow(row, matrix);
            for (SearchMatrixElement<T> emptyElementInRow : emptyElementsByRow) {
                List<SearchMatrixElement<T>> constraints = getStrongElementsByElementSegment(emptyElementInRow, matrix);
                Set<T> valuesFromConstraints = constraints.stream().map(SearchMatrixElement::getStrongValue).collect(Collectors.toSet());
                Set<T> newVariants = new HashSet<>(allElements);
                newVariants.removeAll(valuesFromConstraints);
                if (newVariants.isEmpty()) {
                    throw new RuntimeException("");
                }

                emptyElementInRow.resetVariants(newVariants);
            }
        }
    }

    private List<SearchMatrixElement<T>> getStrongElementsByElementSegment(SearchMatrixElement<T> element, List<List<SearchMatrixElement<T>>> matrix) {
        List<SearchMatrixElement<T>> result = new ArrayList<>();
        int row = element.getRow();
        int column = element.getColumn();

        for (int rowIndex = 0; rowIndex < matrix.size(); rowIndex++) {
            if (rowIndex == row) {
                continue;
            }
            SearchMatrixElement<T> matrixElem = matrix.get(rowIndex).get(column);
            if (matrixElem.isStrong()) {
                result.add(matrixElem);
            }
        }

        for (int columnIndex = 0; columnIndex < matrix.size(); columnIndex++) {
            if (columnIndex == column) {
                continue;
            }
            SearchMatrixElement<T> matrixElem = matrix.get(row).get(columnIndex);
            if (matrixElem.isStrong()) {
                result.add(matrixElem);
            }
        }

        return result;
    }

    private List<SearchMatrixElement<T>> getEmptyElementsByRow(int row, List<List<SearchMatrixElement<T>>> matrix) {
        List<SearchMatrixElement<T>> result = new ArrayList<>();

        for (int column = 0; column < matrix.size(); column++) {
            if (matrix.get(row).get(column).isEmpty()) {
                result.add(matrix.get(row).get(column));
            }
        }
        return result;
    }

    private record RowInfo<T>(
            int rowIndex,
            List<SearchMatrixElement<T>> rowValues
    ) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RowInfo<?> rowInfo = (RowInfo<?>) o;
            return rowIndex == rowInfo.rowIndex && Objects.equals(rowValues, rowInfo.rowValues);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowIndex, rowValues);
        }
    }

    private record SubSegmentValidityResult(
            boolean isValid,
            boolean wasChanges
    ) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            SubSegmentValidityResult that = (SubSegmentValidityResult) o;
            return isValid == that.isValid && wasChanges == that.wasChanges;
        }

        @Override
        public int hashCode() {
            return Objects.hash(isValid, wasChanges);
        }
    }
}
