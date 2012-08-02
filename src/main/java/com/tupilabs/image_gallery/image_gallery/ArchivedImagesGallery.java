/*
 * The MIT License
 *
 * Copyright (c) <2012> <Bruno P. Kinoshita>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tupilabs.image_gallery.image_gallery;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * An image gallery of archived artifacts. Its descriptor is a static inner 
 * class.
 *
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class ArchivedImagesGallery extends ImageGallery {

	private static Logger LOGGER = Logger.getLogger("com.tupilabs.image_gallery");
	
	private static final long serialVersionUID = -1981209232197421074L;

	/**
	 * Title.
	 */
	private final String title;
	/**
	 * Include pattern.
	 */
	private final String includes;
	/**
	 * Images width.
	 */
	private final Integer imageWidth;
	/**
	 * If checked, marks the build as unstable if no archives were found.
	 */
	private final boolean markBuildAsUnstableIfNoArchivesFound;
	
	/**
	 * Constructor called from jelly.
	 * @param includes
	 * @param imageWidth
	 * @param markBuildAsUnstableIfNoArchivesFound
	 */
	@DataBoundConstructor
	public ArchivedImagesGallery(String title, String includes, Integer imageWidth,
			boolean markBuildAsUnstableIfNoArchivesFound) {
		super();
		this.title = title;
		this.includes = includes;
		this.imageWidth = imageWidth;
		this.markBuildAsUnstableIfNoArchivesFound = markBuildAsUnstableIfNoArchivesFound;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return the includes
	 */
	public String getIncludes() {
		return includes;
	}
	
	/**
	 * @return the imageWidth
	 */
	public Integer getImageWidth() {
		return imageWidth;
	}
	
	/**
	 * @return the markBuildAsUnstableIfNoArchivesFound
	 */
	public boolean isMarkBuildAsUnstableIfNoArchivesFound() {
		return markBuildAsUnstableIfNoArchivesFound;
	}

	@Extension
	public static class DescriptorImpl extends ImageGalleryDescriptor {
		/* (non-Javadoc)
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return "Archived images gallery";
		}
	}

	/* (non-Javadoc)
	 * @see com.tupilabs.image_gallery.image_gallery.ImageGallery#createImageGallery(hudson.model.AbstractBuild, hudson.model.BuildListener)
	 */
	@Override
	public boolean createImageGallery(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException, IOException {
		listener.getLogger().append("Creating archived images gallery.");
		if(build.getHasArtifacts()) {
			File artifactsDir = build.getArtifactsDir();
			FilePath artifactsPath = new FilePath(artifactsDir);
			FilePath[] foundFiles = artifactsPath.list(includes);
			if(LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Found " + (foundFiles != null ? foundFiles.length : 0) + " files.");
			}
			if(foundFiles != null && foundFiles.length > 0) {
				List<String> images = new ArrayList<String>();
				for(FilePath foundFile : foundFiles) {
					String fileName = "";
					FilePath temp = foundFile;
					while(!temp.getParent().equals(artifactsPath)) {
						fileName = foundFile.getParent().getName() + "/" + fileName;
						temp = temp.getParent();
					}
					if(fileName.length() > 0) {
						fileName += "/";
					}
					fileName += foundFile.getName();
					images.add(fileName);
				}
				build.addAction(new ArchivedImagesGalleryBuildAction(this.title, images.toArray(new String[0]), imageWidth));
			} else {
				listener.getLogger().append("No files found for image gallery.");
			}
		} else {
			if(markBuildAsUnstableIfNoArchivesFound) {
				build.setResult(Result.UNSTABLE);
			}
			listener.getLogger().append("This build has no artifacts. Skipping image gallery in this build.");
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.tupilabs.image_gallery.image_gallery.ImageGallery#getProjectActions(hudson.model.AbstractProject)
	 */
	@Override
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		AbstractBuild<?, ?> build = project.getLastBuild();
		if(build != null) {
			File artifactsDir = build.getArtifactsDir();
			FilePath artifactsPath = new FilePath(artifactsDir);
			List<String> images = new ArrayList<String>();
			try {
				FilePath[] foundFiles = artifactsPath.list(includes);
				if(LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Found " + (foundFiles != null ? foundFiles.length : 0) + " files.");
				}
				if(foundFiles != null && foundFiles.length > 0) {
					for(FilePath foundFile : foundFiles) {
						String fileName = "";
						FilePath temp = foundFile;
						while(!temp.getParent().equals(artifactsPath)) {
							fileName = foundFile.getParent().getName() + "/" + fileName;
							temp = temp.getParent();
						}
						if(fileName.length() > 0) {
							fileName += "/";
						}
						fileName += foundFile.getName();
						images.add("" + build.getNumber() + "/artifact/" + fileName);
					}
				}
				ArchivedImagesGalleryProjectAction action = new ArchivedImagesGalleryProjectAction(this.title, images.toArray(new String[0]), this.imageWidth);
				return Arrays.asList(action);
			} catch(IOException ioe) {
				LOGGER.log(Level.WARNING, ioe.getMessage());
			} catch(InterruptedException ie) {
				LOGGER.log(Level.WARNING, ie.getMessage());
			}
			return Collections.emptyList();
		} else {
			return Collections.emptyList();
		}
	}
	
}
