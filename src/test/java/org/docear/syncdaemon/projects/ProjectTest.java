package org.docear.syncdaemon.projects;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class ProjectTest {

	@Test
	public void testToRelativePath(){
		String rootPath = "D:/foo/bar";
		String relativPath = "test/test/foo/bar/";
		String path = rootPath + relativPath;
		String result = Project.toRelativePath(rootPath, path);
		assertThat(result).isEqualTo(FilenameUtils.separatorsToSystem(relativPath));
	}
	
	@Test
	public void testToRelativePathByProject(){
		String rootPath = "D:/foo/bar";
		Project project = new Project("-1", rootPath, -1,"name");
		String relativPath = "test/test/foo/bar/";
		String path = rootPath + relativPath;
		String result = project.toRelativePath(path);
		assertThat(result).isEqualTo(FilenameUtils.separatorsToSystem(relativPath));
	}
	
	@Test
	public void testToRelativePathByProjectFail(){
		String rootPath = "D:/foo/bar";
		Project project = new Project("-1", rootPath, -1,"name");
		String path = "ThisIsNotAValidRootPath";
		String result = project.toRelativePath(path);
		assertThat(result).isNull();
	}
}
