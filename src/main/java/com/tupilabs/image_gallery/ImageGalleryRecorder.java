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
package com.tupilabs.image_gallery;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.tupilabs.image_gallery.image_gallery.ImageGallery;

/**
 * An image gallery recorder. 
 * <p> 
 * It uses {@link ImageGallery ImageGallery} extension point to create 
 * galleries for different formats.
 *
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @see ImageGallery
 * @since 0.1
 */
public class ImageGalleryRecorder extends Recorder {
	
	private static Logger LOGGER = Logger.getLogger("com.tupilabs.image_gallery");

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	
	/**
	 * List of ImageGallery (an extension point defined in this plug-in).
	 */
	private final List<ImageGallery> imageGalleries;
	
	
	@DataBoundConstructor
	public ImageGalleryRecorder(List<ImageGallery> imageGalleries) {
		this.imageGalleries = imageGalleries;
	}
	
	/**
	 * @return the imageGalleries
	 */
	public List<ImageGallery> getImageGalleries() {
		return imageGalleries;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	/* (non-Javadoc)
	 * @see hudson.tasks.BuildStepCompatibilityLayer#getProjectActions(hudson.model.AbstractProject)
	 */
	@Override
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		Collection<Action> actions = new ArrayList<Action>();
		if(this.imageGalleries != null) {
			for(ImageGallery imageGallery : this.imageGalleries) {
				if(LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Add project actions for image gallery: " + imageGallery.getDescriptor().getDisplayName());
				}
				@SuppressWarnings("unchecked")
				Collection<Action> imageGalleryActions = (Collection<Action>) imageGallery.getProjectActions(project);
				for(Action imageGalleryAction : imageGalleryActions) {
					actions.add(imageGalleryAction);
				}
			}
		}
		return actions;
	}
	
	/* (non-Javadoc)
	 * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		listener.getLogger().append("Creating image galleries.");
		boolean r = true;
		for(ImageGallery imageGallery : this.imageGalleries) {
			try {
				if(LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Creating image gallery: " + imageGallery.getDescriptor().getDisplayName());
				}
				imageGallery.createImageGallery(build, listener);
			} catch (IOException ioe) {
				r = false;
				ioe.printStackTrace(listener.getLogger());
			} catch(InterruptedException ie) {
				r = false;
				ie.printStackTrace(listener.getLogger());
			}
		}
		return r;
	}
	
	/* (non-Javadoc)
	 * @see hudson.tasks.Recorder#getDescriptor()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public BuildStepDescriptor getDescriptor() {
		return DESCRIPTOR;
	}
	
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		// exposed for jelly
		public final Class<ImageGalleryRecorder> imageGalleryBuildType = ImageGalleryRecorder.class;
		
		public DescriptorImpl() {
			super(ImageGalleryRecorder.class);
		}
		
		/* (non-Javadoc)
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return "Create image gallery";
		}

		/* (non-Javadoc)
		 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
		 */
		@Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			return Boolean.TRUE;
		}
		
		// exposed for Jelly
	    public List<Descriptor<? extends ImageGallery>> getApplicableImageGalleries(AbstractProject<?, ?> p) {
	    	List<Descriptor<? extends ImageGallery>> list = new LinkedList<Descriptor<? extends ImageGallery>>();
	    	for(Descriptor<? extends ImageGallery> rs : ImageGallery.all()) {
	    		list.add(rs);
	    	}
	    	return list;
	    }
		
		/*
		 * --- Validation methods ---
		 */
		public FormValidation doCheckMandatory(@QueryParameter String value) {
			FormValidation returnValue = FormValidation.ok();
			if (StringUtils.isBlank(value)) {
				returnValue = FormValidation.error("This option is required");
			}
			return returnValue;
		}
		
	}

}
