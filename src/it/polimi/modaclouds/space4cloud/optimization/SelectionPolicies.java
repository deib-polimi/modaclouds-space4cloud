package it.polimi.modaclouds.space4cloud.optimization;

public enum SelectionPolicies {
	RANDOM("random"), FIRST("first"), LONGEST("longest"), UTILIZATION(
			"utilization");

	public static SelectionPolicies getPropertyFromName(String name) {
		if (name.equals(RANDOM.toString()))
			return RANDOM;
		else if (name.equals(FIRST.toString()))
			return FIRST;
		else if (name.equals(LONGEST.toString()))
			return LONGEST;
		else if (name.equals(UTILIZATION.toString()))
			return UTILIZATION;
		return null;
	}

	private final String name;

	private SelectionPolicies(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return (otherName == null) ? false : name.equals(otherName);
	}

	@Override
	public String toString() {
		return name;
	}
}
