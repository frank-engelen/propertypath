package de.codecentric.propertypath.demolib;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class Address {

    @Property
    private String street;

    public String getStreet() {
	return street;
    }

    public void setStreet(String street) {
	this.street = street;
    }
}
