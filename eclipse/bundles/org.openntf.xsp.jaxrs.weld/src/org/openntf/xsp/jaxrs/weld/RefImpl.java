package org.openntf.xsp.jaxrs.weld;

import org.glassfish.jersey.internal.util.collection.Ref;

public class RefImpl<T> implements Ref<T> {

    private T reference;

    RefImpl() {
        this.reference = null;
    }

    RefImpl(final T value) {
        this.reference = value;
    }

    @Override
    public T get() {
        return reference;
    }

    @Override
    public void set(final T value) throws IllegalStateException {
        this.reference = value;
    }

    @Override
    public String toString() {
        return "DefaultRefImpl{"
               + "reference=" + reference
               + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Ref)) {
            return false;
        }

        Object otherRef = ((Ref) obj).get();
        T ref = this.reference;
        return ref == otherRef || (ref != null && ref.equals(otherRef));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.reference != null ? this.reference.hashCode() : 0);
        return hash;
    }
}