package com.athaydes.sparkws;

class SettableOnce<T> {

    private T value;
    private boolean wasSet = false;
    private final String name;

    SettableOnce( String name, T defaultValue ) {
        this.name = name;
        this.value = defaultValue;
    }

    T get() {
        return value;
    }

    void set( T value ) {
        if ( wasSet ) {
            throw new IllegalStateException( name + " was already set" );
        }
        wasSet = true;
        this.value = value;
    }

}
