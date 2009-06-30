package com.aliasi.lingmed.mesh;

public enum DescriptorClass {

    ONE(1), 
    TWO(2), 
    THREE(3), 
    FOUR(4);
        
    private final int mNumericalValue;

    DescriptorClass(int numericalValue) {
        mNumericalValue = numericalValue;
    }

    public String toString() {
        return Integer.toString(mNumericalValue);
    }

}