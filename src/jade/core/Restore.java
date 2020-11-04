package jade.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Restore {
	String DEFAULT_RESTORE = "_DEFAULT_";
	
	boolean skip() default false;
	String method() default DEFAULT_RESTORE;
}
