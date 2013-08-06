package de.hub.srcanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gmt.modisco.java.AbstractTypeDeclaration;
import org.eclipse.gmt.modisco.java.ArrayType;
import org.eclipse.gmt.modisco.java.BodyDeclaration;
import org.eclipse.gmt.modisco.java.ClassDeclaration;
import org.eclipse.gmt.modisco.java.CompilationUnit;
import org.eclipse.gmt.modisco.java.ConstructorDeclaration;
import org.eclipse.gmt.modisco.java.EnumDeclaration;
import org.eclipse.gmt.modisco.java.FieldDeclaration;
import org.eclipse.gmt.modisco.java.ImportDeclaration;
import org.eclipse.gmt.modisco.java.InterfaceDeclaration;
import org.eclipse.gmt.modisco.java.MethodDeclaration;
import org.eclipse.gmt.modisco.java.NamedElement;
import org.eclipse.gmt.modisco.java.PrimitiveType;
import org.eclipse.gmt.modisco.java.SingleVariableDeclaration;
import org.eclipse.gmt.modisco.java.Type;

import de.hub.srcanalysis.datamodel.DependencyType;
import de.hub.srcanalysis.datamodel.FileDependency;

public class JavaClassCouplingAnalysis {

    private class FileBasedOrderComparator implements Comparator<String> {
	@Override
	public int compare(String o1, String o2) {
	    return o1.compareTo(o2);
	}

    }

    private TreeMap<String, ArrayList<FileDependency>> fileBasedCouplings = new TreeMap<String, ArrayList<FileDependency>>(
	    new FileBasedOrderComparator());

    /**
     * Configuration variables to set the granularity
     */
    public boolean analyzeImports = true;
    public boolean analyzeMethods = true;
    public boolean analyzeFields = true;
    public boolean analyzeConstructors = true;

    private String currentFilePath = "";

    /**
     * 
     * @return
     */
    public TreeMap<String, ArrayList<FileDependency>> getCouplings() {
	return fileBasedCouplings;
    }

    /**
     * 
     * @param map
     */
    public void calculateCouplings(HashMap<String, CompilationUnit> map) {
	Iterator<CompilationUnit> valueIt = map.values().iterator();
	while (valueIt.hasNext()) {
	    CompilationUnit next = valueIt.next();

	    currentFilePath = next.getOriginalFilePath();

	    EList<AbstractTypeDeclaration> types = next.getTypes();

	    if (types.size() > 0) {
		AbstractTypeDeclaration abstractTypeDeclaration = types.get(0);

		// Classes
		if (abstractTypeDeclaration instanceof ClassDeclaration) {
		    ClassDeclaration classDeclaration = (ClassDeclaration) abstractTypeDeclaration;

		    if (analyzeImports) {
			// check for types in imports
			Iterator<ImportDeclaration> importIt = next.getImports().iterator();

			while (importIt.hasNext()) {
			    analyzeImport(importIt.next());
			}
		    }

		    // check for types in the whole body of the classDeclaration
		    Iterator<BodyDeclaration> bodyIt = classDeclaration.getBodyDeclarations().iterator();
		    while (bodyIt.hasNext()) {
			BodyDeclaration bodyDecl = bodyIt.next();
			if (analyzeFields && bodyDecl instanceof FieldDeclaration) {
			    analyzeFields((FieldDeclaration) bodyDecl);
			} else if (analyzeConstructors && bodyDecl instanceof ConstructorDeclaration) {
			    analyzeConstructor((ConstructorDeclaration) bodyDecl);
			} else if (analyzeMethods && bodyDecl instanceof MethodDeclaration) {
			    analyzeMethod((MethodDeclaration) bodyDecl);
			}
		    }
		    // Interfaces
		} else if (abstractTypeDeclaration instanceof InterfaceDeclaration) {
		    InterfaceDeclaration interfaceDecl = (InterfaceDeclaration) abstractTypeDeclaration;

		    // check for types in the whole body of the classDeclaration
		    Iterator<BodyDeclaration> bodyIt = interfaceDecl.getBodyDeclarations().iterator();
		    while (bodyIt.hasNext()) {
			BodyDeclaration bodyDecl = bodyIt.next();
			if (analyzeFields && bodyDecl instanceof FieldDeclaration) {
			    analyzeFields((FieldDeclaration) bodyDecl);
			} else if (analyzeConstructors && bodyDecl instanceof ConstructorDeclaration) {
			    analyzeConstructor((ConstructorDeclaration) bodyDecl);
			} else if (analyzeMethods && bodyDecl instanceof MethodDeclaration) {
			    analyzeMethod((MethodDeclaration) bodyDecl);
			}
		    }
		} else {
		    System.out.println("WARN: Not yet handled typeDeclaration (" + abstractTypeDeclaration + ")");
		}
	    }

	}

	System.out.println("--------- RESULT: ---------------------");
	Iterator<String> couplingKeyIt = fileBasedCouplings.keySet().iterator();
	while (couplingKeyIt.hasNext()) {
	    String couplingKey = couplingKeyIt.next();
	    System.out.println(couplingKey + " --> " + fileBasedCouplings.get(couplingKey));
	}
    }

