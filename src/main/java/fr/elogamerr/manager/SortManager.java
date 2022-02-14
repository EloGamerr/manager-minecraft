package fr.elogamerr.manager;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class SortManager {
    public static final <K, V extends Comparable<V>> LinkedHashMap<K, V> sortByValues(final Map<K, V> map, int ascending)
    {
        Comparator<K> valueComparator =  new Comparator<K>()
        {
            private int ascending;
            public int compare(K k1, K k2) {
                int compare = map.get(k2) == null ? 0 : map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return ascending*compare;
            }
            public Comparator<K> setParam(int ascending)
            {
                this.ascending = ascending;
                return this;
            }
        }.setParam(ascending);

        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return new LinkedHashMap<>(sortedByValues);
    }
}
