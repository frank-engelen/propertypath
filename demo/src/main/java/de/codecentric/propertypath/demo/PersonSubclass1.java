package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class PersonSubclass1 extends Person {

    @SuppressWarnings("hiding")
    public static final PersonSubclass1Properties<PersonSubclass1, PersonSubclass1> PROPERTIES = PersonSubclass1Properties.newPersonSubclass1Properties();

    @Property
    private String number;

    public String getNumber() {
	return number;
    }

    public void setNumber(String number) {
	this.number = number;
    }

}
