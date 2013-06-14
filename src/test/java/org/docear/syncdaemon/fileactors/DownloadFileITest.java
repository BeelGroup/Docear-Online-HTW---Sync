package org.docear.syncdaemon.fileactors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.projects.Project;
import org.fest.assertions.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 *
 * At the moment windows specific
 * will be changed very soon :) (Julius)
 */
public class DownloadFileITest {

    @Test
    @Ignore
    public void testGetFileMetaData() {
        final ActorSystem actorSystem = ActorSystem.apply();
        final Daemon daemon = TestUtils.testDaemon();
        daemon.onStart();
        final ActorRef fileChangeActor = daemon.getFileChangeActor();
        final Project project = new Project("507f191e810c19729de860ea","D:\\p1",0L);
        final FileMetaData fileMetaData = FileMetaData.file("/new.mm","","507f191e810c19729de860ea", false, 0L);
        
        
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedOnServer(project,fileMetaData),getRef());
            expectNoMsg();
        }};

        final File file = new File("D:\\p1\\new.mm");
        Assertions.assertThat(file).exists();
        file.delete();
    }
}
