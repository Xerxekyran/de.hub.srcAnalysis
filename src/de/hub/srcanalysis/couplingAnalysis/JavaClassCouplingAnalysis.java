package de.hub.srcanalysis.couplingAnalysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gmt.modisco.java.AbstractMethodDeclaration;
import org.eclipse.gmt.modisco.java.AbstractTypeDeclaration;
import org.eclipse.gmt.modisco.java.ArrayType;
import org.eclipse.gmt.modisco.java.Block;
import org.eclipse.gmt.modisco.java.BodyDeclaration;
import org.eclipse.gmt.modisco.java.ClassDeclaration;
import org.eclipse.gmt.modisco.java.CompilationUnit;
import org.eclipse.gmt.modisco.java.ConstructorDeclaration;
import org.eclipse.gmt.modisco.java.EnumDeclaration;
import org.eclipse.gmt.modisco.java.Expression;
import org.eclipse.gmt.modisco.java.ExpressionStatement;
import org.eclipse.gmt.modisco.java.FieldDeclaration;
import org.eclipse.gmt.modisco.java.ImportDeclaration;
import org.eclipse.gmt.modisco.java.InterfaceDeclaration;
import org.eclipse.gmt.modisco.java.MethodDeclaration;
import org.eclipse.gmt.modisco.java.MethodInvocation;
import org.eclipse.gmt.modisco.java.NamedElement;
import org.eclipse.gmt.modisco.java.PrimitiveType;
import org.eclipse.gmt.modisco.java.ReturnStatement;
import org.eclipse.gmt.modisco.java.SingleVariableDeclaration;
import org.eclipse.gmt.modisco.java.Statement;
import org.eclipse.gmt.modisco.java.Type;
import org.eclipse.gmt.modisco.java.VariableDeclaration;

import de.hub.srcanalysis.datamodel.DependencyType;
import de.hub.srcanalysis.datamodel.FileDependency;

public class JavaClassCouplingAnalysis implements CouplingAnalysis{

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
    public boolean analyzeMethodDeclarations = true;
    public boolean analyzeMethodBodies = true;
    public boolean analyzeFields = true;
    public boolean analyzeConstructors = true;
    
    public boolean ignoreStandardClasses = true;
    public boolean skipSrcGenDependencies = true;

    private String currentFilePath = "";

