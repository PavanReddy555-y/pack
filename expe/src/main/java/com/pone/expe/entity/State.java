package com.pone.expe.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class State {
    private String name;
    private String countryName;
    private List<City> cities;
}
