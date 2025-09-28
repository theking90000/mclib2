package be.theking90000.mclib2.config;


import java.io.*;

public interface ConfigResolver {

    /**
     * Resolve a given configuration name to an InputStream
     * @param name the name of the configuration to resolve
     * @return the InputStream of the resolved configuration, or null if not found
     */
    InputStream resolve(String name) throws IOException;

    default String resolveAsString(String name) throws IOException {
        try(InputStream in = resolve(name)) {
            if(in == null) return null;
            try (BufferedInputStream bin = new BufferedInputStream(in)) {
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bin.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, bytesRead));
                }
                return sb.toString();
            }
        }
    }

    /**
     * Store a configuration with the given name
     * @param name the name of the configuration to store
     * @return the OutputStream to write the configuration to
     */
    OutputStream store(String name) throws IOException;

}
