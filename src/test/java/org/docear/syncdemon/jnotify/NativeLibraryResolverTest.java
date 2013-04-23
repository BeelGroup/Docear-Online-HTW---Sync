package org.docear.syncdemon.jnotify;

import org.fest.assertions.Condition;
import org.junit.Test;

import java.io.File;

import static org.apache.commons.io.FileUtils.getTempDirectory;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.fest.assertions.Assertions.assertThat;

public class NativeLibraryResolverTest {
    @Test
    public void testCopyingNativeLibraryFromJar() throws Exception {
        final File folderForLib = new File(getTempDirectory(), "sync-demon-test");
        if (folderForLib.exists()) {
            forceDelete(folderForLib);
        }
        new NativeLibraryResolver(folderForLib).run();
        assertThat(folderForLib).isDirectory();
        final File[] files = folderForLib.listFiles();
        assertThat(files).hasSize(1);
        assertThat(files[0]).describedAs("is executable").is(new Condition<File>() {
            @Override
            public boolean matches(File file) {
                return file.canExecute();
            }
        });
    }
}
