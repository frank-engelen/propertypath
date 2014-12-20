package de.codecentric.propertypath.demolib;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class Address {

    @Property
    private String streetAndNumber;

    @Property
    private String city;

    public String getStreetAndNumber() {
	return streetAndNumber;
    }

    public void setStreetAndNumber(String streetAndNumber) {
	this.streetAndNumber = streetAndNumber;
    }

}
