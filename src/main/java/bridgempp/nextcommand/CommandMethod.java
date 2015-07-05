package bridgempp.nextcommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import bridgempp.PermissionsManager.Permission;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface CommandMethod
{
	String trigger() default "?$CLASSNAME ";
	Permission permissions() default Permission.EXIT;
}