    /**
     * 
     * @param map
     */
    public TreeMap<String, ArrayList<FileDependency>> calculateCouplings(HashMap<String, CompilationUnit> map) {
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
			} else if (bodyDecl instanceof MethodDeclaration) {
			    if (analyzeMethodDeclarations) {
				analyzeMethodDeclaration((MethodDeclaration) bodyDecl);
			    }
			    if (analyzeMethodBodies) {
				analyzeMethodBody((MethodDeclaration) bodyDecl);
			    }
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
			} else if (analyzeMethodDeclarations && bodyDecl instanceof MethodDeclaration) {
			    analyzeMethodDeclaration((MethodDeclaration) bodyDecl);
			}
		    }
		} else {
		    // System.out.println("WARN: Not yet handled typeDeclaration (" + abstractTypeDeclaration + ")");
		}
	    }

	}

	return fileBasedCouplings;
	
	/*
	 * System.out.println("--------- RESULT: ---------------------");
	 * Iterator<String> couplingKeyIt =
	 * fileBasedCouplings.keySet().iterator(); while
	 * (couplingKeyIt.hasNext()) { String couplingKey =
	 * couplingKeyIt.next(); ArrayList<FileDependency> arrayList =
	 * fileBasedCouplings.get(couplingKey); System.out.println(couplingKey +
	 * " --> "); Iterator<FileDependency> iterator = arrayList.iterator();
	 * while(iterator.hasNext()) { System.out.println(iterator.next()); } }
	 */
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
     * @param depType
     */
    private void analyzeType(Type type, DependencyType depType) {
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
		addDependency(currentFilePath, type.getName(), depType);
	    }
	} else if (type instanceof EnumDeclaration) {
	    // EnumDeclaration
	    CompilationUnit originalCompilationUnit = ((EnumDeclaration) type).getOriginalCompilationUnit();
	    if (!analyzeCompilationUnit(originalCompilationUnit)) {
		// System.out.println("WARN: Found an enum type with no compilation unit ("
		// + type.getName() + ")");
		addDependency(currentFilePath, type.getName(), depType);
	    }
	} else {
	    // System.out.println("WARN: Not yet analyzed type of Type (" + type + ")");
	}
    }

    /**
     * 
     * @param type
     */
    private void analyzeType(Type type) {
	analyzeType(type, DependencyType.Unknown);
    }

    /**
     * 
     * @param compilationUnit
     */
    private boolean analyzeCompilationUnit(CompilationUnit compilationUnit, DependencyType depType) {
	if (compilationUnit == null)
	    return false;

	String originalFilePath = compilationUnit.getOriginalFilePath();
	if (originalFilePath != null && originalFilePath != "") {
	    addDependency(currentFilePath, originalFilePath, depType);
	}
	return true;
    }

    /**
     * 
     * @param compilationUnit
     * @return
     */
    private boolean analyzeCompilationUnit(CompilationUnit compilationUnit) {
	return analyzeCompilationUnit(compilationUnit, DependencyType.Unknown);
    }

    /**
     * 
     * @param method
     */
    private void analyzeMethodDeclaration(MethodDeclaration method) {
	Iterator<SingleVariableDeclaration> iterator = method.getParameters().iterator();
	while (iterator.hasNext()) {
	    SingleVariableDeclaration next = iterator.next();
	    Type type = next.getType().getType();
	    analyzeType(type);
	}
    }

    /**
     * 
     * @param method
     */
    private void analyzeMethodBody(MethodDeclaration method) {

	Block body = method.getBody();
	if (body != null) {
	    EList<Statement> statements = body.getStatements();
	    if (statements != null) {
		// browse through all statements
		Iterator<Statement> it = statements.iterator();
		while (it.hasNext()) {
		    Statement st = it.next();

		    // An expression
		    if (st instanceof ExpressionStatement) {
			ExpressionStatement ex = (ExpressionStatement) st;
			Expression expression = ex.getExpression();
			if (expression != null) {

			    // Method invocation
			    if (expression instanceof MethodInvocation) {
				// look if we can get the called method and the
				// compilation unit of the class of that method
				AbstractMethodDeclaration invokedMethod = ((MethodInvocation) expression).getMethod();
				if (invokedMethod != null) {
				    CompilationUnit originalCompilationUnit = invokedMethod.getOriginalCompilationUnit();
				    if (!analyzeCompilationUnit(originalCompilationUnit, DependencyType.FunctionCall)) {
					// method invocation call into a class
					// that we do not have the compilation
					// unit from, try to get the
					// abstgractTypeDeclaration instead
					AbstractTypeDeclaration abstractTypeDeclaration = invokedMethod.getAbstractTypeDeclaration();
					if (abstractTypeDeclaration != null) {
					    analyzeType(abstractTypeDeclaration, DependencyType.FunctionCall);
					}
				    }
				} else {
				    System.out.println("WARN: Invoked Method is null");
				}
			    }
			}
			// System.out.println("Expression: " + expression);
		    } else if (st instanceof ReturnStatement) {

		    } else if (st instanceof VariableDeclaration) {

		    }
		}
	    }
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

	if (!analyzeCompilationUnit(originalCompilationUnit, DependencyType.Import)) {
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
	if (skipSrcGenDependencies) {
	    if (from.contains("src-gen") || to.getTargetDependency().contains("src-gen") || from.contains("gen-src")
		    || to.getTargetDependency().contains("gen-src")) {
		return false;
	    }
	}
	
	if(ignoreStandardClasses) {
	    if(!from.contains("\\") || !to.getTargetDependency().contains("\\")) {
		return false;
	    }
	}

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
