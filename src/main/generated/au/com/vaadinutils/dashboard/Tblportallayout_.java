/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package au.com.vaadinutils.dashboard;

import au.com.vaadinutils.entity.BaseCrudEntity_;
import java.lang.Boolean;
import java.lang.Long;
import java.lang.String;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=au.com.vaadinutils.dashboard.Tblportallayout.class)
public class Tblportallayout_ extends BaseCrudEntity_  {
    public static volatile SingularAttribute<Tblportallayout,Long> account;
    public static volatile SingularAttribute<Tblportallayout,Boolean> default_;
    public static volatile SingularAttribute<Tblportallayout,String> name;
    public static volatile SetAttribute<Tblportallayout,Tblportal> portals;
}