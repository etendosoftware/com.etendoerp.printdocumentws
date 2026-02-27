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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.model.common.enterprise.DocumentTemplate;

/**
 * Tests for {@link PrintDocument}.
 */
@ExtendWith(MockitoExtension.class)
class PrintDocumentTest {

  private static final String PARAM_ORDER = "order";
  private static final String PARAM_INVOICE = "invoice";
  private static final String PARAM_SHIPMENT = "shipment";
  private static final String PARAM_QUOTATION = "quotation";
  private static final String PARAM_ORDER_PROFORMA = "orderProforma";
  private static final String PARAM_SHIPMENT_VALUED = "shipmentValued";
  private static final String PARAM_QUOTATION_PROFORMA = "quotationProforma";
  private static final String PARAM_ORGANIZATION = "organization";
  private static final String PARAM_PURCHASE = "purchase";
  private static final String DOC_ORD_001 = "ORD-001";
  private static final String ORG_1 = "ORG-1";

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpServletResponse mockResponse;

  private PrintDocument printDocument;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    printDocument = spy(new PrintDocument());
  }

  /**
   * Verifies that an OBException is thrown when organization parameter is null.
   */
  @Test
  void testDoGetThrowsWhenOrganizationIsNull() {
    when(mockRequest.getParameter(PARAM_ORDER)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_INVOICE)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORDER_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT_VALUED)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORGANIZATION)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_PURCHASE)).thenReturn(null);

    OBException ex = assertThrows(OBException.class,
        () -> printDocument.doGet("", mockRequest, mockResponse));
    assertTrue(ex.getMessage().contains(PARAM_ORGANIZATION));
  }

  /**
   * Verifies that an OBException is thrown when organization parameter is empty.
   */
  @Test
  void testDoGetThrowsWhenOrganizationIsEmpty() {
    when(mockRequest.getParameter(PARAM_ORDER)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_INVOICE)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORDER_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT_VALUED)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORGANIZATION)).thenReturn("");
    when(mockRequest.getParameter(PARAM_PURCHASE)).thenReturn(null);

    OBException ex = assertThrows(OBException.class,
        () -> printDocument.doGet("", mockRequest, mockResponse));
    assertTrue(ex.getMessage().contains(PARAM_ORGANIZATION));
  }

  /**
   * Verifies that an OBException with document details is thrown when no documents are found.
   *
   * @throws Exception if an error occurs
   */
  @Test
  void testDoGetThrowsWhenNoDocumentsFound() throws Exception {
    when(mockRequest.getParameter(PARAM_ORDER)).thenReturn(DOC_ORD_001);
    when(mockRequest.getParameter(PARAM_INVOICE)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORDER_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT_VALUED)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORGANIZATION)).thenReturn(ORG_1);
    when(mockRequest.getParameter(PARAM_PURCHASE)).thenReturn(null);

    doNothing().when(printDocument).fillDocuments(nullable(String.class), nullable(String.class),
        nullable(String.class), nullable(String.class), nullable(String.class),
        nullable(String.class));

    OBException ex = assertThrows(OBException.class,
        () -> printDocument.doGet("", mockRequest, mockResponse));
    assertTrue(ex.getMessage().contains(DOC_ORD_001));
  }

  /**
   * Verifies that the error message includes all provided document numbers when none are found.
   *
   * @throws Exception if an error occurs
   */
  @Test
  void testDoGetErrorMessageIncludesAllDocumentNumbers() throws Exception {
    when(mockRequest.getParameter(PARAM_ORDER)).thenReturn(DOC_ORD_001);
    when(mockRequest.getParameter(PARAM_INVOICE)).thenReturn("INV-001");
    when(mockRequest.getParameter(PARAM_SHIPMENT)).thenReturn("SHP-001");
    when(mockRequest.getParameter(PARAM_QUOTATION)).thenReturn("QUO-001");
    when(mockRequest.getParameter(PARAM_ORDER_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT_VALUED)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORGANIZATION)).thenReturn(ORG_1);
    when(mockRequest.getParameter(PARAM_PURCHASE)).thenReturn(null);

    doNothing().when(printDocument).fillDocuments(nullable(String.class), nullable(String.class),
        nullable(String.class), nullable(String.class), nullable(String.class),
        nullable(String.class));

    OBException ex = assertThrows(OBException.class,
        () -> printDocument.doGet("", mockRequest, mockResponse));
    String msg = ex.getMessage();
    assertTrue(msg.contains(DOC_ORD_001));
    assertTrue(msg.contains("INV-001"));
    assertTrue(msg.contains("SHP-001"));
    assertTrue(msg.contains("QUO-001"));
  }

  /**
   * Verifies that orderProforma sets the document type to SALESORDER.
   *
   * @throws Exception if an error occurs
   */
  @Test
  void testDoGetSetsDocumentTypeForOrderProforma() throws Exception {
    when(mockRequest.getParameter(PARAM_ORDER)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_INVOICE)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORDER_PROFORMA)).thenReturn("PRO-001");
    when(mockRequest.getParameter(PARAM_SHIPMENT_VALUED)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORGANIZATION)).thenReturn(ORG_1);
    when(mockRequest.getParameter(PARAM_PURCHASE)).thenReturn(null);

    doNothing().when(printDocument).fillDocuments(nullable(String.class), nullable(String.class),
        nullable(String.class), nullable(String.class), nullable(String.class),
        nullable(String.class));

    List<BaseOBObject> orderList = new ArrayList<>();
    orderList.add(mock(BaseOBObject.class));
    doReturn(orderList).when(printDocument).getOrder("PRO-001", ORG_1, null);
    doNothing().when(printDocument).generateDocuments(mockRequest, mockResponse,
        printDocument.customDocuments, true);
    doNothing().when(printDocument).printReports(mockResponse, false);

    printDocument.doGet("", mockRequest, mockResponse);

    assertEquals(DocumentType.SALESORDER, getDocumentType(printDocument));
    assertEquals(1, printDocument.customDocuments.size());
  }

  /**
   * Verifies that shipmentValued sets the document type to SHIPMENT.
   *
   * @throws Exception if an error occurs
   */
  @Test
  void testDoGetSetsDocumentTypeForShipmentValued() throws Exception {
    when(mockRequest.getParameter(PARAM_ORDER)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_INVOICE)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORDER_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT_VALUED)).thenReturn("SHV-001");
    when(mockRequest.getParameter(PARAM_QUOTATION_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORGANIZATION)).thenReturn(ORG_1);
    when(mockRequest.getParameter(PARAM_PURCHASE)).thenReturn(null);

    doNothing().when(printDocument).fillDocuments(nullable(String.class), nullable(String.class),
        nullable(String.class), nullable(String.class), nullable(String.class),
        nullable(String.class));

    List<BaseOBObject> shipmentList = new ArrayList<>();
    shipmentList.add(mock(BaseOBObject.class));
    doReturn(shipmentList).when(printDocument).getShipment("SHV-001", ORG_1, null);
    doNothing().when(printDocument).generateDocuments(mockRequest, mockResponse,
        printDocument.customDocuments, true);
    doNothing().when(printDocument).printReports(mockResponse, false);

    printDocument.doGet("", mockRequest, mockResponse);

    assertEquals(DocumentType.SHIPMENT, getDocumentType(printDocument));
    assertEquals(1, printDocument.customDocuments.size());
  }

  /**
   * Verifies that quotationProforma sets the document type to QUOTATION.
   *
   * @throws Exception if an error occurs
   */
  @Test
  void testDoGetSetsDocumentTypeForQuotationProforma() throws Exception {
    when(mockRequest.getParameter(PARAM_ORDER)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_INVOICE)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_ORDER_PROFORMA)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_SHIPMENT_VALUED)).thenReturn(null);
    when(mockRequest.getParameter(PARAM_QUOTATION_PROFORMA)).thenReturn("QPR-001");
    when(mockRequest.getParameter(PARAM_ORGANIZATION)).thenReturn(ORG_1);
    when(mockRequest.getParameter(PARAM_PURCHASE)).thenReturn(null);

    doNothing().when(printDocument).fillDocuments(nullable(String.class), nullable(String.class),
        nullable(String.class), nullable(String.class), nullable(String.class),
        nullable(String.class));

    List<BaseOBObject> quotationList = new ArrayList<>();
    quotationList.add(mock(BaseOBObject.class));
    doReturn(quotationList).when(printDocument).getQuotation("QPR-001", ORG_1, null);
    doNothing().when(printDocument).generateDocuments(mockRequest, mockResponse,
        printDocument.customDocuments, true);
    doNothing().when(printDocument).printReports(mockResponse, false);

    printDocument.doGet("", mockRequest, mockResponse);

    assertEquals(DocumentType.QUOTATION, getDocumentType(printDocument));
    assertEquals(1, printDocument.customDocuments.size());
  }

  /**
   * Verifies that getReportManager delegates to super when customManager is false.
   */
  @Test
  void testGetReportManagerDelegatesToSuperWhenNotCustom() {
    org.openbravo.model.common.enterprise.DocumentType mockDocType =
        mock(org.openbravo.model.common.enterprise.DocumentType.class);

    doReturn(null).when(printDocument).getReportManager(mockRequest, mockDocType, false);

    assertNull(printDocument.getReportManager(mockRequest, mockDocType, false));
  }

  /**
   * Verifies that getReportManager delegates to super when template location is empty.
   */
  @Test
  void testGetReportManagerDelegatesToSuperWhenTemplateLocationEmpty() {
    org.openbravo.model.common.enterprise.DocumentType mockDocType =
        mock(org.openbravo.model.common.enterprise.DocumentType.class);
    DocumentTemplate mockTemplate = mock(DocumentTemplate.class);
    when(mockTemplate.isActive()).thenReturn(true);
    when(mockTemplate.getTemplateLocation()).thenReturn("");
    when(mockDocType.getDocumentTemplateList()).thenReturn(Collections.singletonList(mockTemplate));

    assertThrows(NullPointerException.class,
        () -> printDocument.getReportManager(mockRequest, mockDocType, true));
  }

  /**
   * Verifies that getReportManager delegates to super when no active templates exist.
   */
  @Test
  void testGetReportManagerDelegatesToSuperWhenNoActiveTemplates() {
    org.openbravo.model.common.enterprise.DocumentType mockDocType =
        mock(org.openbravo.model.common.enterprise.DocumentType.class);
    DocumentTemplate mockTemplate = mock(DocumentTemplate.class);
    when(mockTemplate.isActive()).thenReturn(false);
    when(mockDocType.getDocumentTemplateList()).thenReturn(Collections.singletonList(mockTemplate));

    assertThrows(NullPointerException.class,
        () -> printDocument.getReportManager(mockRequest, mockDocType, true));
  }

  /**
   * Verifies that customDocuments list is initially empty.
   */
  @Test
  void testCustomDocumentsInitiallyEmpty() {
    assertNotNull(printDocument.customDocuments);
    assertTrue(printDocument.customDocuments.isEmpty());
  }

  private static DocumentType getDocumentType(Object target) throws Exception {
    Field field = com.smf.ws.printdocument.PrintDocument.class.getDeclaredField("documentType");
    field.setAccessible(true);
    return (DocumentType) field.get(target);
  }
}
