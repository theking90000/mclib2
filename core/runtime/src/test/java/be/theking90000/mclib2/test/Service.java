package be.theking90000.mclib2.test;

import be.theking90000.mclib2.annotations.RegisteredAnnotation;
import java.lang.annotation.*;

@RegisteredAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {}