package au.com.vaadinutils.crud;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import au.com.bytecode.opencsv.CSVWriter;
import au.com.vaadinutils.jasper.AttachmentType;
import au.com.vaadinutils.util.PipedOutputStreamWrapper;

public class GridContainerCSVExport<E>
{
	PipedOutputStreamWrapper stream = new PipedOutputStreamWrapper();
	Logger logger = LogManager.getLogger();
	private GridHeadingPropertySet headingsSet;
	private Grid grid;
	private LinkedHashMap<String, Object> extraColumnHeadersAndPropertyIds;

	public GridContainerCSVExport(final String fileName, final Grid grid, final GridHeadingPropertySet headingsSet)
	{

		this.grid = grid;
		this.headingsSet = headingsSet;
		final Window window = new Window();
		window.setCaption("Download " + fileName + " CSV data");
		window.center();
		window.setHeight("100");
		window.setWidth("300");
		window.setResizable(false);
		window.setModal(true);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setMargin(true);

		window.setContent(layout);

		UI.getCurrent().addWindow(window);
		window.setVisible(true);

		final Button downloadButton = createDownloadButton(fileName, window);

		layout.addComponent(downloadButton);
		layout.setComponentAlignment(downloadButton, Alignment.MIDDLE_CENTER);
	}

	private Button createDownloadButton(final String fileName, final Window window)
	{
		final Button downloadButton = new Button("Download CSV Data");
		downloadButton.setDisableOnClick(true);

		@SuppressWarnings("serial")
		StreamSource source = new StreamSource()
		{

			@Override
			public InputStream getStream()
			{

				try
				{
					ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(arrayOutputStream));

					export(grid, bufferedWriter, headingsSet);
					return new ByteArrayInputStream(arrayOutputStream.toByteArray());
				}
				catch (Throwable e)
				{
					logger.error(e, e);
					Notification.show(e.getMessage());
				}
				finally
				{
					Runnable runner = new Runnable()
					{

						@Override
						public void run()
						{
							try
							{
								Thread.sleep(500);

								UI.getCurrent().access(new Runnable()
								{

									@Override
									public void run()
									{
										window.close();

									}
								});
							}
							catch (InterruptedException e)
							{
								logger.error(e, e);
							}

						}
					};
					new Thread(runner, "Dialog closer").start();
				}
				return null;
			}
		};

		StreamResource resource = new StreamResource(source, fileName + AttachmentType.CSV.getFileExtension());
		resource.setMIMEType(AttachmentType.CSV.getMIMETypeString());

		FileDownloader fileDownloader = new FileDownloader(resource);
		fileDownloader.setOverrideContentType(false);
		fileDownloader.extend(downloadButton);
		return downloadButton;
	}

	public void export(Grid grid, Writer stream, GridHeadingPropertySet headingsSet) throws IOException
	{

		CSVWriter writer = new CSVWriter(stream);

		Map<String, Object> headerPropertyMap = new LinkedHashMap<>();

		List<GridHeadingToPropertyId> cols = headingsSet.getColumns();
		for (GridHeadingToPropertyId col : cols)
		{
			headerPropertyMap.put(col.getHeader(), col.getPropertyId());
		}

		List<String> headerList = new LinkedList<>();
		headerList.addAll(headerPropertyMap.keySet());
		extraColumnHeadersAndPropertyIds = getExtraColumnHeadersAndPropertyIds();
		headerList.addAll(extraColumnHeadersAndPropertyIds.keySet());

		writeHeaders(writer, headerList);

		Set<Object> properties = new LinkedHashSet<>();
		properties.addAll(headerPropertyMap.values());

		for (Object id : grid.getContainerDataSource().getItemIds())
		{
			writeRow(writer, grid, id, properties);
		}

		writer.flush();

	}

	private void writeRow(CSVWriter writer, Grid grid, Object id, Set<Object> properties)
	{
		Item item = grid.getContainerDataSource().getItem(id);
		String[] values = new String[properties.size() + extraColumnHeadersAndPropertyIds.size()];
		int i = 0;
		for (Object propertyId : properties)
		{
			@SuppressWarnings("rawtypes")
			final Property itemProperty = item.getItemProperty(propertyId);
			if (itemProperty != null)
			{
				Object value = itemProperty.getValue();
				if (value != null)
				{
					final Object convertedValue = convert(value);
					if (convertedValue != null)
					{
						values[i++] = sanitiseValue(convertedValue);
					}
					else
					{
						values[i++] = "";
					}
				}
				else
				{
					values[i++] = "";
				}
			}
		}

		for (Object columnId : extraColumnHeadersAndPropertyIds.values())
		{
			String value = getValueForExtraColumn(item, columnId);
			if (value == null)
			{
				value = "";
			}
			values[i++] = value;
		}
		writer.writeNext(values);

	}

	public Object convert(Object value)
	{
		return value;
	}

	public String sanitiseValue(final Object value)
	{
		String sanitisedValue;

		if (value instanceof String)
		{
			sanitisedValue = new HtmlToPlainText().getPlainText(Jsoup.parse(value.toString()));
		}
		else
		{
			sanitisedValue = value.toString();
		}

		if (sanitisedValue == null)
		{
			sanitisedValue = "";
		}

		return sanitisedValue;
	}

	private void writeHeaders(CSVWriter writer, List<String> headers)
	{
		writer.writeNext(headers.toArray(new String[]
		{}));
	}

	/**
	 * propertyId's will later be passed to getValueForExtraColumn so it can
	 * generate the data for a column
	 * 
	 * @return - an ordered map where key=heading string and value = a unique
	 *         propertyId
	 */
	protected LinkedHashMap<String, Object> getExtraColumnHeadersAndPropertyIds()
	{
		return new LinkedHashMap<>();
	}

	/**
	 * 
	 * @param item
	 * @param columnId
	 *            - as specified in the map returned from
	 *            getExtraColumnHeadersAndPropertyIds()
	 * @return
	 */
	protected String getValueForExtraColumn(Item item, Object columnId)
	{
		return null;
	}

}
