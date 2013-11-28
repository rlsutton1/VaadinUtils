package au.com.vaadinutils.converter;

import java.math.BigDecimal;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.DefaultConverterFactory;

public class VaadinUtilsConverterFactory extends DefaultConverterFactory {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
    protected <PRESENTATION, MODEL> Converter<PRESENTATION, MODEL> findConverter(
            Class<PRESENTATION> presentationType, Class<MODEL> modelType) {
        // Handle String <-> BigDecimal
        if (presentationType == String.class && modelType == BigDecimal.class) {
            return (Converter<PRESENTATION, MODEL>) new StringToBigDecimalConverter();
        }
        // Let default factory handle the rest
        return super.findConverter(presentationType, modelType);
    }
}