package org.docear.syncdaemon.client;

import java.util.List;

import org.docear.syncdaemon.fileindex.FileMetaData;

public class FolderMetaData {
	
	private final FileMetaData metaData;
	private final List<FileMetaData> contents;
	
	public FolderMetaData(FileMetaData metaData, List<FileMetaData> contents){
		this.metaData = metaData;
		this.contents = contents;
	}
	
	public List<FileMetaData> getContents() {
		return contents;
	}
	
	public FileMetaData getMetaData() {
		return metaData;
	}
}
