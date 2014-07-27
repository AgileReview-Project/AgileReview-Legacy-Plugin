/**
 * Copyright (c) 2011, 2012 AgileReview Development Team and others.
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License - v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Malte Brunnlieb, Philipp Diebold, Peter Reuter, Thilo Rauch
 */
package de.tukl.cs.softech.agilereview.export;

import java.util.Calendar;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReplyDocument.Reply;

/**
 * 
 * @author Malte Brunnlieb (06.11.2013)
 */
public class CommentWrapper {
    
    private String author;
    
    private Calendar creationDate;
    
    private String id;
    
    private Calendar lastModified;
    
    private int priority;
    
    private String recipient;
    
    private Reply[] replies;
    
    private String reviewID;
    
    private int status;
    
    private String text;
    
    /**
     * File path of the comment
     */
    private FileExportWrapper file;
    
    /**
     * Creates a comment wrapper in order to add additional information
     * @param comment {@link Comment}
     * @param file file of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public CommentWrapper(Comment comment, FileExportWrapper file) {
        this.file = file;
        this.author = comment.getAuthor();
        this.creationDate = comment.getCreationDate();
        this.id = comment.getId();
        this.lastModified = comment.getLastModified();
        this.priority = comment.getPriority();
        this.recipient = comment.getRecipient();
        this.replies = comment.getReplies().getReplyArray();
        this.reviewID = comment.getReviewID();
        this.status = comment.getStatus();
        this.text = comment.getText();
    }
    
    /**
     * @return the text of a {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public String getText() {
        return text;
    }
    
    /**
     * @return the status of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * 
     * @return the review id of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public String getReviewID() {
        return reviewID;
    }
    
    /**
     * 
     * @return an array of {@link Reply}s
     * @author Malte Brunnlieb (06.11.2013)
     */
    public Reply[] getReplies() {
        return replies;
    }
    
    /**
     * @return the recipient of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public String getRecipient() {
        return recipient;
    }
    
    /**
     * @return the priority of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * 
     * @return the last modified date of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public Calendar getLastModified() {
        return lastModified;
    }
    
    /**
     * 
     * @return the comment id
     * @author Malte Brunnlieb (06.11.2013)
     */
    public String getId() {
        return id;
    }
    
    /**
     * 
     * @return the creation date of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public Calendar getCreationDate() {
        return creationDate;
    }
    
    /**
     * 
     * @return Returns the author of the {@link Comment}
     * @author Malte Brunnlieb (06.11.2013)
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * @return the filePath
     * @author Malte Brunnlieb (06.11.2013)
     */
    public FileExportWrapper getFile() {
        return file;
    }
    
}
