package org.docear.syncdaemon.fileactors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import org.apache.commons.io.FileUtils;
import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.PersistenceException;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.fest.assertions.Assertions;
import org.junit.*;

import java.io.File;
import java.io.IOException;

/**
 * At the moment windows specific
 * will be changed very soon :) (Julius)
 */
public class FileChangeActorTestsITest {

    private final static HashAlgorithm hashAlgorithm = new SHA2();
    private final static User user = new User("Julius", "Julius-token");
    private final static String projectId = "507f191e810c19729de860ea";
    private final static String rootPath = System.getProperty("user.home")+File.separator+"docear"+File.separator+"testProject";
    private final static String filePath = "/new.mm";
    private final static Project project = new Project(projectId, rootPath, 0L);
    private final static FileMetaData fileMetaData = FileMetaData.file(filePath, "", projectId, false, 0L);
    private final static File fileOnFS = new File("D:\\p1\\new.mm");
    private static ActorSystem actorSystem;
    private static Daemon daemon;
    private static ActorRef fileChangeActor;
    private static ClientService clientService;
    private static IndexDbService indexDbService;
    private static File testFile;
    private static String testFileHash;

    @BeforeClass
    public static void beforeClass() throws IOException {
        actorSystem = ActorSystem.apply();
        daemon = TestUtils.testDaemon();
        daemon.onStart();
        fileChangeActor = daemon.getFileChangeActor();
        clientService = daemon.service(ClientService.class);
        indexDbService = daemon.service(IndexDbService.class);

        //put file locally
        String pathOfClass = FileChangeActorTestsITest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        testFile = new File(pathOfClass + File.separator + "new.mm");
        testFileHash = hashAlgorithm.generate(testFile);
    }

    @AfterClass
    public static void afterClass() {
        deleteTestFile();
    }

    @Before
    public void setUp() {
        deleteTestFile();
    }

    @After
    public void tearDown() throws IOException {
        deleteTestFile();
        //put new.mm on server
        FileUtils.copyFile(testFile, fileOnFS);
        final FileMetaData newMeta = FileMetaData.file(filePath, testFileHash, projectId, false, getCurrentRevisionOnServerOfTestFile());
        clientService.upload(user, project, newMeta);

        deleteTestFile();
    }

