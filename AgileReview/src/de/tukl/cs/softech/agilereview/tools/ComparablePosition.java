package de.tukl.cs.softech.agilereview.tools;

import org.eclipse.jface.text.Position;

public class ComparablePosition extends Position implements Comparable {

	public ComparablePosition(Position p) {
		super(p.offset, p.length);
	}
	
	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof ComparablePosition) {
			if(offset < ((ComparablePosition)arg0).offset){
				return -1;
			} else if(offset > ((ComparablePosition)arg0).offset) {
				return 1;
			}
		}
		return 0;
	}
}
