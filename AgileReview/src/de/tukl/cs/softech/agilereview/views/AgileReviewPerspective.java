package de.tukl.cs.softech.agilereview.views;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 *  This class is meant to serve as an example for how various contributions 
 *  are made to a perspective. Note that some of the extension point id's are
 *  referred to as API constants while others are hardcoded and may be subject 
 *  to change. 
 */
public class AgileReviewPerspective implements IPerspectiveFactory {

	/**
	 * creates a new instance of AgileReviewPerspective
	 */
	public AgileReviewPerspective() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout factory) {
		// Create layout
//		IFolderLayout topLeft =	factory.createFolder("left", IPageLayout.LEFT, 0.2f, factory.getEditorArea());
//		topLeft.addView("de.tukl.cs.softech.agilereview.view.reviewnavigator.view");
//		topLeft.addView("org.eclipse.jdt.ui.PackageExplorer");
//		
//		factory.addView("de.tukl.cs.softech.agilereview.view.commenttableview.view",
//				IPageLayout.BOTTOM,	0.7f, factory.getEditorArea());
//		
//		factory.addView("de.tukl.cs.softech.agilereview.view.commentdetailview.view",
//				IPageLayout.RIGHT, 0.75f,	factory.getEditorArea());
		
		// Contributing to Alt+Shift+N
//		factory.addNewWizardShortcut("de.tukl.cs.softech.agilereview.newReviewWizard");
//	
//		// Contributing to Window->Show View (will be alphabetically)
//		factory.addShowViewShortcut("org.eclipse.jdt.ui.PackageExplorer");
//		factory.addShowViewShortcut("org.eclipse.ui.views.ContentOutline");
//		factory.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");
//		factory.addShowViewShortcut("org.eclipse.ui.views.ProblemView");
		
		// Contributing to Window->Open Perspective (will be alphabetically)
//		factory.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective");
//		factory.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective");
		
	}
}
