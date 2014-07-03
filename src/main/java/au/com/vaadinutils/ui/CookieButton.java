package au.com.vaadinutils.ui;

import javax.servlet.http.Cookie;

import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;

public class CookieButton extends Button
{
	// Logger logger = LogManager.getLogger();

	private String cookiePath;
	private CookieButtonCallback callback;
	private String onText;
	private String offText;

	public CookieButton(String cookiePath,  final String onText, final String offText,CookieButtonCallback callback)
	{
		this.callback = callback;
		this.cookiePath = cookiePath;
		this.onText = onText;
		this.offText = offText;
		setCaption(offText);
		addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 4061004605229827783L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				setMode(getCaption().equals("Off"));
			}

		});
	}

	private void setMode(boolean on)
	{
		if (on)
		{
			setCaption(offText);
			Cookie myCookie = new Cookie("HandsetView", "On");
			myCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
			VaadinService.getCurrentResponse().addCookie(myCookie);
			callback.on();

		}
		else
		{
			setCaption(onText);
			Cookie myCookie = new Cookie("HandsetView", "Off");
			myCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
			VaadinService.getCurrentResponse().addCookie(myCookie);
			callback.off();

		}
	}

	void init()
	{
		boolean mode = false;
		for (Cookie cookie : VaadinService.getCurrentRequest().getCookies())
		{
			System.out.println(cookie.getName());
			if (cookie.getName().equals(cookiePath))
			{
				mode = cookie.getValue().equalsIgnoreCase("On");
				break;
			}
		}
		setMode(mode);

	}
}
