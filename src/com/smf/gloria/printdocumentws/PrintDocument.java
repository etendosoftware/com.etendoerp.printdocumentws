package com.smf.gloria.printdocumentws;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.ConfigParameters;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.service.db.DalConnectionProvider;

import com.openbravo.gps.printandsend.Obpas_printsend_template;

public class PrintDocument extends com.smf.ws.printdocument.PrintDocument {

  public final List<BaseOBObject> customDocuments = new ArrayList<BaseOBObject>();

  @Override
  public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    // Get Parameters
    final String orderNumber = request.getParameter("order");
    final String invoiceNumber = request.getParameter("invoice");
    final String shipmentNumber = request.getParameter("shipment");
    final String quotationNumber = request.getParameter("quotation");
    final String orderProformaNumber = request.getParameter("orderProforma");
    final String shipmentValuedNumber = request.getParameter("shipmentValued");
    final String quotationProformaNumber = request.getParameter("quotationProforma");
    final String isPurchase = request.getParameter("purchase");

    fillDocuments(orderNumber, invoiceNumber, shipmentNumber, quotationNumber, isPurchase);
    if (documents != null && documents.size() > 0) {
      generateDocuments(request, response, documents, false);
    }

    if (orderProformaNumber != null) {
      documentType = DocumentType.SALESORDER;
      customDocuments.addAll(getOrder(orderProformaNumber, isPurchase));
    }
    if (shipmentValuedNumber != null) {
      documentType = DocumentType.SHIPMENT;
      customDocuments.addAll(getShipment(shipmentValuedNumber, isPurchase));
    }
    if (quotationProformaNumber != null) {
      documentType = DocumentType.QUOTATION;
      customDocuments.addAll(getQuotation(quotationProformaNumber, isPurchase));
    }

    if (customDocuments != null && customDocuments.size() > 0) {
      generateDocuments(request, response, customDocuments, true);
    }

    if ((documents == null || documents.size() == 0)
        && (customDocuments == null || customDocuments.size() == 0)) {
      String errorMessage = "";
      errorMessage = "No se pudo encontrar el documento. \n";
      if (orderNumber != null) {
        errorMessage = errorMessage + "Pedido de Venta: " + orderNumber + "\n";
      }
      if (shipmentNumber != null) {
        errorMessage = errorMessage + "Albarán: " + shipmentNumber + "\n";
      }
      if (invoiceNumber != null) {
        errorMessage = errorMessage + "Factura: " + invoiceNumber + "\n";
      }
      if (quotationNumber != null) {
        errorMessage = errorMessage + "Presupuesto: " + quotationNumber + "\n";
      }
      if (orderProformaNumber != null) {
        errorMessage = errorMessage + "Factura Proforma (pedido de venta): " + orderProformaNumber
            + "\n";
      }
      if (shipmentValuedNumber != null) {
        errorMessage = errorMessage + "Albarán Valorado (Cliente): " + shipmentValuedNumber + "\n";
      }
      if (quotationProformaNumber != null) {
        errorMessage = errorMessage + "Factura Proforma (presupuesto): " + quotationProformaNumber
            + "\n";
      }
      throw new OBException(errorMessage);
    } else {
      printReports(response, savedReports.size() > 1);
    }
  }

  @Override
  public ReportManager getReportManager(HttpServletRequest request,
      org.openbravo.model.common.enterprise.DocumentType docType, boolean customManager) {

    if (customManager) {
      String templateLocation = "";

      // Obtain custom template location and filename
      for (Obpas_printsend_template temp : docType.getObpasPrintsendTemplateList()) {
        if (temp.isActive()) {
          templateLocation = temp.getTemplatefilelocation();
          break;
        }
      }

      if (templateLocation == null || "".equals(templateLocation)) {
        return super.getReportManager(request, docType, false);
      }

      ServletContext context = request.getSession().getServletContext();
      ConfigParameters params = (ConfigParameters) context.getAttribute(CONFIG_ATTRIBUTE);

      return new ReportManagerCustom(new DalConnectionProvider(), params.strFTPDirectory,
          HttpBaseUtils.getLocalAddress(request) + "/web", params.strBaseDesignPath,
          params.strDefaultDesignPath, params.prefix, false, templateLocation);
    } else {
      return super.getReportManager(request, docType, false);
    }

  }

}