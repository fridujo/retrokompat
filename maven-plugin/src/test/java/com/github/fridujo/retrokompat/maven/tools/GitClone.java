package com.github.fridujo.retrokompat.maven.tools;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface GitClone {

    String NO_VALUE = "<NO_VALUE>";

    /**
     * @return the URI of the git repository to clone
     */
    String uri();

    String tag() default NO_VALUE;
}
