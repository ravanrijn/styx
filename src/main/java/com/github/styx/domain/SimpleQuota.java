package com.github.styx.domain;

public class SimpleQuota extends Identifiable {

    public SimpleQuota(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SimpleQuota that = (SimpleQuota) o;
        if (!getId().equals(that.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
