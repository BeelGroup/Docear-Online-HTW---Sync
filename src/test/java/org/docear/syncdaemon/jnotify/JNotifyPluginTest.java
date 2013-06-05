package org.docear.syncdaemon.jnotify;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;
import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.Daemon;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.docear.syncdaemon.TestUtils.*;
import static org.fest.assertions.Assertions.assertThat;

public class JNotifyPluginTest {
    @Test
    @Ignore
    public void testJNotifyIsActive() throws Exception {
        final Daemon daemon = testDaemon();
        daemon.onStart();
        final File file = new File(".marker");
        FileUtils.write(file, "old");
        file.deleteOnExit();
        final JNotifyListener listener = new JNotifyListener() {
            public boolean itWorked = false;

            @Override
            public void fileCreated(int i, String s, String s2) {
            }

            @Override
            public void fileDeleted(int i, String s, String s2) {
            }

            @Override
            public void fileModified(final int wd, final String rootPath, final String name) {
                final String path = rootPath + name;
                if (path.endsWith(".marker")) {
                    itWorked = true;
                }
            }

            @Override
            public void fileRenamed(int i, String s, String s2, String s3) {
            }

            @Override
            public String toString() {
                return Boolean.toString(itWorked);
            }
        };
        final int id = JNotify.addWatch(file.getAbsolutePath(), JNotify.FILE_MODIFIED, true, listener);
        //fire enough events
        FileUtils.write(file, "new");
        FileUtils.write(file, "new");
        FileUtils.write(file, "new");
        JNotify.removeWatch(id);
        assertThat(listener.toString()).isEqualTo("true");
    }
}
