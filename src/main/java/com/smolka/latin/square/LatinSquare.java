package com.smolka.latin.square;

import java.util.List;

public interface LatinSquare {

    boolean check(Integer[][] square);

    Integer[][] getFirstVariant(Integer[][] square);

    List<Integer[][]> getVariantsWithLimit(Integer[][] square, int limit);
}
