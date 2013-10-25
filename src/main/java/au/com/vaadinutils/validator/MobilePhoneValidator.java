package au.com.vaadinutils.validator;

import com.vaadin.data.validator.RegexpValidator;


/**
 * String validator for e-mail addresses. The e-mail address syntax is not
 * complete according to RFC 822 but handles the vast majority of valid e-mail
 * addresses correctly.
 * 
 * See {@link com.vaadin.data.validator.AbstractStringValidator} for more
 * information.
 * 
 * <p>
 * An empty string or a null is always accepted - use the required flag on
 * fields or a separate validator (or override {@link #isValidValue(String)}) to
 * fail on empty values.
 * </p>
 * 
 * @author Vaadin Ltd.
 * @since 5.4
 */
@SuppressWarnings("serial")
public class MobilePhoneValidator extends RegexpValidator {

    /**
     * Creates a validator for checking that a string is a syntactically valid
     * e-mail address.
     * 
     * @param errorMessage
     *            the message to display in case the value does not validate.
     */
    public MobilePhoneValidator(String errorMessage) {
        super(
                "^([0-9]){8}",
                true, errorMessage.replaceAll("\\s",""));
    }
}
