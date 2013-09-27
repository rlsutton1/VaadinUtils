package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public interface SplitField extends Component
{

	Label getLabel();

	String getCaption();

	void hideLabel();

}
