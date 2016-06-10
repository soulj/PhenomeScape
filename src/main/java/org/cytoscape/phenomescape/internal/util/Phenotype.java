package org.cytoscape.phenomescape.internal.util;

public class Phenotype {
	

private String ID;
private String Name;
private Boolean Selected = Boolean.FALSE;




@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((ID == null) ? 0 : ID.hashCode());
	return result;
}


@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	Phenotype other = (Phenotype) obj;
	if (ID == null) {
		if (other.ID != null)
			return false;
	} else if (!ID.equals(other.ID))
		return false;
	return true;
}


Phenotype(String iD, String name) {
	this.setID(iD);
	this.setName(name);
}


public String getID() {
	return ID;
}
public void setID(String iD) {
	ID = iD;
}
public String getName() {
	return Name;
}
public void setName(String name) {
	Name = name;
}

public Boolean getSelected() {
	return Selected;
}

public void setSelected(Boolean selected) {
	Selected = selected;
}
	


}
