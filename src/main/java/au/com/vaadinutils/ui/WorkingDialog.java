package au.com.vaadinutils.ui;

import au.com.vaadinutils.listener.CancelListener;
import au.com.vaadinutils.listener.ClickEventLogged;
import au.com.vaadinutils.listener.CompleteListener;
import au.com.vaadinutils.listener.ProgressListener;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Displays a dialog designed to be shown when a long running task is in
 * progress.
 * 
 * You can use WorkingDialog in one of two ways.
 * 
 * 1) Call WorkingDialog Add it to the UI via
 * UI.getCurrent().addWindow(workingDialog); Then set a runnable by calling
 * setWorker(new Runnable() {} ); The working dialog will then display its self
 * and run the Runnable in a background thread. When the Runnable completes the
 * WorkingDialog will be removed from the UI.
 * 
 * 2) Call WorkingDialog Add it to the UI via
 * UI.getCurrent().addWindow(workingDialog); Pass it to your own thread as a
 * 'ProgressListener'. When your thread calls either the complete or exception
 * methods the Working Dialog will be closed. Calls to itemError are ignored.
 * 
 */

public class WorkingDialog extends Window implements ProgressListener<String>
{
	private static final long serialVersionUID = 1L;
	private Label messageLabel;
	private VerticalLayout content;
	private Button cancel;
	private CancelListener cancelListener;
	private CompleteListener completeListener;
	private VerticalLayout layout;

	/**
	 * Displays a dialog designed to be shown when a long running task is in
	 * progress.
	 * 
	 * @param caption
	 * @param message
	 */
	public WorkingDialog(String caption, String message)
	{
		this(caption, message, null, null);
	}

	/**
	 * Displays a dialog designed to be shown when a long running task is in
	 * progress.
	 * 
	 * @param caption
	 * @param message
	 * @param listener
	 */
	public WorkingDialog(String caption, String message, CancelListener listener)
	{
		this(caption, message, listener, null);
	}

	/**
	 * Displays a dialog designed to be shown when a long running task is in
	 * progress.
	 * 
	 * @param caption
	 * @param message
	 * @param refresher
	 */
	public WorkingDialog(String caption, String message, Refresher refresher)
	{
		this(caption, message, null, refresher);
	}

	/**
	 * Display the Working Dialog with a Cancel Button. If the user clicks the
	 * Cancel button the listener will be sent a cancel button. The setWorker
	 * method does not support being cancelled.
	 * 
	 * @param caption
	 * @param message
	 * @param listener
	 * @param refresher
	 */
	public WorkingDialog(String caption, String message, CancelListener listener, Refresher refresher)
	{
		super(caption);
		this.setModal(true);
		this.setClosable(false);
		this.setResizable(false);
		content = new VerticalLayout();
		// this.setWidth("100px");
		this.setHeight("150px");
		content.setSizeFull();
		content.setMargin(true);
		content.setSpacing(true);

		this.cancelListener = listener;

		layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setSizeFull();

		HorizontalLayout progressArea = new HorizontalLayout();
		progressArea.setSizeFull();
		ProgressBar progress = new ProgressBar();
		progressArea.addComponent(progress);
		progress.setIndeterminate(true);
		messageLabel = new Label(message);
		messageLabel.setContentMode(ContentMode.HTML);
		messageLabel.setSizeFull();
		progressArea.addComponent(messageLabel);
		progressArea.setExpandRatio(messageLabel, 1);
		layout.addComponent(progressArea);
		content.addComponent(layout);

		if (listener != null)
		{
			cancel = new Button("Cancel");
			cancel.addClickListener(new ClickEventLogged.ClickListener()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void clicked(ClickEvent event)
				{
					WorkingDialog.this.cancelListener.cancel();
					WorkingDialog.this.close();

				}
			});
			content.addComponent(cancel);
			content.setComponentAlignment(cancel, Alignment.BOTTOM_RIGHT);

		}

		if (refresher != null)
			addExtension(refresher);

		this.setContent(content);
		this.center();

	}

	/**
	 * Pass a Runnable that WorkingDialog will run in a background thread. On
	 * completion of the thread the complete listener will be notified and the
	 * WorkingDialog will remove itself rom the UI.
	 * 
	 * @param runnable
	 *            the runnable to be run in a background thread.
	 * @param listener
	 *            a complete listener to be notified when the thread has
	 *            finished.
	 */

	public void setWorker(Runnable runnable, CompleteListener listener)
	{
		this.completeListener = listener;

		Thread worker = new Thread(new Worker(this, runnable));
		worker.start();

	}

	class Worker implements Runnable
	{
		private Runnable runnable;
		private WorkingDialog parent;

		Worker(WorkingDialog parent, Runnable runnable)
		{
			this.parent = parent;
			this.runnable = runnable;
		}

		@Override
		public void run()
		{
			try
			{
				this.runnable.run();
			}
			finally
			{
				new UIUpdater(new Runnable()
				{
					public void run()
					{
						parent.complete(0);
					}
				});
			}

		}

	}

	public void addUserComponent(final Component component)
	{
		new UIUpdater(new Runnable()
		{
			@Override
			public void run()
			{
				layout.addComponent(component);
			}
		});
	}

	public void progress(int count, int max, final String message)
	{
		new UIUpdater(new Runnable()
		{
			@Override
			public void run()
			{
				messageLabel.setValue(message);
			}
		});
	}

	public void complete(int sent)
	{
		if (this.completeListener != null)
			this.completeListener.complete();
		WorkingDialog.this.close();

	}

	@Override
	public void itemError(Exception e, String status)
	{
		// Ignored.

	}

	@Override
	public void exception(Exception e)
	{
		if (this.completeListener != null)
			this.completeListener.complete();
		WorkingDialog.this.close();
	}

	public void removeUserComponent(Component component)
	{
		layout.removeComponent(component);
		
	}

}
