package com.cdsoftware.lirionizer.process;

/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.adempiere.exceptions.DBException;
import org.compiere.model.MProcessPara;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

/**
 *	Unallocated Payments To Date Report
 *	Based on C_Payment_V.
 *  @author Angel Lara
 */
@org.adempiere.base.annotation.Process
public class UnallocatedPayments extends SvrProcess
{
	/** The date to calculate the days due from			*/
	private Timestamp	p_DateAcct = null;
	private Timestamp	p_DateAcctTo = null;
	private String 	p_IsReceipt = "";
	private int			p_C_BPartner_ID = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("DateAcct")) {
				p_DateAcct = para[i].getParameterAsTimestamp();
				p_DateAcctTo = para[i].getParameter_ToAsTimestamp();
			}
			else if (name.equals("IsReceipt"))
				p_IsReceipt =  para[i].getParameterAsString();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else
				MProcessPara.validateUnknownParameter(getProcessInfo().getAD_Process_ID(), para[i]);
		}
	}	//	prepare

	/**
	 * 	DoIt
	 *	@return Message
	 *	@throws Exception
	 */
	protected String doIt()
	{
		if (log.isLoggable(Level.INFO)) log.info("DateAcct=" + p_DateAcct + ", DateAcctTo=" + p_DateAcctTo
			+ ", IsReceipt=" + p_IsReceipt + ", C_BPartner_ID=" + p_C_BPartner_ID); 

		StringBuilder sql = new StringBuilder();
		
		// Insert Clause
		sql.append("INSERT INTO T_CDS_C_Payment (");
		sql.append("c_payment_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, ");
		sql.append("documentno, datetrx, isreceipt, c_doctype_id, trxtype, c_bankaccount_id, c_bpartner_id, ");
		sql.append("c_invoice_id, c_bp_bankaccount_id, c_paymentbatch_id, tendertype, creditcardtype, ");
		sql.append("creditcardnumber, creditcardvv, creditcardexpmm, creditcardexpyy, micr, routingno, ");
		sql.append("accountno, checkno, a_name, a_street, a_city, a_state, a_zip, a_ident_dl, a_ident_ssn, ");
		sql.append("a_email, voiceauthcode, orig_trxid, ponum, c_currency_id, c_conversiontype_id, ");
		sql.append("payamt, discountamt, writeoffamt, taxamt, overunderamt, multiplierap, isoverunderpayment, ");
		sql.append("isapproved, r_pnref, r_result, r_respmsg, r_authcode, r_avsaddr, r_avszip, r_info, ");
		sql.append("processing, oprocessing, docstatus, docaction, isprepayment, c_charge_id, isreconciled, ");
		sql.append("isallocated, isonline, processed, posted, c_campaign_id, c_project_id, c_activity_id, ");
		sql.append("ad_orgtrx_id, chargeamt, c_order_id, dateacct, description, isselfservice, processedon, ");
		sql.append("currencyrate, convertedamt, isoverridecurrencyrate, ");
		sql.append("allocatedamttodate, openamttodate, ad_pinstance_id) ");

		sql.append("SELECT ");
		sql.append("p.c_payment_id, p.ad_client_id, p.ad_org_id, p.isactive, p.created, p.createdby, p.updated, p.updatedby, ");
		sql.append("p.documentno, p.datetrx, p.isreceipt, p.c_doctype_id, p.trxtype, p.c_bankaccount_id, p.c_bpartner_id, ");
		sql.append("p.c_invoice_id, p.c_bp_bankaccount_id, p.c_paymentbatch_id, p.tendertype, p.creditcardtype, ");
		sql.append("p.creditcardnumber, p.creditcardvv, p.creditcardexpmm, p.creditcardexpyy, p.micr, p.routingno, ");
		sql.append("p.accountno, p.checkno, p.a_name, p.a_street, p.a_city, p.a_state, p.a_zip, p.a_ident_dl, p.a_ident_ssn, ");
		sql.append("p.a_email, p.voiceauthcode, p.orig_trxid, p.ponum, p.c_currency_id, p.c_conversiontype_id, ");
		sql.append("p.payamt, p.discountamt, p.writeoffamt, p.taxamt, p.overunderamt, p.multiplierap, p.isoverunderpayment, ");
		sql.append("p.isapproved, p.r_pnref, p.r_result, p.r_respmsg, p.r_authcode, p.r_avsaddr, p.r_avszip, p.r_info, ");
		sql.append("p.processing, p.oprocessing, p.docstatus, p.docaction, p.isprepayment, p.c_charge_id, p.isreconciled, ");
		sql.append("p.isallocated, p.isonline, p.processed, p.posted, p.c_campaign_id, p.c_project_id, p.c_activity_id, ");
		sql.append("p.ad_orgtrx_id, p.chargeamt, p.c_order_id, p.dateacct, p.description, p.isselfservice, p.processedon, ");
		sql.append("p.currencyrate, p.convertedamt, p.isoverridecurrencyrate, ");
		sql.append("COALESCE(al.amount, 0) AS allocatedamttodate, ");
		sql.append("p.payamt - COALESCE(al.amount, 0) AS openamttodate, ");
		sql.append(this.getAD_PInstance_ID()).append(" AS ad_pinstance_id ");

		sql.append("FROM c_payment_v p ");
		sql.append("LEFT JOIN (");
		sql.append("  SELECT SUM(al.amount) AS amount, al.c_payment_id ");
		sql.append("  FROM C_AllocationLine al ");
		sql.append("  INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID = a.C_AllocationHdr_ID) ");
		sql.append("  WHERE a.IsActive = 'Y' AND al.c_payment_id IS NOT NULL ");

		if (p_DateAcct != null) {
		    sql.append("  AND a.DateAcct BETWEEN '").append(p_DateAcct).append("' AND '").append(p_DateAcctTo).append("' ");
		}

		sql.append("  GROUP BY al.c_payment_id ");
		sql.append(") al ON al.c_payment_id = p.c_payment_id ");
		sql.append("WHERE p.AD_Client_ID = ").append(this.getAD_Client_ID());

		if (p_C_BPartner_ID > 0) {
		    sql.append(" AND p.C_BPartner_ID = ").append(p_C_BPartner_ID);
		}
		if (p_IsReceipt.equals("Y")) {
		    sql.append(" AND p.IsReceipt = 'Y' ");
		} else if (p_IsReceipt.equals("N")) {
		    sql.append(" AND p.IsReceipt = 'N' ");
		}
		if (p_DateAcct != null) {
		    sql.append(" AND p.DateAcct BETWEEN '").append(p_DateAcct).append("' AND '").append(p_DateAcctTo).append("' ");
		}

		sql.append(" ORDER BY p.DateAcct, p.documentno");

		try {
		    DB.executeUpdateEx(sql.toString(), get_TrxName());
		}
		catch (DBException e1) {
			log.warning("Problem = " + e1.getLocalizedMessage());
		}
		//	
		return "";
	}	//	doIt

}	//	Aging

