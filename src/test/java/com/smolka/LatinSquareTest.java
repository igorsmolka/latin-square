package com.smolka;

import com.smolka.latin.square.impl.LatinSquareImpl;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LatinSquareTest {

    @Test
    public void test_checkNegative() {
        Integer[][] field = {
                { 1, 2, 3 },
                { 1, 3, 2 },
                { 2, 1, 3 }
        };

        LatinSquareImpl latinSquare = new LatinSquareImpl();
        assert !latinSquare.check(field);
    }

    @Test
    public void test_checkPositive() {
        Integer[][] field = {
                { 1, 2, 3 },
                { 3, 1, 2 },
                { 2, 3, 1 }
        };

        LatinSquareImpl latinSquare = new LatinSquareImpl();
        assert latinSquare.check(field);
    }

    @Test
    public void test_findingMillionVariants() {
        Integer[][] field = {
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
                { null, null, null, null, null, null, null, null, null },
        };

        LatinSquareImpl latinSquare = new LatinSquareImpl();

        int limit = 1000000;

        List<Integer[][]> result = latinSquare.getVariantsWithLimit(field, limit);
        for (Integer[][] variantInResult : result) {
            assert latinSquare.check(variantInResult);
        }

        assert result.size() == limit;

        Set<Integer[][]> setForDupesCheck = new HashSet<>(result);
        assert setForDupesCheck.size() == result.size();
    }

    @Test
    public void test_findingFirstEverest() {
        Integer[][] field = {
                { 8, null, null, null, null, null, null, null, null },
                { null, null, 3, 6, null, null, null, null, null },
                { null, 7, null, null, 9, null, 2, null, null },
                { null, 5, null, null, null, 7, null, null, null },
                { null, null, null, null, 4, 5, 7, null, null },
                { null, null, null, 1, null, null, null, 3, null },
                { null, null, 1, null, null, null, null, 6, 8 },
                { null, null, 8, 5, null, null, null, 1, null },
                { null, 9, null, null, null, null, 4, null, null },
        };

        LatinSquareImpl latinSquare = new LatinSquareImpl();

        Integer[][] result = latinSquare.getFirstVariant(field);
        assert result != null;
        assert latinSquare.check(result);
    }

    @Test
    public void test_findingTwoSimple() {
        Integer[][] field = {
                { 1, 2, 3, 4, 5 },
                { null, 4, null, null, null },
                { null, null, null, 5, null },
                { 2, null, null, null, null },
                { 3, null, null, null, null }
        };

        int limit = 2;

        LatinSquareImpl latinSquare = new LatinSquareImpl();
        List<Integer[][]> result = latinSquare.getVariantsWithLimit(field, limit);

        for (Integer[][] variantInResult : result) {
            assert latinSquare.check(variantInResult);
        }

        assert result.size() == limit;

        Set<Integer[][]> setForDupesCheck = new HashSet<>(result);
        assert setForDupesCheck.size() == result.size();
    }
    
    @Test
    public void test_findingFirstSimple() {
        Integer[][] field = {
                { 1, 2, 3, 4, 5 },
                { null, 4, null, null, null },
                { null, null, null, 5, null },
                { 2, null, null, null, null },
                { 3, null, null, null, null }
        };

        LatinSquareImpl latinSquare = new LatinSquareImpl();
        Integer[][] result = latinSquare.getFirstVariant(field);
        assert result != null;
        assert latinSquare.check(result);
    }
}
