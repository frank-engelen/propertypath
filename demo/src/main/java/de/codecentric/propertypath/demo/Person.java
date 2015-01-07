package de.codecentric.propertypath.demo;

import java.util.List;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;
import de.codecentric.propertypath.demolib.Address;

@WithProperties
public class Person {

    public static final PersonProperties<Person, Person> PROPERTIES = PersonProperties.newPersonProperties();

    @Property
    private String name;

    @Property
    private String surname;

    @Property
    private Address address;

    @Property
    private List<String> type;

    @Property
    private boolean active;

    @Property
    private int age;
    
    @Property
    private String writeOnlyProperty;

    public String getName() {
	return name;
    }

    @Property
    public String getFullName() {
	return name + " " + surname;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getSurname() {
	return surname;
    }

    public void setSurname(String surname) {
	this.surname = surname;
    }

    public Address getAddress() {
	return address;
    }

    public void setAddress(Address address) {
	this.address = address;
    }

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	public void setWriteOnlyProperty(String writeOnlyProperty) {
		this.writeOnlyProperty = writeOnlyProperty;
	}

}
