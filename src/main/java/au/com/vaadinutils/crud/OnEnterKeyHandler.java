package au.com.vaadinutils.crud;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.TextField;

public abstract class OnEnterKeyHandler
{

	final ShortcutListener enterShortcut = new ShortcutListener("EnterKeyShorcut", ShortcutAction.KeyCode.ENTER, null)
	{

		private static final long serialVersionUID = 1L;

		@Override
		public void handleAction(Object sender, Object target)
		{
			enterKeyPressed();
		}
	};

	public void attachTo(final TextField component)
	{
		component.addFocusListener(new FieldEvents.FocusListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void focus(FieldEvents.FocusEvent event)
			{
				component.addShortcutListener(enterShortcut);
			}
		});

		component.addBlurListener(new FieldEvents.BlurListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void blur(FieldEvents.BlurEvent event)
			{
				component.removeShortcutListener(enterShortcut);
			}
		});
	}

	public abstract void enterKeyPressed();

}