package au.com.vaadinutils.crud;



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

}
