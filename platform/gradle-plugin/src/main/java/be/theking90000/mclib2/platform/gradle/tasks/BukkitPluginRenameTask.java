package be.theking90000.mclib2.platform.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public abstract class BukkitPluginRenameTask extends DefaultTask {

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getInput();

    @Input
    public abstract Property<String> getNewClassName();

    @Input
    public abstract Property<String> getOldClassName();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    private Set<String> writtenEntries = new HashSet<>();

    public BukkitPluginRenameTask() {
        getOldClassName().convention("be/theking90000/mclib2/platform/adapter/BukkitAdapter");
    }

    private boolean alreadyAdded(JarOutputStream jos, String name) {
        return !writtenEntries.add(name); // true if duplicate
    }

    private String javaName(String name) {
        return name.replace("/", ".").replace(".class", "");
    }

    @TaskAction
    public void run() throws IOException {
        File outputJar = getOutput().getAsFile().get();

        outputJar.getParentFile().mkdirs();

        try(FileOutputStream fos = new FileOutputStream(outputJar);
            JarOutputStream jos = new JarOutputStream(fos)) {

            for(File inputJar : getInput()) {
                try (JarFile jarFile = new JarFile(inputJar)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    JarEntry entry;
                    while (entries.hasMoreElements()) {
                        entry = entries.nextElement();

                        if (entry.isDirectory()) continue;

                        try (InputStream is = jarFile.getInputStream(entry)) {
                            String name = entry.getName();

                            if(alreadyAdded(jos, name)) continue;

                            if(name.endsWith(".class")) {
                                byte[] modifiedClass = processClass(is);
                                String outName = name.equals(getOldClassName().get() + ".class") ? getNewClassName().get() + ".class" : name;
                                jos.putNextEntry(new JarEntry(outName));
                                jos.write(modifiedClass);
                                jos.closeEntry();
                            } else if(name.equals("plugin.yml")) {
                                String file = readToString(is);
                                file = file.replace("main: " + javaName(getOldClassName().get()), "main: " + javaName(getNewClassName().get()));
                                jos.putNextEntry(new JarEntry(name));
                                jos.write(file.getBytes(StandardCharsets.UTF_8));
                                jos.closeEntry();
                            } else {
                                jos.putNextEntry(new JarEntry(name));
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    jos.write(buffer, 0, bytesRead);
                                }
                                jos.closeEntry();
                            }
                        }

                    }
                }
            }
        }
    }


    private byte[] processClass(InputStream is) throws IOException {
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = new ClassRemapper(
                cw,
                new org.objectweb.asm.commons.Remapper() {
                    @Override
                    public String map(String internalName) {
                        if (internalName.equals(getOldClassName().get())) {
                            return getNewClassName().get();
                        }
                        return internalName;
                    }
                });
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    public static String readToString(InputStream in) throws IOException {
        StringWriter writer = new StringWriter();
        try (Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            char[] buffer = new char[1024];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        return writer.toString();
    }

}
