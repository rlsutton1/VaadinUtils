package au.com.vaadinutils.fields;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

/**
 * This implementation needs to be completed as we need to turn it into a full
 * blown widget for it to actually work.
 */

public class CKEditorEmailField extends CKEditorTextField
{
	private static final long serialVersionUID = 1L;

	public CKEditorEmailField(boolean readonly)
	{
		super(getConfig(readonly, null));
		setSizeFull();

	}

	public interface ConfigModifier
	{

		Map<String, String> modifyToolbarOptions(Map<String, String> defaultConfig);

		CKEditorConfig modifyConfig(CKEditorConfig config);

	}

	public CKEditorEmailField(boolean readonly, ConfigModifier configModifier)
	{
		super();
		Map<String, String> options = configModifier.modifyToolbarOptions(getDefaultConfig());
		CKEditorConfig localconfig = configModifier.modifyConfig(getConfig(readonly, options));
		setConfig(localconfig);
		setSizeFull();

	}

	static public Map<String, String> getDefaultConfig()
	{
		Map<String, String> configMap = new HashMap<String, String>();

		// configMap
		// .put("source",
		// "{ name: 'document', items : [ 'Source','-','Save','NewPage','DocProps','Preview','Print','-','Templates' ] }");
		//
		// configMap
		// .put("paragraph",
		// "{ name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ] }");
		// configMap
		// .put("clipboard",
		// "{ name: 'clipboard', items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] }");
		// configMap.put("editing",
		// "{ name: 'editing', items : [ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ] }");
		// configMap.put("document",
		// "{ name: 'links', items : [ 'Link','Unlink','Anchor' ] }");
		//
		// configMap
		// .put("basicstyles",
		// "{ name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] }");
		// configMap.put("colors",
		// "{ name: 'colors', items : [ 'TextColor','BGColor' ] }");
		//
		// configMap.put("styles",
		// "{ name: 'styles', items : [ 'Styles','Format','Font','FontSize' ] }");
		//
		// configMap.put("tools",
		// "{ name: 'tools', items : [ 'Maximize', 'ShowBlocks','-','About' ] }");

		configMap
				.put("style",
						"{ items: ['Styles','Format','Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat'] }");
		configMap.put("colour", "{ items: ['TextColor','BGColor'] }");
		configMap.put("size", "{ items: ['Font','FontSize'] }");
		configMap.put("justify", "{ items: ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'] }");
		configMap.put("paste", "{ items: ['Cut','Copy','Paste','PasteText','PasteFromWord'] }");
		configMap.put("find", "{ items: ['Find','Replace'] }");
		configMap.put("undo", "{ items: ['Undo','Redo'] }");
		configMap.put("bullets", "{ items: ['NumberedList','BulletedList'] }");
		configMap.put("indent", "{ items: ['Outdent','Indent','CreateDiv'] }");
		configMap.put("table", "{ items: ['Table','HorizontalRule','PageBreak','SpecialChar'] }");
		configMap.put("image", "{ items: ['Image','Link','Unlink'] }");
		configMap.put("source", "{ items: ['Source','ShowBlocks','Maximize'] }");

		return configMap;

	}

	static CKEditorConfig getConfig(boolean readonly, Map<String, String> configMap)
	{
		if (configMap == null)
		{
			configMap = getDefaultConfig();
			configMap.remove("tools");
			configMap.remove("document");
		}

		String toolbarLineJs = buildToolbarJs(configMap);

		CKEditorConfig config = new CKEditorConfig();
		config.useCompactTags();
		config.disableElementsPath();
		config.setResizeEnabled(false);
		config.setToolbarCanCollapse(false);
		config.disableResizeEditor();

		if (readonly)
			config.addCustomToolbarLine("");
		else
			config.addCustomToolbarLine(toolbarLineJs);

		return config;
	}

	private static String buildToolbarJs(Map<String, String> configMap)
	{
		String js = "";
		for (String entry : configMap.values())
		{
			js += entry + ",";
		}
		if (js.endsWith(","))
		{
			js = js.substring(0, js.length() - 1);
		}
		return js;

	}

}
