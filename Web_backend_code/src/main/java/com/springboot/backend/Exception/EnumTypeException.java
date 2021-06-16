package com.springboot.backend.Exception;

public class EnumTypeException extends RuntimeException {
    private String park;

    public EnumTypeException(String park) {
        this.park = park;
    }

    public String getPark() {
        return park;
    }
}
