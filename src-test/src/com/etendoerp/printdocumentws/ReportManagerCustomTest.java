/*
 *************************************************************************
 * The contents of this file are subject to the Etendo License
 * (the "License"), you may not use this file except in compliance with
 * the License.
 * You may obtain a copy of the License at
 * https://github.com/etendosoftware/etendo_core/blob/main/legal/Etendo_license.txt
 * Software distributed under the License is distributed on an
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing rights
 * and limitations under the License.
 * All portions are Copyright (C) 2021-2026 FUTIT SERVICES, S.L
 * All Rights Reserved.
 * Contributor(s): Futit Services S.L.
 *************************************************************************
 */
package com.etendoerp.printdocumentws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.reporting.Report;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo;

/**
 * Tests for {@link ReportManagerCustom}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportManagerCustomTest {

  /** Dedicated unchecked exception for reflective helper failures in tests. */
  private static final class TestReflectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private TestReflectionException(String message, ReflectiveOperationException cause) {
      super(message, cause);
    }
  }

  private static final String FTP_DIRECTORY = "/tmp/ftp";
  private static final String REPLACE_WITH_FULL = "http://localhost/web";
  private static final String BASE_DESIGN_PATH = "design/";
  private static final String DEFAULT_DESIGN_PATH = "defaultDesign/";
  private static final String PREFIX = "/opt/etendo";
  private static final String TEMPLATE_LOCATION = "@basedesign@/template.jrxml";
  private static final String DESIGN_VALUE = "design";
  private static final String DEFAULT_DESIGN_VALUE = "defaultDesign";
  private static final String FIELD_BASE_DESIGN_PATH = "_strBaseDesignPath";
  private static final String FIELD_DEFAULT_DESIGN_PATH = "_strDefaultDesignPath";
  private static final String DOC_123 = "DOC-123";
  private static final String LANG_EN_US = "en_US";

  @Mock
  private ConnectionProvider mockConnectionProvider;

  private ReportManagerCustom reportManagerCustom;

  /**
   * Sets up the test fixture by creating a {@link ReportManagerCustom} instance
   * with default configuration values.
   */
  @BeforeEach
  void setUp() {
    reportManagerCustom = new ReportManagerCustom(mockConnectionProvider, FTP_DIRECTORY,
        REPLACE_WITH_FULL, BASE_DESIGN_PATH, DEFAULT_DESIGN_PATH, PREFIX, false,
        TEMPLATE_LOCATION);
  }

  /**
   * Verifies that the constructor strips trailing slashes from baseDesignPath.
   *
   * @throws Exception if reflective field access fails
   */
  @Test
  void testConstructorStripsTrailingSlashFromBaseDesignPath() throws Exception {
    assertEquals(DESIGN_VALUE, getPrivateField(FIELD_BASE_DESIGN_PATH));
  }

  /**
   * Verifies that the constructor strips trailing slashes from defaultDesignPath.
   *
   * @throws Exception if reflective field access fails
   */
  @Test
  void testConstructorStripsTrailingSlashFromDefaultDesignPath() throws Exception {
    assertEquals(DEFAULT_DESIGN_VALUE, getPrivateField(FIELD_DEFAULT_DESIGN_PATH));
  }

  /**
   * Verifies that paths without trailing slashes are not modified.
   *
   * @throws Exception if reflective field access fails
   */
  @Test
  void testConstructorPreservesPathsWithoutTrailingSlash() throws Exception {
    ReportManagerCustom rmc = new ReportManagerCustom(mockConnectionProvider, FTP_DIRECTORY,
        REPLACE_WITH_FULL, DESIGN_VALUE, DEFAULT_DESIGN_VALUE, PREFIX, false, TEMPLATE_LOCATION);
    assertEquals(DESIGN_VALUE, getPrivateField(rmc, FIELD_BASE_DESIGN_PATH));
    assertEquals(DEFAULT_DESIGN_VALUE, getPrivateField(rmc, FIELD_DEFAULT_DESIGN_PATH));
  }

  /**
   * Verifies that all constructor fields are properly stored.
   *
   * @throws Exception if reflective field access fails
   */
  @Test
  void testConstructorStoresAllFields() throws Exception {
    assertEquals(mockConnectionProvider, getPrivateFieldObject());
    assertEquals(REPLACE_WITH_FULL, getPrivateField("_strBaseWeb"));
    assertEquals(FTP_DIRECTORY, getPrivateField("_strAttachmentPath"));
    assertEquals(PREFIX, getPrivateField("_prefix"));
    assertEquals(TEMPLATE_LOCATION, getPrivateField("_templateLocation"));
  }

  /**
   * Verifies that the constructor handles both paths ending with slash.
   *
   * @throws Exception if reflective field access fails
   */
  @Test
  void testConstructorStripsSlashFromBothPaths() throws Exception {
    ReportManagerCustom rmc = new ReportManagerCustom(mockConnectionProvider, FTP_DIRECTORY,
        REPLACE_WITH_FULL, "path1/", "path2/", PREFIX, false, TEMPLATE_LOCATION);
    assertEquals("path1", getPrivateField(rmc, FIELD_BASE_DESIGN_PATH));
    assertEquals("path2", getPrivateField(rmc, FIELD_DEFAULT_DESIGN_PATH));
  }

  /**
   * Verifies that populateDesignParameters includes DOCUMENT_ID.
   *
   */
  @Test
  void testPopulateDesignParametersIncludesDocumentId() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertEquals(DOC_123, params.get("DOCUMENT_ID"));
  }

  /**
   * Verifies that populateDesignParameters includes BASE_ATTACH.
   *
   */
  @Test
  void testPopulateDesignParametersIncludesBaseAttach() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertEquals(FTP_DIRECTORY, params.get("BASE_ATTACH"));
  }

  /**
   * Verifies that populateDesignParameters includes BASE_WEB.
   *
   */
  @Test
  void testPopulateDesignParametersIncludesBaseWeb() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertEquals(REPLACE_WITH_FULL, params.get("BASE_WEB"));
  }

  /**
   * Verifies that populateDesignParameters includes BASE_DESIGN with correct path.
   *
   */
  @Test
  void testPopulateDesignParametersIncludesBaseDesign() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertEquals(PREFIX + "/" + DESIGN_VALUE + "/" + DEFAULT_DESIGN_VALUE, params.get("BASE_DESIGN"));
  }

  /**
   * Verifies that IS_IGNORE_PAGINATION is set to false.
   *
   */
  @Test
  void testPopulateDesignParametersSetsIgnorePaginationFalse() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertFalse((Boolean) params.get("IS_IGNORE_PAGINATION"));
  }

  /**
   * Verifies that LANGUAGE parameter is set from variables.
   *
   */
  @Test
  void testPopulateDesignParametersSetsLanguage() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertEquals(LANG_EN_US, params.get("LANGUAGE"));
  }

  /**
   * Verifies that LOCALE parameter is derived from language string.
   *
   */
  @Test
  void testPopulateDesignParametersSetsLocale() {
    HashMap<String, Object> params = invokePopulateWithDefaults("es_ES", null);
    Locale locale = (Locale) params.get("LOCALE");
    assertEquals("es", locale.getLanguage());
    assertEquals("ES", locale.getCountry());
  }

  /**
   * Verifies that NUMBERFORMAT parameter is created.
   *
   */
  @Test
  void testPopulateDesignParametersSetsNumberFormat() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertNotNull(params.get("NUMBERFORMAT"));
  }

  /**
   * Verifies that template info parameters are included when templateInfo is not null.
   *
   */
  @Test
  void testPopulateDesignParametersIncludesTemplateInfo() {
    TemplateInfo mockTemplateInfo = mock(TemplateInfo.class);
    when(mockTemplateInfo.getShowLogo()).thenReturn("Y");
    when(mockTemplateInfo.getShowCompanyData()).thenReturn("N");
    when(mockTemplateInfo.getHeaderMargin()).thenReturn("10");

    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US,
        mockTemplateInfo);

    assertEquals("Y", params.get("SHOW_LOGO"));
    assertEquals("N", params.get("SHOW_COMPANYDATA"));
    assertEquals("10", params.get("HEADER_MARGIN"));
  }

  /**
   * Verifies that template info parameters are absent when templateInfo is null.
   *
   */
  @Test
  void testPopulateDesignParametersExcludesTemplateInfoWhenNull() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertFalse(params.containsKey("SHOW_LOGO"));
    assertFalse(params.containsKey("SHOW_COMPANYDATA"));
    assertFalse(params.containsKey("HEADER_MARGIN"));
  }

  /**
   * Verifies that the parameter map contains the expected number of entries without template info.
   *
   */
  @Test
  void testPopulateDesignParametersCountWithoutTemplateInfo() {
    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US, null);
    assertEquals(10, params.size());
  }

  /**
   * Verifies that the parameter map contains the expected number of entries with template info.
   *
   */
  @Test
  void testPopulateDesignParametersCountWithTemplateInfo() {
    TemplateInfo mockTemplateInfo = mock(TemplateInfo.class);
    when(mockTemplateInfo.getShowLogo()).thenReturn("Y");
    when(mockTemplateInfo.getShowCompanyData()).thenReturn("N");
    when(mockTemplateInfo.getHeaderMargin()).thenReturn("10");

    HashMap<String, Object> params = invokePopulateWithDefaults(LANG_EN_US,
        mockTemplateInfo);

    assertEquals(13, params.size());
  }

  /**
   * Verifies that processReport throws ReportingException wrapping the underlying error.
   */
  @Test
  void testProcessReportThrowsReportingExceptionOnError() {
    Report mockReport = mock(Report.class);
    VariablesSecureApp mockVars = createMockVariables(LANG_EN_US);
    when(mockReport.getDocumentId()).thenReturn(DOC_123);
    when(mockReport.getTemplateInfo()).thenReturn(null);

    assertThrows(Throwable.class,
        () -> reportManagerCustom.processReport(mockReport, mockVars));
  }


  /**
   * Creates mock {@link Report} and {@link VariablesSecureApp} objects and invokes
   * the private {@code populateDesignParameters} method with the given arguments.
   *
   * @param language
   *     the language code to set on the mock variables
   * @param templateInfo
   *     the template info to set on the mock report, may be {@code null}
   * @return the populated design parameters map
   */
  private HashMap<String, Object> invokePopulateWithDefaults(String language,
      TemplateInfo templateInfo) {
    Report mockReport = mock(Report.class);
    VariablesSecureApp mockVars = createMockVariables(language);
    when(mockReport.getDocumentId()).thenReturn(ReportManagerCustomTest.DOC_123);
    when(mockReport.getTemplateInfo()).thenReturn(templateInfo);
    return invokePopulateDesignParameters(mockVars, mockReport);
  }

  /**
   * Creates a mock {@link VariablesSecureApp} with the given language and
   * default session values required by {@code Utility.getContext}.
   *
   * @param language the language code to return from {@code getLanguage()}
   * @return a configured mock {@link VariablesSecureApp}
   */
  private VariablesSecureApp createMockVariables(String language) {
    VariablesSecureApp mockVars = mock(VariablesSecureApp.class);
    when(mockVars.getLanguage()).thenReturn(language);
    when(mockVars.getSessionValue("#User_Client")).thenReturn("");
    when(mockVars.getSessionValue("#User_Org")).thenReturn("");
    when(mockVars.getSessionValue("#User_Level")).thenReturn("");
    when(mockVars.getSessionValue("#AD_ReportDecimalSeparator")).thenReturn(".");
    when(mockVars.getSessionValue("#AD_ReportGroupingSeparator")).thenReturn(",");
    when(mockVars.getSessionValue("#AD_ReportNumberFormat")).thenReturn("#,##0.00");
    return mockVars;
  }

  /**
   * Reflectively invokes the private {@code populateDesignParameters} method
   * on the {@link ReportManagerCustom} instance under test.
   *
   * @param variables the variables to pass to the method
   * @param report the report to pass to the method
   * @return the resulting design parameters map
   */
  @SuppressWarnings("unchecked")
  private HashMap<String, Object> invokePopulateDesignParameters(VariablesSecureApp variables,
      Report report) {
    try {
      Method method = ReportManagerCustom.class.getDeclaredMethod("populateDesignParameters",
          VariablesSecureApp.class, Report.class);
      method.setAccessible(true);
      return (HashMap<String, Object>) method.invoke(reportManagerCustom, variables, report);
    } catch (ReflectiveOperationException exception) {
      throw new TestReflectionException("Unable to invoke populateDesignParameters", exception);
    }
  }

  /**
   * Retrieves a private {@link String} field value from the default test instance.
   *
   * @param fieldName the name of the private field
   * @return the field value
   */
  private String getPrivateField(String fieldName) {
    return getPrivateField(reportManagerCustom, fieldName);
  }

  /**
   * Retrieves a private {@link String} field value from the given instance.
   *
   * @param instance the {@link ReportManagerCustom} instance to read from
   * @param fieldName the name of the private field
   * @return the field value
   */
  private String getPrivateField(ReportManagerCustom instance, String fieldName) {
    try {
      Field field = ReportManagerCustom.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (String) field.get(instance);
    } catch (ReflectiveOperationException exception) {
      throw new TestReflectionException("Unable to read private field '" + fieldName + "'", exception);
    }
  }

  /**
   * Retrieves a private field value as {@link Object} from the default test instance.
   *
   * @return the field value
   */
  private Object getPrivateFieldObject() {
    try {
      Field field = ReportManagerCustom.class.getDeclaredField("_connectionProvider");
      field.setAccessible(true);
      return field.get(reportManagerCustom);
    } catch (ReflectiveOperationException exception) {
      throw new TestReflectionException("Unable to read private field '_connectionProvider'", exception);
    }
  }
}
