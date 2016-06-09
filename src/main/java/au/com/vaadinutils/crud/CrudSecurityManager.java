package au.com.vaadinutils.crud;

/**
 * A CrudSecurityManager is created each time a users logs into the application.
 *
 * The security Manager maps the User to the SecurityModel which stores the
 * Feature/Action/Role matrix for the application.
 *
 * @author bsutton
 *
 */

public interface CrudSecurityManager
{

	boolean canUserView();

	boolean canUser(Enum<?> changeAccountGroups);

	boolean canUserDelete();

	boolean canUserEdit();

	boolean canUserCreate();

	Long getAccountId();

	boolean isUserSuperUser();

	String getFeatureName();

	String getUserDisallowedReason(Enum<?> outboundContactHub);

}
