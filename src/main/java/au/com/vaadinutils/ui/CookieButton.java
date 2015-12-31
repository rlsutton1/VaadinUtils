package au.com.vaadinutils.ui;

import au.com.vaadinutils.user.UserSettingsStorage;
import au.com.vaadinutils.user.UserSettingsStorageFactory;

import com.vaadin.ui.Button;

public class CookieButton extends Button
{
	// Logger logger = LogManager.getLogger();

	private static final String OFF_STATE = "CookieButton-Off";
	private static final String ON_STATE = "CookieButton-On";
	private static final long serialVersionUID = 2052581680067745511L;
	private String cookiePath;
	private CookieButtonCallback callback;
	private String onText;
	private String offText;

	UserSettingsStorage userSettings = UserSettingsStorageFactory.getUserSettingsStorage();

	public CookieButton(String cookiePath, final String onText, final String offText, CookieButtonCallback callback)
	{
		this.callback = callback;
		this.cookiePath = cookiePath;
		this.onText = onText;
		this.offText = offText;
		setCaption(offText);
		setId(offText);
		addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 4061004605229827783L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				setMode(getCaption().equals(onText));
			}

		});

	}

	private void setMode(boolean on)
	{
		if (on)
		{
			setCaption(offText);
			setId(offText);
			userSettings.store(cookiePath, ON_STATE);
			callback.on();

		}
		else
		{
			setCaption(onText);
			setId(onText);
			userSettings.store(cookiePath, OFF_STATE);
			callback.off();

		}
	}

	public void restoreStateFromCookie()
	{
		boolean mode = false;
		mode = userSettings.get(cookiePath).equalsIgnoreCase(ON_STATE);
		setMode(mode);

	}
}
