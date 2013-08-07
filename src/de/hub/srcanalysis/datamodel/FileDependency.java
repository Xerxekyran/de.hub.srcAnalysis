package de.hub.srcanalysis.datamodel;

/**
 * DataObject class for dependencies
 * 
 * @author george
 * 
 */
public class FileDependency {
    private String sourceDependency = "";
    private String targetDependency = "";
    private DependencyType dependecyType = DependencyType.Unknown;
    private Object additionalInformation = null;

    public FileDependency(String sourceDependency, String targetDependency, DependencyType dependecyType, Object additionalInformation) {
	super();
	this.sourceDependency = sourceDependency;
	this.targetDependency = targetDependency;
	this.dependecyType = dependecyType;
	this.additionalInformation = additionalInformation;
    }

    public FileDependency(String sourceDependency, String targetDependency, DependencyType dependecyType) {
	super();
	this.sourceDependency = sourceDependency;
	this.targetDependency = targetDependency;
	this.dependecyType = dependecyType;
    }

    public String getSourceDependency() {
	return sourceDependency;
    }

    public void setSourceDependency(String sourceDependency) {
	this.sourceDependency = sourceDependency;
    }

    public String getTargetDependency() {
	return targetDependency;
    }

    public void setTargetDependency(String targetDependency) {
	this.targetDependency = targetDependency;
    }

    public DependencyType getDependecyType() {
	return dependecyType;
    }

    public void setDependecyType(DependencyType dependecyType) {
	this.dependecyType = dependecyType;
    }

    public Object getAdditionalInformation() {
	return additionalInformation;
    }

    public void setAdditionalInformation(Object additionalInformation) {
	this.additionalInformation = additionalInformation;
    }

    @Override
    public String toString() {
	return "from [" + getSourceDependency() + "] to [" + getTargetDependency() + "] of type [" + getDependecyType() + "]";
    }

    @Override
    public boolean equals(Object arg0) {
	if (arg0 == null || !(arg0 instanceof FileDependency))
	    return false;

	FileDependency fd = (FileDependency) arg0;

	boolean retVal = getDependecyType().equals(fd.getDependecyType()) && getSourceDependency().equals(fd.getSourceDependency())
		&& getTargetDependency().equals(fd.getTargetDependency()) && getAdditionalInformation() == fd.getAdditionalInformation();
	//System.out.println(fd.getSourceDependency() + "-> "+ fd.getTargetDependency() +" equals "+ getSourceDependency() +" -> "+ getTargetDependency() + " == "+retVal );
	return retVal;
    }
}
