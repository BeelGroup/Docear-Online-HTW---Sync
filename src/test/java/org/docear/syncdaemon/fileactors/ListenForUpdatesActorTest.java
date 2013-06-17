package org.docear.syncdaemon.fileactors;

import java.io.File;

import org.docear.syncdaemon.Daemon;
import org.docear.syncdaemon.TestUtils;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ListenForUpdatesResponse;
import org.docear.syncdaemon.config.ConfigService;

import org.docear.syncdaemon.fileactors.Messages.FileChangedOnServer;
import org.docear.syncdaemon.fileactors.Messages.ProjectAdded;
import org.docear.syncdaemon.fileactors.Messages.ProjectDeleted;
import org.docear.syncdaemon.fileactors.ListenForUpdatesActor;
import org.docear.syncdaemon.fileindex.FileMetaData;
import org.docear.syncdaemon.fileindex.FileReceiver;
import org.docear.syncdaemon.hashing.HashAlgorithm;
import org.docear.syncdaemon.hashing.SHA2;
import org.docear.syncdaemon.indexdb.IndexDbService;
import org.docear.syncdaemon.indexdb.PersistenceException;
import org.docear.syncdaemon.indexdb.h2.H2IndexDbService;
import org.docear.syncdaemon.projects.Project;
import org.docear.syncdaemon.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.docear.syncdaemon.TestUtils.testDaemon;
import static org.fest.assertions.Assertions.assertThat;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class ListenForUpdatesActorTest {

    private final static HashAlgorithm hashAlgorithm = new SHA2();
    private final static User user = new User("Julius", "Julius-token");
    private final static String projectId = "507f191e810c19729de860ea";
    private final static String rootPath = "D:\\p1";
    private final static String filePath = "/new.mm";
    private final static Project project = new Project(projectId, rootPath, 0L);
    private final static FileMetaData fileMetaData = FileMetaData.file(filePath, "", projectId, false, 0L);
    private final static File fileOnFS = new File("D:\\p1\\new.mm");
    private static Daemon daemon;
    private static ActorRef listenForUpdatesActor;
    private static TestActorRef<TestActor> fileChangeActor;
    private static IndexDbService indexDbService;
    private static ClientService clientService;
    private static TestActor testActor;
    
    @Before
    public void setUp(){
        ActorSystem actorSystem = ActorSystem.create("System");
    	Props props = Props.apply(TestActor.class);
    	fileChangeActor = TestActorRef.create(actorSystem, props, "testActor");
    	testActor = fileChangeActor.underlyingActor();
    	testActor.resetCounter();
        
        daemon = TestUtils.testDaemon();
        daemon.onStart();
        clientService = daemon.service(ClientService.class);
        indexDbService = daemon.service(IndexDbService.class);
        
        listenForUpdatesActor = actorSystem.actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                final ConfigService service = daemon.service(ConfigService.class);
                return (UntypedActor) new ListenForUpdatesActor(user, clientService, fileChangeActor, indexDbService, service);
            }
        }), "listenForUpdatesActor");
    }
    
    @After
    public void tearDown() throws Exception {
        daemon.onStop();
        daemon = null;
        indexDbService = null;
    }
    
    @Test
    @Ignore
    public void testNewProject() throws PersistenceException {
        	ListenForUpdatesResponse response = new ListenForUpdatesResponse();
        	Map<String, Long> newProjects = new HashMap<String, Long>();
        	newProjects.put(projectId, 8L);
        	response.setNewProjects(newProjects);
        	
        	listenForUpdatesActor.tell(response, fileChangeActor);

        	int iterationCnt = 10;
        	while (iterationCnt > 0
        			&& testActor.getTotalCounter() != 1
        			&& testActor.getProjectAddedCounter() != 1) {
        		iterationCnt--;
        		try {
        			Thread.sleep(500);
        		} catch (Exception e){}
        	}
        	
        	assertThat(testActor.getTotalCounter()).isEqualTo(1);
        	assertThat(testActor.getProjectAddedCounter()).isEqualTo(1);
    }
    
    @Test
    @Ignore
    public void testDeleteProject() throws PersistenceException {
        	ListenForUpdatesResponse response = new ListenForUpdatesResponse();
        	List<String> deletedProjects = new LinkedList<String>();
            final String projectId = "507f191e810c19729de860ea";
            deletedProjects.add(projectId);
        	response.setDeletedProjects(deletedProjects);
        	
        	indexDbService.save(FileMetaData.file(filePath, "hash", projectId, true, 8));

        	//listenForUpdatesActor.tell(response, fileChangeActor);

        	int iterationCnt = 10;
        	while (iterationCnt > 0
        			&& testActor.getTotalCounter() != 1
        			&& testActor.getProjectDeletedCounter() != 1) {
        		iterationCnt--;
        		try {
        			Thread.sleep(500);
        		} catch (Exception e){}
        	}
        	
        	assertThat(testActor.getTotalCounter()).isEqualTo(1);
        	assertThat(testActor.getProjectDeletedCounter()).isEqualTo(1);
    }

    private static class TestActor extends UntypedActor{
    	
    	private int fileChangedOnServerCounter = 0;
    	private int projectDeletedCounter = 0;
    	private int projectAddedCounter = 0;
    	private int otherCounter = 0;
    	private int totalCounter = 0;
    	
    	private static final Logger logger = LoggerFactory.getLogger(TestActor.class);
    	
    	public void resetCounter(){
    		fileChangedOnServerCounter = 0;
        	projectDeletedCounter = 0;
        	projectAddedCounter = 0;
        	otherCounter = 0;
        	totalCounter = 0;
    	}
       	
    	@Override
    	public void onReceive(Object message) throws Exception {
    		totalCounter++;
    		if (message instanceof FileChangedOnServer){
    			fileChangedOnServerCounter++;
    		} else if (message instanceof ProjectDeleted){
    			projectDeletedCounter++;
    		} else if (message instanceof ProjectAdded){
    			projectAddedCounter++;
    		} else {
    			otherCounter++;
    		}
    	}
    	
    	public int getTotalCounter() {
    		return totalCounter;
		}
    	
    	public int getProjectAddedCounter() {
			return projectAddedCounter;
		}

		public int getFileChangedOnServerCounter() {
			return fileChangedOnServerCounter;
		}

		public int getProjectDeletedCounter() {
			return projectDeletedCounter;
		}

		public int getOtherCounter() {
			return otherCounter;
		}
    }
}
