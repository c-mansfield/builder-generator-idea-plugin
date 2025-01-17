package pl.mjedynak.idea.plugins.builder.verifier;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.mjedynak.idea.plugins.builder.settings.CodeStyleSettings;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class PsiFieldVerifierTest {

    private PsiFieldVerifier psiFieldVerifier;
    private PsiMethod[] constructors;
    private PsiMethod[] methods;
    private PsiParameter[] parameters;

    @Mock(strictness = LENIENT) private PsiField psiField;
    @Mock private PsiClass psiClass;
    @Mock private PsiMethod constructor;
    @Mock private PsiParameterList parameterList;
    @Mock(strictness = LENIENT) private PsiParameter parameter;
    @Mock private PsiType psiType;
    @Mock(strictness = LENIENT) private PsiMethod method;
    @Mock private PsiModifierList modifierList;
    @Mock(strictness = LENIENT) private CodeStyleSettings settings;

    private String name;

    @BeforeEach
    public void setUp() {
        psiFieldVerifier = new PsiFieldVerifier();
        setField(psiFieldVerifier, "codeStyleSettings", settings);
        given(settings.getParameterNamePrefix()).willReturn(EMPTY);
        given(settings.getFieldNamePrefix()).willReturn(EMPTY);
        constructors = new PsiMethod[1];
        constructors[0] = constructor;
        methods = new PsiMethod[1];
        methods[0] = method;
        parameters = new PsiParameter[1];
        parameters[0] = parameter;
        name = "name";
    }

    @Test
    void shouldNotVerifyThatFieldIsSetInConstructorIfConstructorDoesNotExist() {
        // given
        given(psiClass.getConstructors()).willReturn(new PsiMethod[0]);

        // when
        boolean result = psiFieldVerifier.isSetInConstructor(psiField, psiClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldNotVerifyThatFieldIsSetInConstructorIfConstructorHasDifferentParameterName() {
        // given
        prepareBehaviourForReturningParameter();
        given(parameter.getType()).willReturn(psiType);
        given(psiField.getType()).willReturn(psiType);
        given(parameter.getName()).willReturn(name);
        given(psiField.getName()).willReturn("differentName");

        // when
        boolean result = psiFieldVerifier.isSetInConstructor(psiField, psiClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldNotVerifyThatFieldIsSetInConstructorIfConstructorHasDifferentParameterType() {
        // given
        PsiType differentPsiType = mock(PsiType.class);
        prepareBehaviourForReturningParameter();
        given(parameter.getType()).willReturn(psiType);
        given(psiField.getType()).willReturn(differentPsiType);
        given(parameter.getName()).willReturn(name);
        given(psiField.getName()).willReturn(name);

        // when
        boolean result = psiFieldVerifier.isSetInConstructor(psiField, psiClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldVerifyThatFieldIsSetInConstructorIfConstructorHasTheSameParameterTypeAndName() {
        // given
        prepareBehaviourForReturningParameter();
        given(parameter.getType()).willReturn(psiType);
        given(psiField.getType()).willReturn(psiType);
        given(parameter.getName()).willReturn(name);
        given(psiField.getName()).willReturn(name);

        // when
        boolean result = psiFieldVerifier.isSetInConstructor(psiField, psiClass);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldVerifyThatFieldIsSetInSetterMethodIfItIsNotPrivateAndHasCorrectParameter() {
        // given
        given(psiClass.getAllMethods()).willReturn(methods);
        given(method.getModifierList()).willReturn(modifierList);
        given(psiField.getName()).willReturn("field");
        given(method.getName()).willReturn("setField");

        // when
        boolean result = psiFieldVerifier.isSetInSetterMethod(psiField, psiClass);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldVerifyThatFieldIsNotSetInSetterMethodIfItIsPrivate() {
        // given
        given(psiClass.getAllMethods()).willReturn(methods);
        given(method.getModifierList()).willReturn(modifierList);
        given(psiField.getName()).willReturn("field");
        given(modifierList.hasExplicitModifier(PsiModifier.PRIVATE)).willReturn(true);
        given(method.getName()).willReturn("setField");
        // when
        boolean result = psiFieldVerifier.isSetInSetterMethod(psiField, psiClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldVerifyThatFieldIsNotSetInSetterMethodIfItIsNotPrivateButHasIncorrectParameter() {
        // given
        given(psiClass.getAllMethods()).willReturn(methods);
        given(method.getModifierList()).willReturn(modifierList);
        given(psiField.getName()).willReturn("field");
        given(method.getName()).willReturn("setAnotherField");
        // when
        boolean result = psiFieldVerifier.isSetInSetterMethod(psiField, psiClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldVerifyThatFieldHasGetterMethodAvailableIfTheMethodIsNotPrivateAndHasCorrectName() {
        // given
        given(psiClass.getAllMethods()).willReturn(methods);
        given(method.getModifierList()).willReturn(modifierList);
        given(psiField.getName()).willReturn("field");
        given(method.getName()).willReturn("getField");

        // when
        boolean result = psiFieldVerifier.hasGetterMethod(psiField, psiClass);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldVerifyThatFieldHasNoGetterMethodAvailableIfTheMethodIsPrivate() {
        // given
        given(psiClass.getAllMethods()).willReturn(methods);
        given(method.getModifierList()).willReturn(modifierList);
        given(psiField.getName()).willReturn("field");
        given(modifierList.hasExplicitModifier(PsiModifier.PRIVATE)).willReturn(true);
        given(method.getName()).willReturn("setField");
        // when
        boolean result = psiFieldVerifier.hasGetterMethod(psiField, psiClass);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldVerifyThatFieldHasNoGetterMethodAvailableIfTheMethodIsNotPrivateButHasIncorrectName() {
        // given
        given(psiClass.getAllMethods()).willReturn(methods);
        given(method.getModifierList()).willReturn(modifierList);
        given(psiField.getName()).willReturn("field");
        given(method.getName()).willReturn("getAnotherField");
        // when
        boolean result = psiFieldVerifier.hasGetterMethod(psiField, psiClass);

        // then
        assertThat(result).isFalse();
    }

    private void prepareBehaviourForReturningParameter() {
        given(psiClass.getConstructors()).willReturn(constructors);
        given(constructor.getParameterList()).willReturn(parameterList);
        given(parameterList.getParameters()).willReturn(parameters);
    }
}
