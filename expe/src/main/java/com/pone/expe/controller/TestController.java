package com.pone.expe.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.pone.expe.entity.City;
import com.pone.expe.entity.Country;
import com.pone.expe.entity.Place;
import com.pone.expe.entity.State;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TestController {

	public static void main(String[] args) {
		

		 Country india = new Country("India", Arrays.asList(
		            new State("Karnataka", "India", Arrays.asList(
		                new City("Bangalore", "Karnataka", Arrays.asList(
		                    new Place("Koramangala", "Bangalore", "560001"),
		                    new Place("Indiranagar", "Bangalore", "560002"),
		                    new Place("Whitefield", "Bangalore", "560066")
		                )),
		                new City("Mysore", "Karnataka", Arrays.asList(
		                    new Place("Jayalakshmipuram", "Mysore", "570012"),
		                    new Place("Vijayanagar", "Mysore", "570017")
		                ))
		            )),
		            new State("Maharashtra", "India", Arrays.asList(
		                new City("Mumbai", "Maharashtra", Arrays.asList(
		                    new Place("Andheri", "Mumbai", "400053"),
		                    new Place("Bandra", "Mumbai", "400050")
		                ))
		            ))
		        ));
		 
		 
	        // Example 1: Filter by exact pincode
	        System.out.println("=== Places with pincode 560001 ===");
	        List<Place> places = india.getStates().stream()
	            .flatMap(state -> state.getCities().stream())
	            .flatMap(city -> city.getPlaces().stream())
	            .filter(place -> place.getPincode().equals("560001"))
	            .collect(Collectors.toList());
	        
	        places.forEach(p -> System.out.println(p.getName() + " - " + p.getPincode()));
	        
	        
	        List<String> list = india.getStates().stream().flatMap(state -> state.getCities().stream())
	        .flatMap(city -> city.getPlaces().stream())
	        .map(Place::getPincode)
	        .distinct()
	        .sorted()
	        .toList();
	        
	        System.out.println(list);
	        
	        List<PlaceDTO> filtered = filterByPincode(india, "560066");
	        filtered.forEach(System.out::println);
	        
		
	}
	
    // Filter by exact pincode
    public static List<PlaceDTO> filterByPincode(Country country, String pincode) {
        return country.getStates().stream()
            .flatMap(state -> state.getCities().stream()
                .flatMap(city -> city.getPlaces().stream()
                    .filter(place -> place.getPincode().equals(pincode))
                    .map(place -> new PlaceDTO(
                        country.getName(),
                        state.getName(),
                        city.getName(),
                        place.getName(),
                        place.getPincode()
                    ))
                )
            )
            .collect(Collectors.toList());
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlaceDTO {
    private String country;
    private String state;
    private String city;
    private String placeName;
    private String pincode;
    
    @Override
    public String toString() {
        return String.format("%s → %s → %s → %s → %s", 
            country, state, city, placeName, pincode);
    }
}
