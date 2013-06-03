package org.docear.syncdaemon.jnotify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.lang3.SystemUtils.*;

public class NativeLibraryResolver {
    private static final Logger logger = LoggerFactory.getLogger(NativeLibraryResolver.class);
    private final File destinationFolderNativeLibrary;

    public NativeLibraryResolver(final File destinationFolderNativeLibrary) {
        this.destinationFolderNativeLibrary = destinationFolderNativeLibrary;
    }

    private String nativeLibraryFilename() {
        String name = "libjnotify.so";
        if(IS_OS_WINDOWS) {
            name = OS_ARCH.contains("64") ? "jnotify_64bit.dll" : "jnotify.dll";
        } else if (IS_OS_MAC) {
            name = "libjnotify.jnilib";
        }
        return name;
    }

    private String nativeLibraryJarPath() {
        return "/native_libraries/" + System.getProperty("sun.arch.data.model") + "bits" + "/" + nativeLibraryFilename();
    }

    private File destinationFileForNativeLibrary() throws IOException {
        forceMkdir(destinationFolderNativeLibrary);
        final File destination = new File(destinationFolderNativeLibrary, nativeLibraryFilename());
        return destination;
    }

    public void run() throws IOException {
        try {
            updateNativeLibraryPath();
        } catch (NoSuchFieldException e) {
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        }
        final String libraryPath = nativeLibraryJarPath();
        logger.debug("loading jnotify library from {}", libraryPath);
        final InputStream resourceAsStream = getClass().getResourceAsStream(libraryPath);
        if(resourceAsStream == null)
            throw new RuntimeException("Cannot find jnotify in. {" + libraryPath + "} of the jnotify JAR");
        final File destination = destinationFileForNativeLibrary();
        copyInputStreamToFile(resourceAsStream, destination);
        destination.setExecutable(true);
    }

    private void updateNativeLibraryPath() throws IOException, NoSuchFieldException, IllegalAccessException {
        final String oldPath = System.getProperty("java.library.path");
        final String newPath = destinationFolderNativeLibrary.getCanonicalPath() + PATH_SEPARATOR + oldPath;
        System.setProperty("java.library.path", newPath);
        logger.debug("'java.library.path' path is now {}", System.getProperty("java.library.path"));

        //hack http://blog.cedarsoft.com/2010/11/setting-java-library-path-programmatically/
        //necessary since "java.library.path" is cached
        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(null, null);
    }
}