    /**
     * 
     * @param field
     */
    private void analyzeFields(FieldDeclaration field) {
	// System.out.println("Field: " + field.getName() + " : " +
	// field.getType().toString());
    }

    /**
     * 
     * @param type
     */
    private void analyzeType(Type type) {

	if (type instanceof ClassDeclaration) {
	    // ClassDeclaration
	    CompilationUnit originalCompilationUnit = type.getOriginalCompilationUnit();
	    if (!analyzeCompilationUnit(originalCompilationUnit)) {
		// System.out.println("WARN: Found a type with no compilation unit ("
		// + type.getName() + ")");
		addDependency(currentFilePath, type.getName());
	    }

	} else if (type instanceof PrimitiveType) {
	    // PrimitiveType
	    // we do not add dependencies to primitive types
	} else if (type instanceof ArrayType) {
	    // ArrayType
	    analyzeType(((ArrayType) type).getElementType().getType());
	} else if (type instanceof InterfaceDeclaration) {
	    // InterfaceDeclaration
	    CompilationUnit originalCompilationUnit = ((InterfaceDeclaration) type).getOriginalCompilationUnit();
	    if (!analyzeCompilationUnit(originalCompilationUnit)) {
		// System.out.println("WARN: Found an interface type with no compilation unit ("
		// + type.getName() + ")");
		addDependency(currentFilePath, type.getName());
	    }
	} else if (type instanceof EnumDeclaration) {
	    // EnumDeclaration
	    CompilationUnit originalCompilationUnit = ((EnumDeclaration) type).getOriginalCompilationUnit();
	    if (!analyzeCompilationUnit(originalCompilationUnit)) {
		// System.out.println("WARN: Found an enum type with no compilation unit ("
		// + type.getName() + ")");
		addDependency(currentFilePath, type.getName());
	    }
	} else {
	    System.out.println("WARN: Not yet analyzed type of Type (" + type + ")");
	}
    }

    /**
     * 
     * @param compilationUnit
     */
    private boolean analyzeCompilationUnit(CompilationUnit compilationUnit) {
	if (compilationUnit == null)
	    return false;

	String originalFilePath = compilationUnit.getOriginalFilePath();
	if (originalFilePath != null && originalFilePath != "") {
	    addDependency(currentFilePath, originalFilePath);
	}
	return true;
    }

    /**
     * 
     * @param method
     */
    private void analyzeMethod(MethodDeclaration method) {
	Iterator<SingleVariableDeclaration> iterator = method.getParameters().iterator();
	while (iterator.hasNext()) {
	    SingleVariableDeclaration next = iterator.next();
	    Type type = next.getType().getType();
	    analyzeType(type);
	}
    }

    /**
     * 
     * @param constr
     */
    private void analyzeConstructor(ConstructorDeclaration constr) {
	// System.out.println("Constructor: " + constr.getName() + " : " +
	// constr.getTypeParameters());
    }

    /**
     * 
     * @param importDecl
     */
    private void analyzeImport(ImportDeclaration importDecl) {
	NamedElement importedElement = importDecl.getImportedElement();
	CompilationUnit originalCompilationUnit = importedElement.getOriginalCompilationUnit();

	if (!analyzeCompilationUnit(originalCompilationUnit)) {
	    // System.out.println("WARN: found an import element with no compilation unit ("
	    // + importedElement.getName() + ")");
	    addDependency(currentFilePath, importedElement.getName(), DependencyType.Import);
	}
    }

    /**
     * Adds the dependency entry for the given string ids
     * 
     * @param from
     *            id for the dependency source
     * @param to
     *            id for the dependency target
     * @return returns true if the dependency was added successfully, false if
     *         the dependency already exists
     */
    private boolean addDependency(String from, FileDependency to) {
	boolean retVal = false;

	ArrayList<FileDependency> deps = fileBasedCouplings.get(from);
	if (deps == null) {
	    deps = new ArrayList<FileDependency>();
	    deps.add(to);
	    fileBasedCouplings.put(from, deps);
	    retVal = true;
	} else if (deps.contains(to)) {
	    // dependency was already added
	    retVal = false;
	} else {
	    deps.add(to);
	    fileBasedCouplings.put(from, deps);
	    retVal = true;
	}

	// check if the "to"-dependency is also listed as key in the dependency
	// map
	if (fileBasedCouplings.get(to.getTargetDependency()) == null) {
	    fileBasedCouplings.put(to.getTargetDependency(), new ArrayList<FileDependency>());
	}

	return retVal;
    }

    /**
     * 
     * @param from
     * @param to
     * @return
     */
    private boolean addDependency(String from, String to) {
	FileDependency fd = new FileDependency(from, to, DependencyType.Unknown);
	return addDependency(from, fd);
    }

    /**
     * 
     * @param from
     * @param to
     * @param type
     * @return
     */
    private boolean addDependency(String from, String to, DependencyType type) {
	FileDependency fd = new FileDependency(from, to, type);
	return addDependency(from, fd);
    }
}
