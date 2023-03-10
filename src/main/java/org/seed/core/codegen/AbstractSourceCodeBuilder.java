/**
 * Seed
 * Copyright (C) 2021 EUU⛰ROCKS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.seed.core.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seed.C;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public abstract class AbstractSourceCodeBuilder implements SourceCodeBuilder {
	
	private class MemberMetadata {
		
		final TypeClass typeClass;
		
		final String name;
		
		MemberMetadata(String name, TypeClass typeClass) {
			Assert.notNull(name, C.NAME);
			Assert.notNull(typeClass, C.TYPECLASS);
			
			this.typeClass = typeClass;
			this.name = name;
		}
		
	}
	
	protected static final String LF = System.lineSeparator();
	
	protected static final String LFLF = LF + LF;
	
	protected static final String SEPARATOR = ", ";
	
	protected static final String CODE_PLACEHOLDER = LF + "\t\t// put your code here" + LF;
	
	private final ClassMetadata classMetadata;
	
	private final StringBuilder codeBuffer = new StringBuilder();
	
	private final Set<TypeClass> importTypes = new HashSet<>();
	
	private final Set<String> importPackages = new HashSet<>();
	
	private final Map<String, MemberMetadata> memberMap = new HashMap<>();
	
	protected AbstractSourceCodeBuilder(GeneratedObject generatedObject, boolean isAbstract,
										TypeClass superClass, TypeClass[] interfaceClasses, 
										AnnotationMetadata ...annotations) {
		Assert.notNull(generatedObject, "generated object");

		classMetadata = new ClassMetadata(generatedObject, isAbstract, superClass, 
										  interfaceClasses, annotations);
	}
	
	@Override
	public final SourceCode build() {
		return build(BuildMode.COMPLETE);
	}
	
	protected SourceCode build(boolean isGenerated) {
		final StringBuilder buildBuffer = new StringBuilder();
		
		// package
		buildPackage(buildBuffer, classMetadata.packageName);
		
		// imports
		if (isGenerated) {
			addImport(GeneratedCode.class);
		}
		if (classMetadata.annotations != null) {
			for (AnnotationMetadata annotation : classMetadata.annotations) {
				addImport(annotation);
			}
		}
		if (classMetadata.interfaceClasses != null) {
			for (TypeClass interfaceClass : classMetadata.interfaceClasses) {
				addImport(interfaceClass);
			}
		}
		if (classMetadata.superClass != null) {
			addImport(classMetadata.superClass);
		}
		
		final var importPackageList = new ArrayList<String>(importPackages);
		Collections.sort(importPackageList);
		importPackageList.forEach(i -> buildImportPackage(buildBuffer, i));
		
		final var importList = new ArrayList<TypeClass>(importTypes);
		importList.removeIf(type -> importPackages.contains(type.packageName));
		TypeClass.sort(importList);
		importList.forEach(i -> buildImport(buildBuffer, i));
		buildBuffer.append(LF);
		
		// class
		buildClassDefinition(buildBuffer, isGenerated);
		// code
		buildBuffer.append(codeBuffer).append('}').append(LF);
		
		return createSourceCode(buildBuffer.toString());
	}
	
	protected void addImport(Class<?> importClass) {
		Assert.notNull(importClass, "import class");
		
		addImport(new TypeClass(importClass));
	}
	
	protected void addImport(TypeClass typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
		
		if (typeClass.genericClass != null) {
			addImport(typeClass.genericClass);
		}
		if (typeClass.typeClasses != null) {
			for (TypeClass type : typeClass.typeClasses) {
				addImport(type);
			}
		}
		if (!(typeClass.packageName.startsWith("java.lang") ||
			  typeClass.packageName.equals(classMetadata.packageName))) {
			importTypes.add(typeClass);
		}
	}
	
	protected void addImport(AnnotationMetadata annotation) {
		Assert.notNull(annotation, C.ANNOTATION);
		
		addImport(annotation.annotationClass);
		if (annotation.hasParameters()) {
			for (Object value : annotation.parameterMap.values()) {
				if (value instanceof AnnotationMetadata[]) {
					final var paramAnnotations = (AnnotationMetadata[]) value;
					for (AnnotationMetadata paramAnnotation : paramAnnotations) {
						addImport(paramAnnotation);
					}
				}
				else {
					addImport(value.getClass());
				}
			}
		}
	}
	
	protected void addImportPackage(String packageName) {
		Assert.notNull(packageName, "package name");
		
		importPackages.add(packageName);
	}

	protected void addMember(String name, TypeClass typeClass, AnnotationMetadata ...annotations) {
		Assert.notNull(typeClass, C.TYPECLASS);
		Assert.notNull(name, C.NAME);
		Assert.state(!memberMap.containsKey(name), "duplicate member definition for: " + name);
		
		addImport(typeClass);
		if (annotations != null) {
			for (AnnotationMetadata annotation : annotations) {
				addImport(annotation);
			}
		}
		final var member = new MemberMetadata(name, typeClass);
		memberMap.put(name, member);
		buildMember(codeBuffer, member, annotations);
	}
	
	protected void addGetterAndSetter(String memberName, AnnotationMetadata ...annotations) {
		addGetter(memberName, annotations);
		addSetter(memberName);
	}
	
	protected void addGetter(String memberName, AnnotationMetadata ...annotations) {
		if (annotations != null) {
			for (AnnotationMetadata annotation : annotations) {
				addImport(annotation);
			}
		}
		buildGetter(codeBuffer, getMember(memberName), annotations);
	}
	
	protected void addSetter(String memberName) {
		buildSetter(codeBuffer, getMember(memberName));
	}
	
	protected void addMethod(TypeClass returnType, String methodName, 
							 ParameterMetadata[] parameters, 
							 String content, AnnotationMetadata ...annotations) {
		Assert.notNull(methodName, "method name");
		Assert.notNull(content, C.CONTENT);
		
		// annotations
		if (annotations != null) {
			for (AnnotationMetadata annotation : annotations) {
				addImport(annotation);
				codeBuffer.append('\t');
				buildAnnotation(codeBuffer, annotation);
				codeBuffer.append(LF);
			}
		}
		codeBuffer.append("\tpublic ");
		
		// return type
		if (returnType != null) {
			addImport(returnType);
			buildTypeClass(codeBuffer, returnType);
		}
		else {
			codeBuffer.append("void");
		}
		
		// parameters
		codeBuffer.append(' ').append(methodName).append('(');
		if (parameters != null) {
			boolean first = true;
			for (ParameterMetadata parameter : parameters) {
				addImport(parameter.typeClass);
				if (first) {
					first = false;
				}
				else {
					codeBuffer.append(SEPARATOR);
				}
				if (parameter.annotation != null) {
					addImport(parameter.annotation);
					buildAnnotation(codeBuffer, parameter.annotation);
					codeBuffer.append(' ');
				}
				codeBuffer.append(parameter.typeClass.className).append(' ').append(parameter.name);
			}
		}
		
		// content
		codeBuffer.append(") {").append(LF)
				  .append("\t\t").append(content)
				  .append("\t}").append(LFLF);
 	}
	
	protected void addAnnotation(AnnotationMetadata annotation) {
		Assert.notNull(annotation, C.ANNOTATION);
		
		addImport(annotation);
		codeBuffer.append('\t');
		buildAnnotation(codeBuffer, annotation);
		codeBuffer.append(LF);
	}
	
	protected void addCode(String code) {
		Assert.notNull(code, C.CODE);
		
		codeBuffer.append(code).append(LF);
	}
	
	protected SourceCode createSourceCode(String content) {
		return new SourceCodeImpl(classMetadata, content);
	}
	
	private MemberMetadata getMember(String memberName) {
		Assert.notNull(memberName, "member name");
		Assert.state(memberMap.containsKey(memberName), "member not exist: " + memberName);
		
		return memberMap.get(memberName);
	}
	
	private void buildClassDefinition(StringBuilder buf, boolean isGenerated) {
		// annotations
		if (classMetadata.annotations != null) {
			for (AnnotationMetadata annotation : classMetadata.annotations) {
				buildAnnotation(buf, annotation);
				buf.append(LF);
			}
		}
		
		// class
		buf.append("public");
		if (classMetadata.isAbstract) {
			buf.append(" abstract");
		}
		buf.append(" class ").append(classMetadata.className);
		
		// super class
		if (classMetadata.superClass != null) {
			buf.append(" extends ");
			buildTypeClass(buf, classMetadata.superClass);
		}
		
		// interfaces
		if (isGenerated || classMetadata.interfaceClasses != null) {
			buf.append(" implements ");
			buildClassInterfaces(buf, isGenerated);
		}
		buf.append(" {").append(LFLF);
	}
	
	private void buildClassInterfaces(StringBuilder buf, boolean isGenerated) {
		boolean first = true;
		if (isGenerated) {
			buf.append(GeneratedCode.class.getSimpleName());
			first = false;
		}
		if (classMetadata.interfaceClasses != null) {
			for (TypeClass interfaceClass : classMetadata.interfaceClasses) {
				if (!first) {
					buf.append(SEPARATOR);
				}
				buildTypeClass(buf, interfaceClass);
				first = false;
			}
		}
	}
	
	private static void buildPackage(StringBuilder buf, String packageName) {
		buf.append("package ").append(packageName).append(';').append(LFLF);
	}
	
	private static void buildMember(StringBuilder buf, MemberMetadata member, AnnotationMetadata ...annotations) {
		if (annotations != null) {
			for (AnnotationMetadata annotation : annotations) {
				buf.append('\t');
				buildAnnotation(buf, annotation);
				buf.append(LF);
			}
		}
		buf.append("\tprivate ");
		buildTypeClass(buf, member.typeClass);
		buf.append(' ').append(member.name).append(';').append(LFLF);
	}
	
	private static void buildGetter(StringBuilder buf, MemberMetadata member, AnnotationMetadata ...annotations) {
		if (annotations != null) {
			for (AnnotationMetadata annotation : annotations) {
				buf.append('\t');
				buildAnnotation(buf, annotation);
				buf.append(LF);
			}
		}
		buf.append("\tpublic ");
		buildTypeClass(buf, member.typeClass);
	    buf.append(" get").append(StringUtils.capitalize(member.name)).append("() {").append(LF)
			.append("\t\treturn ").append(member.name).append(';').append(LF)
			.append("\t}").append(LFLF);
	}
	
	private static void buildSetter(StringBuilder buf, MemberMetadata member) {
		buf.append("\tpublic void set").append(StringUtils.capitalize(member.name)).append('(');
		buildTypeClass(buf, member.typeClass);
		buf.append(' ').append(member.name).append(") {").append(LF)
			.append("\t\tthis.").append(member.name).append(" = ").append(member.name).append(';').append(LF)
			.append("\t}").append(LFLF);
	}
	
	private static void buildAnnotation(StringBuilder buf, AnnotationMetadata annotation) {
		buf.append('@').append(annotation.annotationClass.getSimpleName());
		if (annotation.hasParameters()) {
			buf.append('(');
			boolean first = true;
			for (Entry<String, Object> entry : annotation.parameterMap.entrySet()) {
				if (first) {
					first = false;
				}
				else {
					buf.append(SEPARATOR);
				}
				buf.append(entry.getKey()).append(" = ");
				
				if (entry.getValue() instanceof AnnotationMetadata[]) {
					buildAnnotations(buf, (AnnotationMetadata[]) entry.getValue());
					continue;
				}
				if (entry.getValue() instanceof Enum) {
					buf.append(entry.getValue().getClass().getSimpleName()).append('.');
				}
				buf.append(entry.getValue());
			}
			buf.append(')');
		}
		else if (annotation.singleValue != null) {
			buf.append("(\"").append(annotation.singleValue).append("\")");
		}
	}
	
	private static void buildAnnotations(StringBuilder buf, AnnotationMetadata[] annotations) {
		boolean first = true;
		buf.append('{');
		for (AnnotationMetadata annotation : annotations) {
			if (first) {
				first = false;
			}
			else {
				buf.append(SEPARATOR);
			}
			buildAnnotation(buf, annotation);
		}
		buf.append('}');
	}
	
	private static void buildTypeClass(StringBuilder buf, TypeClass typeClass) {
		if (typeClass.genericClass != null) {
			buf.append(typeClass.genericClass.getSimpleName()).append('<');
		}
		buf.append(typeClass.className);
		if (!ObjectUtils.isEmpty(typeClass.typeClasses)) {
			buf.append('<');
			for (int i = 0; i < typeClass.typeClasses.length; i++) {
				if (i > 0) {
					buf.append(SEPARATOR);
				}
				buildTypeClass(buf, typeClass.typeClasses[i]);
			}
			buf.append('>');
		}
		if (typeClass.genericClass != null) {
			buf.append('>');
		}
	}
	
	private static void buildImport(StringBuilder buf, TypeClass typeClass) {
		buf.append("import ").append(typeClass.getQualifiedName()).append(';').append(LF);
	}
	
	private static void buildImportPackage(StringBuilder buf, String packageName) {
		buf.append("import ").append(packageName).append(".*;").append(LF);
	}
	
	protected static AnnotationMetadata newAnnotation(Class<?> annotationClass) {
		return new AnnotationMetadata(annotationClass);
	}
	
	protected static AnnotationMetadata newAnnotation(Class<?> annotationClass, String singleValue) {
		return new AnnotationMetadata(annotationClass, singleValue);
	}
	
	protected static AnnotationMetadata newAnnotation(Class<?> annotationClass, String name, Object value) {
		return new AnnotationMetadata(annotationClass, name, value);
	}
	
	protected static AnnotationMetadata newAnnotation(Class<?> annotationClass, Map<String, Object> parameterMap) {
		return new AnnotationMetadata(annotationClass, parameterMap);
	}
	
	protected static ParameterMetadata newParameter(String name, TypeClass typeClass) {
		return new ParameterMetadata(name, typeClass);
	}
	
	protected static ParameterMetadata newParameter(String name, TypeClass typeClass, AnnotationMetadata annotation) {
		return new ParameterMetadata(name, typeClass, annotation);
	}
	
	protected static TypeClass newTypeClass(Class<?> clas, TypeClass ...typeClasses) {
		return new TypeClass(clas, typeClasses);
	}
	
	protected static TypeClass newTypeClass(String qualifiedName) {
		return new TypeClass(qualifiedName);
	}
	
	protected static TypeClass newTypeClass(GeneratedObject generatedObject) {
		return new TypeClass(generatedObject.getGeneratedPackage(), 
							 generatedObject.getGeneratedClass());
	}
	
	protected static TypeClass newTypeClass(GeneratedObject generatedObject, Class<?> genericClass) {
		return new TypeClass(generatedObject.getGeneratedPackage(), 
							 generatedObject.getGeneratedClass(),
							 genericClass);
	}
	
	protected static String quote(String string) {
		return string != null ? '\"' + string + '\"' : null;
	}
	
}
