package de.tukl.cs.softech.agilereview.control.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.Configuration;
import net.sf.jxls.transformer.XLSTransformer;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;

public class XSLExport {

	public static void exportReviews(List<Review> reviews) {
		Map<String, Object> beans = new HashMap<String, Object>();
		beans.put("reviews", reviews);
        Configuration config = new Configuration();
        XLSTransformer transformer = new XLSTransformer( config );
        try {
			transformer.transformXLS("E:\\report_template.xlsx", beans, "E:\\review_report.xlsx");
		} catch (ParsePropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void exportComments(List<Comment> comments) {
		Map<String, Object> beans = new HashMap<String, Object>();
		beans.put("comments", comments);
        Configuration config = new Configuration();
        XLSTransformer transformer = new XLSTransformer( config );
        try {
			transformer.transformXLS("E:\\report_template.xlsx", beans, "E:\\review_report.xlsx");
		} catch (ParsePropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
