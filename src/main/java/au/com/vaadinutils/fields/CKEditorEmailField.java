package au.com.vaadinutils.fields;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

/**
 * This implementation needs to be completed as we need to turn it into a full
 * blown widget for it to actually work.
 */

public class CKEditorEmailField extends CKEditorTextField
{
	private static final long serialVersionUID = 1L;

	private static String toolbarLineJS = " "
			// +
			// "{ name: 'document', items : [ 'Source','-','Save','NewPage','DocProps','Preview','Print','-','Templates' ] },"
			+ "{ name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ] },"
			+ "{ name: 'clipboard', items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },"
			+ "{ name: 'editing', items : [ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ] },"
			+ "{ name: 'links', items : [ 'Link','Unlink','Anchor' ] },"
			+ "'/',"
			+ "{ name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },"
			+ "{ name: 'colors', items : [ 'TextColor','BGColor' ] },"

			+ "{ name: 'styles', items : [ 'Styles','Format','Font','FontSize' ] }," + "'/',"
			// +
			// "{ name: 'tools', items : [ 'Maximize', 'ShowBlocks','-','About' ] }"
			+ "";

	public CKEditorEmailField(boolean readonly)
	{
		super(getConfig(readonly));

	}

	private static CKEditorConfig getConfig(boolean readonly)
	{
		CKEditorConfig config = new CKEditorConfig();
		config.useCompactTags();
		config.disableElementsPath();
		config.setResizeEnabled(false);
		config.setToolbarCanCollapse(false);
		config.disableResizeEditor();

		if (readonly)
			config.addCustomToolbarLine("");
		else
			config.addCustomToolbarLine(toolbarLineJS);

		return config;
	}

}
