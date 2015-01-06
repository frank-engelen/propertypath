package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class PersonSubclass2 extends Person {

    @SuppressWarnings("hiding")
    public static final PersonSubclass2Properties<PersonSubclass2, PersonSubclass2> PROPERTIES = PersonSubclass2Properties.newPersonSubclass2Properties();

    @Property
    private String number;

    public String getNumber() {
	return number;
    }

    public void setNumber(String number) {
	this.number = number;
    }
}
