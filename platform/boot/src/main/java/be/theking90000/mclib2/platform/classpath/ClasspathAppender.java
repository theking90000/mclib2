package be.theking90000.mclib2.platform.classpath;


import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * A helper class that appends a given {@link Path} to the classpath (for example by adding the path's url to a URLClassLoader).
 */
public interface ClasspathAppender {

    /**
     * Appends the given path to the classpath.
     *
     * @param path the path
     * @throws MalformedURLException in case the path needs to be turned into a URL, this can be thrown
     */
    void appendFileToClasspath(Path path) throws MalformedURLException;
}