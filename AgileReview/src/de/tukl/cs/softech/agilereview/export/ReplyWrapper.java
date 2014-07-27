/**
 * Copyright (c) 2011, 2012 AgileReview Development Team and others.
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License - v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Malte Brunnlieb, Philipp Diebold, Peter Reuter, Thilo Rauch
 */
package de.tukl.cs.softech.agilereview.export;

import java.util.Calendar;

import org.apache.xmlbeans.XmlCursor;

import agileReview.softech.tukl.de.ReplyDocument.Reply;

/**
 * 
 * @author Thilo Rauch (20.07.2014)
 */
public class ReplyWrapper {
    
    private String author;
    private Calendar creationDate;
    private String text;
    private CommentWrapper comment;
    
    public ReplyWrapper(Reply reply, CommentWrapper c) {
        this.author = reply.getAuthor();
        this.creationDate = reply.getCreationDate();
        XmlCursor cursor = reply.newCursor();
        this.text = cursor.getTextValue().trim();
        // TODO: Make this null save
        this.comment = c;
        cursor.dispose();
    }
    
    public String getAuthor() {
        return author;
    }
    
    public Calendar getCreationDate() {
        return creationDate;
    }
    
    public String getText() {
        return text;
    }
    
    public CommentWrapper getComment() {
        return comment;
    }
}
