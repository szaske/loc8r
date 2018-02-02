package com.loc8r.seattle.utils;

import java.util.Comparator;

/**
 * Created by szaske on 2/1/18.
 */

public class AtoZComparator implements Comparator<String> {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
}
