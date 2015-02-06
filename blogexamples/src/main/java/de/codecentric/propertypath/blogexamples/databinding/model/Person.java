package de.codecentric.propertypath.blogexamples.databinding.model;

import java.util.Date;

public class Person {
	public enum Type {
		APPLICANT, STUDENT, MENTOR, TEACHER
	}

	private String firstName;
	private String surname;
	private Date birthday;
	private Type type;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
