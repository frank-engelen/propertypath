package de.codecentric.propertypath.blogexamples.databinding;

import de.codecentric.propertypath.blogexamples.databinding.model.PersonFormModel;
import de.codecentric.propertypath.blogexamples.databinding.ui.DateWidget;
import de.codecentric.propertypath.blogexamples.databinding.ui.DropDownWidget;
import de.codecentric.propertypath.blogexamples.databinding.ui.FormUI;
import de.codecentric.propertypath.blogexamples.databinding.ui.TextWidget;

public class DataBindingExample {

	private FormUI formUI;

	public void buildAndConnectUI(PersonFormModel model) {

		formUI = new FormUI(model);
		formUI.add(new TextWidget("First name", "person.firstName"));
		formUI.add(new TextWidget("Surname", "person.surname"));
		formUI.add(new DateWidget("Birthday", "person.birthday"));
		formUI.add(new DropDownWidget("Type of Person:", "person.type", "possibleTypes"));
	}
}
