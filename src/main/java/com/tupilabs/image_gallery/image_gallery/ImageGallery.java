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

import hudson.DescriptorExtensionList;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Node;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;


/**
 * Image gallery extension point. Multiple image gallery types 
 * extend this class.  
 *
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public abstract class ImageGallery implements Serializable, Describable<ImageGallery>, Comparable<ImageGallery> {

	private static final long serialVersionUID = -1438998620198424163L;

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ImageGallery o) {
		return o != null ? this.getDescriptor().getDisplayName().compareTo(o.getDescriptor().getDisplayName()) : 0;
	}

	/* (non-Javadoc)
	 * @see hudson.model.Describable#getDescriptor()
	 */
	public Descriptor<ImageGallery> getDescriptor() {
		return (ImageGalleryDescriptor) Hudson.getInstance().getDescriptor(getClass());
	}
	
	public static DescriptorExtensionList<ImageGallery, Descriptor<ImageGallery>> all() {
		return Hudson.getInstance().<ImageGallery, Descriptor<ImageGallery>> getDescriptorList(ImageGallery.class);
	}

	public static DescriptorExtensionList<ImageGallery, Descriptor<ImageGallery>> allExcept(Node current) {
		return Hudson.getInstance().<ImageGallery, Descriptor<ImageGallery>> getDescriptorList(ImageGallery.class);
	}
	
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		return null;
	}
	
	public abstract boolean createImageGallery(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException, IOException;

}
