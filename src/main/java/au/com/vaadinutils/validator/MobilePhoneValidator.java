package au.com.vaadinutils.validator;

import com.vaadin.data.validator.RegexpValidator;


/**
 * String validator for australia mobile phones.
 * 
 * See {@link com.vaadin.data.validator.AbstractStringValidator} for more
 * information.
 * 
 * <p>
 * An empty string or a null is always accepted - use the required flag on
 * fields or a separate validator (or override {@link #isValidValue(String)}) to
 * fail on empty values.
 * </p>
 */
@SuppressWarnings("serial")
public class MobilePhoneValidator extends RegexpValidator {

    /**
     * Creates a validator for checking that a string is a valid australian mobile no.
     * 
     * @param errorMessage
     *            the message to display in case the value does not validate.
     */
    public MobilePhoneValidator(String errorMessage) {
        super(
                "^(614|04)[0-9]{8}",
                true, errorMessage.replaceAll("\\s",""));
    }
}
