package au.com.vaadinutils.crud.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class PreviewCSV
{
	 transient Logger logger   =  LogManager.getLogger(PreviewCSV.class.getName());

	public Component getContentFile(File tempFile, String caption, int rowLimit)
	{
		Table table = new Table();
		table.setSizeFull();

		FileReader reader;
		try
		{
			if (tempFile.exists())
			{
				reader = new FileReader(tempFile);
				IndexedContainer indexedContainer = buildContainerFromCSV(reader, rowLimit);
				reader.close();

				/* Finally, let's update the table with the container */
				table.setCaption(caption);
				table.setContainerDataSource(indexedContainer);
				table.setVisible(true);
			}
			else
			{
				/* Finally, let's update the table with the container */
				table.setCaption("No file selected");
				table.setVisible(true);

			}

		}
		catch (FileNotFoundException e)
		{
			logger.error(e, e);
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			logger.error(e, e);
			Notification.show(e.getMessage());
		}

		/* Main layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		layout.addComponent(table);
		layout.setExpandRatio(table, 1);

		return layout;
	}
	
	public Component getContentFromList(List<List<String>> data, String caption, int rowLimit)
	{
		Table table = new Table();
		table.setSizeFull();

		if (data.size() > 1)
		{
			IndexedContainer indexedContainer = buildContainerFromList(data, rowLimit);

			/* Finally, let's update the table with the container */
			table.setCaption(caption);
			table.setContainerDataSource(indexedContainer);
			table.setVisible(true);
		}
		else
		{
			/* Finally, let's update the table with the container */
			table.setCaption("No file selected");
			table.setVisible(true);

		}

		/* Main layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		layout.addComponent(table);
		layout.setExpandRatio(table, 1);

		return layout;
	}


	protected IndexedContainer buildContainerFromCSV(Reader reader, int rowLimit) throws IOException
	{
		IndexedContainer container = new IndexedContainer();
		CSVReader csvReader = null;

		try
		{
			csvReader = new CSVReader(reader);
			String[] columnHeaders = null;
			String[] record;

			// Import no more than 100 records as this is only a sample
			int count = 0;
			while (((record = csvReader.readNext()) != null) && count < rowLimit)
			{
				if (columnHeaders == null)
				{
					columnHeaders = record;
					addItemProperties(container, record);
					columnHeaders = record;
				}
				else
				{
					addRow(container, record, columnHeaders);
					count++;
				}
			}
		}
		finally
		{
			if (csvReader != null)
				csvReader.close();
		}
		return container;
	}

	private static void addItemProperties(IndexedContainer container, String[] fieldMaps)
	{
		for (String key : fieldMaps)
		{
			container.addContainerProperty(key, String.class, null);
		}
	}

	private static void addRow(IndexedContainer container, String[] fields, String[] headers)
	{
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		for (int i = 0; i < headers.length; i++)
		{

			@SuppressWarnings("unchecked")
			Property<String> itemProperty = item.getItemProperty(headers[i]);
			itemProperty.setValue(fields[i]);
		}

	}


	private IndexedContainer buildContainerFromList(List<List<String>> data, int rowLimit)
	{
		IndexedContainer container = new IndexedContainer();

		String[] columnHeaders = null;

		// Import no more than 100 records as this is only a sample
		int count = 0;
		for (List<String> row : data)
		{
			String[] rowArray = row.toArray(new String[0]);
			if (columnHeaders == null)
			{
				addItemProperties(container, rowArray);
				columnHeaders = rowArray;
			}
			else
			{
				addRow(container, rowArray, columnHeaders);
				count++;
			}
			
			if (count > 100)
				break;
		}

		return container;
	}

}
