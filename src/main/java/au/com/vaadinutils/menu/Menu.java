package au.com.vaadinutils.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
// Make this annotation accessible at runtime via reflection.
@Target({ ElementType.TYPE })
// This annotation can only be applied to classes.
public @interface Menu
{
	public final static String MENUBAR = "MenuBar";

	String display();

	String path() default MENUBAR;

	ActionType actionType() default ActionType.NAVIGATE;

	/**
	 * used by ActionType URL and URL_NEW_WINDOW
	 * 
	 * @return
	 */
	String url() default "";

	/**
	 * used by ActionType URL_NEW_WINDOW
	 * 
	 * @return
	 */
	String windowName() default "";

	/**
	 * used by ActionType URL_NEW_WINDOW
	 * 
	 * @return
	 */
	int width() default 0;

	/**
	 * used by ActionType URL_NEW_WINDOW
	 * 
	 * @return
	 */
	int height() default 0;

	/**
	 * this needs to be the last menu added or it will have unpredictable
	 * results
	 * 
	 * @return
	 */
	boolean atTop() default false;
	
	/** 
	 * allows URL_NEW_WINDOW to specify a dynamic size for the new window
	 * @return
	 */
	Class<? extends WindowSizer> windowSizer() default WindowSizerNull.class;
}
