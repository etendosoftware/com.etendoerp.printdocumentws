package com.smf.gloria.printdocumentws;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.report.ReportingUtils;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.Report;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.utils.Replace;

import net.sf.jasperreports.engine.JasperPrint;

public class ReportManagerCustom extends ReportManager {

  private ConnectionProvider _connectionProvider;
  private String _strBaseDesignPath;
  private String _strDefaultDesignPath;
  private String language;
  private String _strBaseWeb; // BASE WEB!!!!!!
  private String _prefix;
  private String _strAttachmentPath;
  private String _templateLocation;

  public ReportManagerCustom(ConnectionProvider connectionProvider, String ftpDirectory,
      String replaceWithFull, String baseDesignPath, String defaultDesignPath, String prefix,
      boolean multiReport, String templateLocation) {
    super(connectionProvider, ftpDirectory, replaceWithFull, baseDesignPath, defaultDesignPath,
        prefix, multiReport);

    _connectionProvider = connectionProvider;
    _strBaseWeb = replaceWithFull;
    _strBaseDesignPath = baseDesignPath;
    _strDefaultDesignPath = defaultDesignPath;
    _strAttachmentPath = ftpDirectory;
    _prefix = prefix;

    // Strip of ending slash character
    if (_strBaseDesignPath.endsWith("/"))
      _strBaseDesignPath = _strBaseDesignPath.substring(0, _strBaseDesignPath.length() - 1);
    if (_strDefaultDesignPath.endsWith("/"))
      _strDefaultDesignPath = _strDefaultDesignPath.substring(0,
          _strDefaultDesignPath.length() - 1);

    _templateLocation = templateLocation;

  }

  public JasperPrint processReport(Report report, VariablesSecureApp variables)
      throws ReportingException {

    setTargetDirectory(report);
    language = variables.getLanguage();
    final String baseDesignPath = _prefix + "/" + _strBaseDesignPath + "/" + _strDefaultDesignPath;

    _templateLocation = Replace.replace(
        Replace.replace(_templateLocation, "@basedesign@", baseDesignPath), "@baseattach@",
        _strAttachmentPath);
    _templateLocation = Replace.replace(_templateLocation, "//", "/");
    final String templateFile = _templateLocation;

    final HashMap<String, Object> designParameters = populateDesignParameters(variables, report);
    designParameters.put("TEMPLATE_LOCATION", _templateLocation);
    JasperPrint jasperPrint = null;

    try {
      jasperPrint = ReportingUtils.generateJasperPrint(templateFile, designParameters, true,
          new DalConnectionProvider(), null);
    } catch (final Exception exception) {
      exception.getStackTrace();
      throw new ReportingException(exception);
    }

    return jasperPrint;
  }

  private HashMap<String, Object> populateDesignParameters(VariablesSecureApp variables,
      Report report) {
    final String baseDesignPath = _prefix + "/" + _strBaseDesignPath + "/" + _strDefaultDesignPath;
    final HashMap<String, Object> designParameters = new HashMap<String, Object>();

    designParameters.put("DOCUMENT_ID", report.getDocumentId());

    designParameters.put("BASE_ATTACH", _strAttachmentPath);
    designParameters.put("BASE_WEB", _strBaseWeb);
    designParameters.put("BASE_DESIGN", baseDesignPath);
    designParameters.put("IS_IGNORE_PAGINATION", false);
    designParameters.put("USER_CLIENT",
        Utility.getContext(_connectionProvider, variables, "#User_Client", ""));
    designParameters.put("USER_ORG",
        Utility.getContext(_connectionProvider, variables, "#User_Org", ""));

    final String lang = variables.getLanguage();
    designParameters.put("LANGUAGE", lang);

    final Locale locale = new Locale(lang.substring(0, 2), lang.substring(3, 5));
    designParameters.put("LOCALE", locale);

    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(variables.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
    dfs.setGroupingSeparator(variables.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
    final DecimalFormat NumberFormat = new DecimalFormat(
        variables.getSessionValue("#AD_ReportNumberFormat"), dfs);
    designParameters.put("NUMBERFORMAT", NumberFormat);

    if (report.getTemplateInfo() != null) {
      designParameters.put("SHOW_LOGO", report.getTemplateInfo().getShowLogo());
      designParameters.put("SHOW_COMPANYDATA", report.getTemplateInfo().getShowCompanyData());
      designParameters.put("HEADER_MARGIN", report.getTemplateInfo().getHeaderMargin());
    }

    return designParameters;
  }

}
