package au.com.vaadinutils.editors;


public interface DialogListener<T>
{
	void confirmed(T details);
	
	void declined();

}
