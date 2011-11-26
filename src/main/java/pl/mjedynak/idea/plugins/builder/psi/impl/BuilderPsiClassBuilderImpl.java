package pl.mjedynak.idea.plugins.builder.psi.impl;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;
import pl.mjedynak.idea.plugins.builder.psi.BuilderPsiClassBuilder;
import pl.mjedynak.idea.plugins.builder.psi.PsiHelper;

import java.util.List;

public class BuilderPsiClassBuilderImpl implements BuilderPsiClassBuilder {

    private static final String PRIVATE_STRING = "private";
    private static final String SPACE = " ";

    private PsiHelper psiHelper;

    private Project project;
    private PsiDirectory targetDirectory;
    private PsiClass srcClass;
    private String builderClassName;
    private List<PsiElementClassMember> psiElementClassMembers;

    private PsiClass builderClass;
    private PsiElementFactory elementFactory;
    private String srcClassName;
    private String srcClassFieldName;

    public BuilderPsiClassBuilderImpl(PsiHelper psiHelper) {
        this.psiHelper = psiHelper;
    }

    @Override
    public BuilderPsiClassBuilder aBuilder(Project project, PsiDirectory targetDirectory, PsiClass psiClass, String builderClassName, List<PsiElementClassMember> psiElementClassMembers) {
        this.project = project;
        this.targetDirectory = targetDirectory;
        this.srcClass = psiClass;
        this.builderClassName = builderClassName;
        this.psiElementClassMembers = psiElementClassMembers;
        JavaDirectoryService javaDirectoryService = psiHelper.getJavaDirectoryService();
        builderClass = javaDirectoryService.createClass(targetDirectory, builderClassName);
        JavaPsiFacade javaPsiFacade = psiHelper.getJavaPsiFacade(project);
        elementFactory = javaPsiFacade.getElementFactory();
        srcClassName = psiClass.getName();
        srcClassFieldName = StringUtils.uncapitalize(srcClassName);
        return this;
    }

    @Override
    public BuilderPsiClassBuilder withFields() {
        checkFields();
        PsiField srcClassNameField = elementFactory.createFieldFromText(PRIVATE_STRING + SPACE + srcClassName + " " + srcClassFieldName + ";", srcClass);
        builderClass.add(srcClassNameField);
        for (PsiElementClassMember classMember : psiElementClassMembers) {
            builderClass.add(classMember.getPsiElement());
        }
        return this;
    }

    @Override
    public BuilderPsiClassBuilder withPrivateConstructor() {
        checkFields();
        PsiMethod constructor = elementFactory.createConstructor();
        constructor.getModifierList().setModifierProperty(PRIVATE_STRING, true);
        builderClass.add(constructor);
        return this;
    }

    @Override
    public BuilderPsiClassBuilder withInitializingMethod() {
        checkFields();
        PsiMethod staticMethod = elementFactory.createMethodFromText(
                "public static " + builderClassName + " a" + srcClassName + "() { return new " + builderClassName + "();}", srcClass);
        builderClass.add(staticMethod);
        return this;
    }

    @Override
    public BuilderPsiClassBuilder withSetMethods() {
        checkFields();
        return this;
    }

    @Override
    public BuilderPsiClassBuilder withBuildMethod() {
        checkFields();
        return this;
    }

    @Override
    public PsiClass build() {
        checkBuilderField();
        return builderClass;
    }

    private void checkBuilderField() {
        if (builderClass == null) {
            throw new IllegalStateException("Builder field not created. Invoke at least aBuilder() method before.");
        }
    }

    private void checkFields() {
        if (anyFieldIsNull()) {
            throw new IllegalStateException("Fields not set. Invoke aBuilder() method before.");
        }
    }

    private boolean anyFieldIsNull() {
        return (project == null || targetDirectory == null || srcClass == null || builderClassName == null || psiElementClassMembers == null);
    }
}
