package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;

@WithProperties
public class PersonSubclass1 extends Person {

	@SuppressWarnings("hiding")
	public static final PersonSubclass1Properties<PersonSubclass1, PersonSubclass1> PROPERTIES = PersonSubclass1Properties
			.newPersonSubclass1Properties();

	@Property
	private String number;

	@Property
	private String number2;

	@Property
	private String wirdUeberschrieben;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

}
