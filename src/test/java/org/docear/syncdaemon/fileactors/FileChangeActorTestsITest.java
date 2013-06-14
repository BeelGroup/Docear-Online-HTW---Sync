package org.docear.syncdaemon.fileactors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.UploadFileITest;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.*;

import java.io.File;
import java.io.IOException;

/**
 *
 * At the moment windows specific
 * will be changed very soon :) (Julius)
 */
public class FileChangeActorTestsITest {

    private static ActorSystem actorSystem;
    private static Daemon daemon;
    private static ActorRef fileChangeActor;
    private static ClientService clientService;
    private static IndexDbService indexDbService;

    private final static HashAlgorithm hashAlgorithm = new SHA2();
    private final static User user = new User("Julius","Julius-token");
    private final static String projectId = "507f191e810c19729de860ea";
    private final static String rootPath = "D:\\p1";
    private final static String filePath = "/new.mm";
    private final static Project project = new Project(projectId,rootPath,0L);
    private final static FileMetaData fileMetaData = FileMetaData.file(filePath,"",projectId, false, 0L);
    private final static File fileOnFS = new File("D:\\p1\\new.mm");

    @BeforeClass
    public static void beforeClass() {
        actorSystem = ActorSystem.apply();
        daemon = TestUtils.testDaemon();
        daemon.onStart();
        fileChangeActor = daemon.getFileChangeActor();
        clientService = daemon.service(ClientService.class);
        indexDbService = daemon.service(IndexDbService.class);
    }

    @Before
    public void setUp() {
        if(fileOnFS.exists()) {
            fileOnFS.delete();
        }
        Assertions.assertThat(fileOnFS).doesNotExist();
    }

    @After
    public void tearDown() {
        if(fileOnFS.exists()) {
            fileOnFS.delete();
        }
        Assertions.assertThat(fileOnFS).doesNotExist();
    }

    @AfterClass
    public static void afterClass() {
        if(fileOnFS.exists()) {
            fileOnFS.delete();
        }
        Assertions.assertThat(fileOnFS).doesNotExist();
    }

    @Test
    public void testNewFileOnServer() {
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedOnServer(project,fileMetaData),getRef());
            expectNoMsg();
        }};
        Assertions.assertThat(fileOnFS).exists();
    }

    @Test
    public void testFileDeletedOnServer() throws IOException {
        //put file locally
        String pathOfClass = UploadFileITest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final File testFile = new File(pathOfClass + File.separator + "new.mm");
        FileUtils.copyFile(testFile, fileOnFS);

        final String hash = hashAlgorithm.generate(fileOnFS);

        //put entry in db
        indexDbService.save(FileMetaData.file(filePath,hash,projectId,true,getCurrentRevisionOnServerOfTestfile()));

        //delete
        final FileMetaData deletedServerMeta = FileMetaData.file(filePath,"",projectId,true,getCurrentRevisionOnServerOfTestfile()+1);
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedOnServer(project,deletedServerMeta),getRef());
            expectNoMsg();
        }};

        Assertions.assertThat(fileOnFS).doesNotExist();
    }

    @Test
    public void testFileUpdatedOnServer() {

    }

    @Test
    public void testFileUpdatedOnServerThanLocalSameTime() {

    }

    @Test
    public void testFileUpdatedLocalThanServerSameTime() {

    }

    private Long getCurrentRevisionOnServerOfTestfile() {
        return clientService.getCurrentFileMetaData(user,fileMetaData).getRevision();
    }
}
