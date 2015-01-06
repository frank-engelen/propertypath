package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class YetAnotherAddress {

    @Property
    private String streetAndNumber;

    @Property
    private String city;

    @Property
    private String country;

    public String getStreetAndNumber() {
	return streetAndNumber;
    }

    public void setStreetAndNumber(String streetAndNumber) {
	this.streetAndNumber = streetAndNumber;
    }

    public String getCity() {
	return city;
    }

    public void setCity(String city) {
	this.city = city;
    }

    public String getCountry() {
	return country;
    }

    public void setCountry(String country) {
	this.country = country;
    }

}
