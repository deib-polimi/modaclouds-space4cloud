package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;

public class RamConstraint extends ArchitecturalConstraint {

	NumericalRange range = new NumericalRange();
	DataHandler dataHandler = DataHandlerFactory.getHandler();

	public RamConstraint(String id, Metric metric, int priority,
			Unit unit) {
		super(id, metric, priority, unit);
		// TODO Auto-generated constructor stub
	}

	public void setMin(double minValue) {
		if(unit.equals(Unit.MEGABYTES))			
			range.setMinValue(minValue);
		else if (unit.equals(Unit.GIGABYTES)){
			setUnit(Unit.MEGABYTES);
			range.setMinValue(minValue * Unit.GB2MB);
		}			
	}

	@Override
	public double checkConstraintDistance(Object measurement) {
		return range.getMinValue() - ((Double) measurement); 

	}

	public double getMin() {
		return range.getMinValue();
	}

	@Override
	public boolean checkConstraint(CloudService resource) {
		if(resource instanceof Compute && range.getMinValue() < ((Compute)resource).getRam())
				return true;
		return false;
	}

}
