package com.regnosys.rosetta.common.serialisation.lookup;

import java.util.Objects;
import java.util.StringJoiner;

public class LookupDataItem {

    private Object key;
    private Object value;

    public LookupDataItem() {
    }

    public LookupDataItem(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LookupDataItem that = (LookupDataItem) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LookupDataItem.class.getSimpleName() + "[", "]")
                .add("key=" + key)
                .add("value=" + value)
                .toString();
    }
}
