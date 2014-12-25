package de.codecentric.propertypath.demolib;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class UsAddress extends Address {

    @Property
    private String state;

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }
}
