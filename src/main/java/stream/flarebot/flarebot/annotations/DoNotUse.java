package stream.flarebot.flarebot.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any method or constructor with this is meant for use in a specific system only, it is not meant to be just used
 * unless you know exactly what you're doing. These will usually be done with other required actions so DO NOT use any
 * piece of code annotated with this.
 */
@Documented
@Target(value = { ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.SOURCE)
public @interface DoNotUse {}