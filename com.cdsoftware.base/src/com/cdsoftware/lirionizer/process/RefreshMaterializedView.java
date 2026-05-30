/**********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Casa del Software                                                 *
 * - Angel Lara                                                        *
 **********************************************************************/
package com.cdsoftware.lirionizer.process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

/**
 * Server process that refreshes a PostgreSQL materialized view based on the AD_Table_ID provided.
 * It identifies the physical table/view name from AD_Table and performs the REFRESH MATERIALIZED VIEW operation.
 */
@org.adempiere.base.annotation.Process
public class RefreshMaterializedView extends SvrProcess {

    private int p_AD_Table_ID = 0;
    private boolean p_IsConcurrent = false;

    /**
     * Reads process parameters.
     *
     * Parameters:
     * - AD_Table_ID: ID of the AD_Table representing the materialized view.
     * - IsConcurrent: Flag to indicate if the refresh should be executed concurrently.
     */
    @Override
    protected void prepare() {
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() != null) {
                if (name.equals("AD_Table_ID")) {
                    p_AD_Table_ID = para[i].getParameterAsInt();
                } else if (name.equals("IsConcurrent")) {
                    p_IsConcurrent = "Y".equals(para[i].getParameterAsString());
                } else {
                    log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
                }
            }
        }
    }

    /**
     * Validates parameters, looks up the active table name, performs security and existence checks on the materialized view,
     * and executes the REFRESH MATERIALIZED VIEW command.
     *
     * @return Success message containing the view name, duration, and mode (Concurrent or Exclusive).
     * @throws Exception if AD_Table_ID is invalid, view name is not found, SQL injection is suspected, view does not exist, or concurrent refresh lacks a unique index.
     */
    @Override
    protected String doIt() throws Exception {
        if (p_AD_Table_ID <= 0) {
            log.severe("Error de seguridad: AD_Table_ID es inválido (<= 0). Usuario: " + getAD_User_ID());
            throw new AdempiereException("Parámetro AD_Table_ID es requerido y debe ser mayor a 0.");
        }

        // Obtener el nombre de la vista en el diccionario de datos
        String sqlTableName = "SELECT TableName FROM AD_Table WHERE AD_Table_ID = ? AND IsActive = 'Y'";
        String tableName = DB.getSQLValueString(get_TrxName(), sqlTableName, p_AD_Table_ID);

        if (tableName == null || tableName.trim().isEmpty()) {
            throw new AdempiereException("No se encontró el nombre de la tabla o vista activa para el AD_Table_ID provisto.");
        }

        // Validación de seguridad contra SQL injection
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
            log.severe("Error de seguridad: El nombre de la vista no es válido por seguridad. Posible inyección SQL: " + tableName);
            throw new AdempiereException("El nombre de la vista no es válido por razones de seguridad.");
        }

        // Validar que la vista materializada realmente exista en la capa física de PostgreSQL
        String checkMatViewSql = "SELECT 1 FROM pg_matviews WHERE matviewname = ?";
        int existsMatView = DB.getSQLValue(get_TrxName(), checkMatViewSql, tableName.toLowerCase());
        if (existsMatView <= 0) {
            log.severe("Validación fallida: " + tableName + " no existe como vista materializada en la base de datos física.");
            throw new Exception("La vista materializada '" + tableName + "' no existe en la base de datos.");
        }

        // Si es concurrente, validar que la vista posea al menos un índice único
        if (p_IsConcurrent) {
            String checkUniqueIndexSql = "SELECT 1 "
                    + "FROM pg_class c "
                    + "JOIN pg_index i ON c.oid = i.indrelid "
                    + "WHERE c.relkind = 'm' "
                    + "AND c.relname = ? "
                    + "AND i.indisunique = true "
                    + "LIMIT 1";
            int hasUniqueIndex = DB.getSQLValue(get_TrxName(), checkUniqueIndexSql, tableName.toLowerCase());
            if (hasUniqueIndex <= 0) {
                log.severe("Validación fallida: Se solicitó refresh concurrente pero " + tableName + " no posee índice único.");
                throw new Exception("Para realizar un refrescamiento concurrente, la vista materializada '" + tableName + "' debe poseer al menos un índice único.");
            }
        }

        log.info("Iniciando actualización de vista materializada: " + tableName 
                + " | Concurrente: " + p_IsConcurrent + " | Solicitado por: " + getAD_User_ID());

        String sql = "REFRESH MATERIALIZED VIEW " + (p_IsConcurrent ? "CONCURRENTLY " : "") + tableName;

        long startTime = System.currentTimeMillis();
        PreparedStatement pstmt = null;
        try {
            pstmt = DB.prepareStatement(sql, get_TrxName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.severe("Error refrescando la vista materializada " + tableName + ": " + e.getMessage());
            throw new RuntimeException("Error al intentar refrescar la base de datos: " + e.getMessage(), e);
        } finally {
            DB.close(pstmt);
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;
        
        // Conversión opcional de la duración para legibilidad
        String durationStr = durationMs + " ms";
        if (durationMs > 1000) {
            durationStr = String.format("%.2f segundos", durationMs / 1000.0);
        }

        log.info("Refresh finalizado de " + tableName + " en " + durationStr);

        return "Vista materializada " + tableName + " actualizada correctamente en " + durationStr + ". "
               + (p_IsConcurrent ? "(Concurrente)" : "(Modo Exclusivo)");
    }
}

