package com.etendoerp.printdocumentws;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.HttpBaseUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.model.common.enterprise.DocumentTemplate;
import org.openbravo.service.db.DalConnectionProvider;

public class PrintDocument extends com.smf.ws.printdocument.PrintDocument {

        public final List<BaseOBObject> customDocuments = new ArrayList<>();

        @Override public void doGet(String path, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

                // Get Parameters
                final String orderNumber = request.getParameter("order");
                final String invoiceNumber = request.getParameter("invoice");
                final String shipmentNumber = request.getParameter("shipment");
                final String quotationNumber = request.getParameter("quotation");
                final String orderProformaNumber = request.getParameter("orderProforma");
                final String shipmentValuedNumber = request.getParameter("shipmentValued");
                final String quotationProformaNumber = request.getParameter("quotationProforma");
                final String orgId = request.getParameter("organization");
                final String isPurchase = request.getParameter("purchase");

                if (orgId == null || StringUtils.isEmpty(orgId)) {
                        throw new OBException(
                            "El parámetro 'organization' es obligatorio y no puede ser vacío.");
                }

                fillDocuments(orderNumber, invoiceNumber, shipmentNumber, quotationNumber, orgId,
                    isPurchase);
                if (documents != null && documents.size() > 0) {
                        generateDocuments(request, response, documents, false);
                }

                if (orderProformaNumber != null) {
                        documentType = DocumentType.SALESORDER;
                        customDocuments.addAll(getOrder(orderProformaNumber, orgId, isPurchase));
                }
                if (shipmentValuedNumber != null) {
                        documentType = DocumentType.SHIPMENT;
                        customDocuments.addAll(
                            getShipment(shipmentValuedNumber, orgId, isPurchase));
                }
                if (quotationProformaNumber != null) {
                        documentType = DocumentType.QUOTATION;
                        customDocuments.addAll(
                            getQuotation(quotationProformaNumber, orgId, isPurchase));
                }

                if (customDocuments != null && customDocuments.size() > 0) {
                        generateDocuments(request, response, customDocuments, true);
                }

                if ((documents == null || documents.size() == 0) && (customDocuments == null || customDocuments.size() == 0)) {
                        String errorMessage = "";
                        errorMessage = "The document could not be found. \n";
                        if (orderNumber != null) {
                                errorMessage = errorMessage + "Order Document Number: " + orderNumber + "\n";
                        }
                        if (shipmentNumber != null) {
                                errorMessage = errorMessage + "Shipment/Receipt Document Number: " + shipmentNumber + "\n";
                        }
                        if (invoiceNumber != null) {
                                errorMessage = errorMessage + "Invoice Document Number: " + invoiceNumber + "\n";
                        }
                        if (quotationNumber != null) {
                                errorMessage = errorMessage + "Quotation Document Number: " + quotationNumber + "\n";
                        }
                        if (orderProformaNumber != null) {
                                errorMessage = errorMessage + "Proforma Invoice (Sales Order): " + orderProformaNumber + "\n";
                        }
                        if (shipmentValuedNumber != null) {
                                errorMessage = errorMessage + "Valued Delivery Note (Customer): " + shipmentValuedNumber + "\n";
                        }
                        if (quotationProformaNumber != null) {
                                errorMessage = errorMessage + "Proforma Invoice (budget): " + quotationProformaNumber + "\n";
                        }
                        throw new OBException(errorMessage);
                } else {
                        printReports(response, savedReports.size() > 1);
                }
        }

        @Override public ReportManager getReportManager(HttpServletRequest request,
            org.openbravo.model.common.enterprise.DocumentType docType, boolean customManager) {

                if (customManager) {
                        String templateLocation = "";

                        // Obtain custom template location and filename
                        for (DocumentTemplate temp : docType.getDocumentTemplateList()) {
                                if (temp.isActive()) {
                                        templateLocation = temp.getTemplateLocation();
                                        break;
                                }
                        }

                        if (templateLocation == null || "".equals(templateLocation)) {
                                return super.getReportManager(request, docType, false);
                        }

                        ServletContext context = request.getSession().getServletContext();
                        ConfigParameters params = (ConfigParameters) context.getAttribute(
                            CONFIG_ATTRIBUTE);

                        return new ReportManagerCustom(new DalConnectionProvider(),
                            params.strFTPDirectory, HttpBaseUtils.getLocalAddress(request) + "/web",
                            params.strBaseDesignPath, params.strDefaultDesignPath, params.prefix,
                            false, templateLocation);
                } else {
                        return super.getReportManager(request, docType, false);
                }

        }

}
