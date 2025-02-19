package com.smolka.latin.square.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SelectionMatrixElement<T> {

    private final int row;

    private final int column;

    private final Set<T> variants;

    public SelectionMatrixElement(int row, int column) {
        this.row = row;
        this.column = column;
        this.variants = new HashSet<>();
    }

    public SelectionMatrixElement(int row, int column, Set<T> variants) {
        this.row = row;
        this.column = column;
        this.variants = variants;
    }

    public int getVariantsSize() {
        return variants.size();
    }

    public Set<T> getVariants() {
        return variants;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public SelectionMatrixElement<T> copy() {
        return new SelectionMatrixElement<>(row, column, new HashSet<>(variants));
    }

    public boolean removeVariants(Set<T> variants) {
        return this.variants.removeAll(variants);
    }

    public void removeVariant(T variant) {
        this.variants.remove(variant);
    }

    public boolean isStrong() {
        return variants.size() == 1;
    }

    public T getStrongValue() {
        if (!isStrong()) {
            throw new RuntimeException("Attempt to unboxing non-strong element");
        }
        return variants.stream().findFirst().orElseThrow();
    }

    public void resetVariants(T variant) {
        this.variants.clear();
        this.variants.add(variant);
    }

    public void resetVariants(Set<T> variants) {
        this.variants.clear();
        this.variants.addAll(variants);
    }

    public boolean isEmpty() {
        return variants.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectionMatrixElement<?> that = (SelectionMatrixElement<?>) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + this.variants.stream().map(Objects::toString).collect(Collectors.joining(", ")) + ")";
    }
}
