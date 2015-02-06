package de.codecentric.propertypath.blogexamples.databinding.model;

import java.util.Arrays;
import java.util.List;

public class PersonFormModel {

	private Person person = new Person();

	private final List<Person.Type> possibleTypes = Arrays.asList(Person.Type.values());

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<Person.Type> getPossibleTypes() {
		return possibleTypes;
	}

}
