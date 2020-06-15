package thingverse.tracing.annotation;


import java.lang.annotation.*;

/**
 * A method annotated with this annotation will be traced if tracing is enabled.
 *
 * @author Arun Patra
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TraceTag {

    /**
     * The key name
     *
     * @return Keyname.
     */
    String key();

    /**
     * The value
     *
     * @return value
     */
    String value();
}
