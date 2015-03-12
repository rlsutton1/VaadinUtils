package au.com.vaadinutils.ui;

public interface AutoCompleteOptionSelected<E>
{

    public void optionSelected(AutoCompleteTextField<E> field,E option);
}
