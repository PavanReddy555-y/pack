package com.pone.expe.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Place {
    private String name;
    private String cityName;
    private String pincode;
}
