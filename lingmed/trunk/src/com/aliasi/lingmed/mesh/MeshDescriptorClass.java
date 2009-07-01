package com.aliasi.lingmed.mesh;

public enum MeshDescriptorClass {

    ONE(1), 
    TWO(2), 
    THREE(3), 
    FOUR(4);
        
    private final int mNumericalValue;

    MeshDescriptorClass(int numericalValue) {
        mNumericalValue = numericalValue;
    }

    public String toString() {
        return Integer.toString(mNumericalValue);
    }

}