    @Test
    public void testNewFileOnServer() throws PersistenceException {
        indexDbService.save(FileMetaData.file(filePath, testFileHash, projectId, true, getCurrentRevisionOnServerOfTestFile()));

        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedOnServer(project, fileMetaData), getRef());
            expectNoMsg();
        }};
        Assertions.assertThat(fileOnFS).exists();
    }

    @Test
    public void testFileDeletedOnServer() throws IOException {

        FileUtils.copyFile(testFile, fileOnFS);

        final String hash = hashAlgorithm.generate(fileOnFS);

        //put entry in db
        indexDbService.save(FileMetaData.file(filePath, hash, projectId, true, getCurrentRevisionOnServerOfTestFile()));

        //delete
        final FileMetaData deletedServerMeta = FileMetaData.file(filePath, "", projectId, true, getCurrentRevisionOnServerOfTestFile() + 1);
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedOnServer(project, deletedServerMeta), getRef());
            expectNoMsg();
        }};

        Assertions.assertThat(fileOnFS).doesNotExist();
    }

    @Test
    public void testFileUpdatedOnServer() throws IOException {
        final String oldContent = "This is a file";
        //prepare
        FileUtils.write(fileOnFS, oldContent);

        final String fileHash = hashAlgorithm.generate(fileOnFS);
        final Long currentRev = getCurrentRevisionOnServerOfTestFile();
        indexDbService.save(FileMetaData.file(filePath, fileHash, projectId, false, currentRev - 1));

        final FileMetaData newMeta = FileMetaData.file(filePath, testFileHash, projectId, false, currentRev);
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedOnServer(project, newMeta), getRef());
            expectNoMsg();
        }};

        Assertions.assertThat(fileOnFS).hasSameContentAs(testFile);
    }

    @Test
    public void testFileUpdatedOnServerToFolder() throws IOException {
        final Long revision = getCurrentRevisionOnServerOfTestFile();

        final FileMetaData newMeta = FileMetaData.file(filePath, "", projectId, true, revision);
        indexDbService.save(newMeta);

        final FileMetaData folderMeta = FileMetaData.folder(projectId, filePath, false, revision + 1);
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedOnServer(project, folderMeta), getRef());
            expectNoMsg();
        }};

        Assertions.assertThat(fileOnFS).isDirectory();
    }

    @Test
    public void testNewFileLocally() throws IOException {
        final Long currentRevision = getCurrentRevisionOnServerOfTestFile();
        clientService.delete(user,project,FileMetaData.file(filePath,testFileHash,projectId,true,currentRevision));
        FileUtils.copyFile(testFile,fileOnFS);

        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedLocally(project,FileMetaData.newFile(filePath,testFileHash,projectId)), getRef());
            //expectNoMsg(Duration.apply(5, TimeUnit.DAYS));
            expectNoMsg();
        }};

        final FileMetaData serverMeta = clientService.getCurrentFileMetaData(user,fileMetaData);
        Assertions.assertThat(serverMeta.getRevision()).isGreaterThan(currentRevision);
    }

    @Test
    public void testFileDeletedLocally() throws IOException {
        final Long revision = getCurrentRevisionOnServerOfTestFile();
        indexDbService.save(FileMetaData.file(filePath, testFileHash, projectId, false, revision));

        //delete
        final FileMetaData deletedLocalMeta = FileMetaData.file(filePath, "", projectId, true, revision);
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedLocally(project, deletedLocalMeta), getRef());
            expectNoMsg();
        }};

        final FileMetaData serverMeta = clientService.getCurrentFileMetaData(user,fileMetaData);
        Assertions.assertThat(serverMeta.isDeleted()).isTrue();
    }

    @Test
    public void testFileUpdatedLocally() throws IOException {

        //prepare index db
        final Long currentRev = getCurrentRevisionOnServerOfTestFile();
        indexDbService.save(FileMetaData.file(filePath, testFileHash, projectId, false, currentRev));

        //change file
        final String newContent = "This is a file";
        FileUtils.write(fileOnFS, newContent);
        final String fileHash = hashAlgorithm.generate(fileOnFS);

        //tell actor, that file has changed
        final FileMetaData newMeta = FileMetaData.file(filePath, fileHash, projectId, false, currentRev);
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedLocally(project, newMeta), getRef());
            expectNoMsg();
        }};

        final Long newRevision = getCurrentRevisionOnServerOfTestFile();
        Assertions.assertThat(newRevision).isGreaterThan(currentRev);
    }

    @Test
    public void testFileUpdatedLocallyToFolder() throws IOException {
        //first need to delete (file can't change to folder directly)
        deleteTestFile();
        final FileMetaData serverMeta = clientService.delete(user,project,fileMetaData);
        indexDbService.save(serverMeta);

        fileOnFS.mkdir();

        final FileMetaData newMeta = FileMetaData.folder(projectId,filePath,false,serverMeta.getRevision());
        new JavaTestKit(actorSystem) {{
            fileChangeActor.tell(new Messages.FileChangedLocally(project, newMeta), getRef());
            expectNoMsg();
        }};

        final FileMetaData fileMetaDataServer = clientService.getCurrentFileMetaData(user,fileMetaData);
        Assertions.assertThat(fileMetaDataServer.isFolder()).isTrue();
    }


    @Test
    public void testFileUpdatedOnServerThanLocalSameTime() {

    }

    @Test
    public void testFileUpdatedLocalThanServerSameTime() {

    }

    private Long getCurrentRevisionOnServerOfTestFile() {
        return clientService.getCurrentFileMetaData(user, fileMetaData).getRevision();
    }

    private static void deleteTestFile() {
        //delete locally
        if (fileOnFS.exists()) {
            fileOnFS.delete();
        }
        Assertions.assertThat(fileOnFS).doesNotExist();
    }
}
