package be.theking90000.mclib2.config;

import be.theking90000.mclib2.annotations.RegisteredAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@RegisteredAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Config {

    /**
     * The name/identifier of the config
     * This is used to identify the config file and should be unique per plugin/mod
     * Example:
     *  Config(name="main") will result in generated file "main.yml"
     * @return the name of the config
     */
    String name();
}
