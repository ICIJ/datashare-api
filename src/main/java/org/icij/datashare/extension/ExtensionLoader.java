package org.icij.datashare.extension;

import org.icij.datashare.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static java.util.Optional.ofNullable;

public class ExtensionLoader {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final Path extensionsDir;

    public ExtensionLoader(Path extensionsDir) {
        this.extensionsDir = extensionsDir;
    }

    public synchronized <T> void load(Consumer<T> registerFunc, Predicate<Class<?>> predicate) throws FileNotFoundException {
        File[] jars = getJars();
        LOGGER.info("read directory {} and found jars (executable): {}", extensionsDir, jars);
        DynamicClassLoader classLoader = (DynamicClassLoader) ClassLoader.getSystemClassLoader();
        for (File jar : jars) {
            try {
                LOGGER.info("loading jar {}", jar);
                classLoader.add(jar.toURI().toURL());
                Class<?> expectedClass = findClassesInJar(predicate, jar);
                if (expectedClass != null) {
                    registerFunc.accept((T) expectedClass);
                }
            } catch (IOException e) {
                LOGGER.error("Cannot load jar " + jar, e);
            }
        }
    }

    synchronized <T> Class<?> findClassesInJar(Predicate<Class<?>> predicate, final File jarFile) throws IOException {
        URLClassLoader ucl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, getClass().getClassLoader());
        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile));
        for (JarEntry jarEntry = jarInputStream.getNextJarEntry(); jarEntry != null; jarEntry = jarInputStream.getNextJarEntry()) {
            if (jarEntry.getName().endsWith(".class")) {
                String classname = jarEntry.getName().replaceAll("/", "\\.");
                classname = classname.substring(0, classname.length() - 6);
                if (!classname.contains("$") && !"module-info".equals(classname)) {
                    try {
                        final Class<?> myLoadedClass = Class.forName(classname, false, ucl);
                        if (predicate.test(myLoadedClass) &&
                                !myLoadedClass.isInterface() && !Modifier.isAbstract(myLoadedClass.getModifiers())) {
                            return myLoadedClass;
                        }
                    } catch (ClassNotFoundException|LinkageError e) {
                        LOGGER.warn("cannot load class {}: {}", classname, e);
                    }
                }
            }
        }
        return null;
    }

    File[] getJars() throws FileNotFoundException {
        return ofNullable(extensionsDir.toFile().listFiles(file -> file.toString().endsWith(".jar") && file.canExecute())).
                orElseThrow(() -> new FileNotFoundException("invalid path for extensions: " + extensionsDir));
    }
}
