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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.adempiere.exceptions.DBException;
import org.compiere.model.MAging;
import org.compiere.model.MProcessPara;
import org.compiere.model.MRole;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.TimeUtil;

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
		
		//Insert Clause
		sql.append("INSERT INTO T_CDS_C_Payment ");
		
		sql.append("SELECT p.*,coalesce(al.amount,0) as allocatedamttodate,p.payamt-coalesce(al.amount,0) openamttodate,"
				+ this.getAD_PInstance_ID()+" "
				+ "FROM c_payment_v p "
				+ "LEFT JOIN (SELECT sum(al.amount) as amount,al.c_payment_id "
				+ "FROM C_AllocationLine al "
				+ "INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID) "
				+ "WHERE a.IsActive='Y' and al.c_payment_id is not null ");
		if (p_DateAcct != null)
		{
			sql.append("AND a.DateTrx BETWEEN '"+p_DateAcct+"' AND '"+p_DateAcctTo+"' ");
		}
		sql.append("GROUP BY al.c_payment_id "
				+ "ORDER BY al.C_Payment_ID) al on al.c_payment_id = p.c_payment_id "
				+ "WHERE p.AD_Client_ID = "+this.getAD_Client_ID());
		if (p_C_BPartner_ID > 0)
		{
			sql.append(" AND p.C_BPartner_ID=").append(p_C_BPartner_ID);
		}
		if (p_IsReceipt.equals("Y"))
		{
			sql.append(" AND p.IsReceipt='Y' ");
		} else if (p_IsReceipt.equals("N")) {
			sql.append(" AND p.IsReceipt='N' ");
		}
		if (p_DateAcct != null)
		{
			sql.append(" AND p.DateTrx BETWEEN '"+p_DateAcct+"' AND '"+p_DateAcctTo+"' ");
		}
		
		sql.append("ORDER BY p.DateTrx,p.documentno");
		
		//
		try
		{
			DB.executeUpdateEx(sql.toString(),get_TrxName());
		}
		catch (DBException e1) {
			log.warning("Problem = " + e1.getLocalizedMessage());
		}
		//	
		return "";
	}	//	doIt

}	//	Aging

