package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class YetAnotherPerson {

    @Property
    private String name;

    @Property
    private YetAnotherAddress address;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public YetAnotherAddress getAddress() {
	return address;
    }

    public void setAddress(YetAnotherAddress address) {
	this.address = address;
    }
}
