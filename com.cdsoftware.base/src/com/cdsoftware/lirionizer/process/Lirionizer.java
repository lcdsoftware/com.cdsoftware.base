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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MSystem;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.compiere.util.Ini;

import com.cdsoftware.lirionizer.base.CustomProcess;

/**
 * Server process that extracts and deploys theme template files to the iDempiere and Jetty directories.
 * It identifies the server configuration and copies HTML, JSP, and ZIP templates to customize the web interface.
 */
@org.adempiere.base.annotation.Process
public class Lirionizer extends CustomProcess{
	
	private String p_jettyPath = "";
	private String p_theme_prefix = "";

	/**
	 * Reads process parameters.
	 *
	 * Parameters:
	 * - jettyPath: Optional custom path for the Jetty server directory.
	 * - Prefix: Prefix used to identify the theme files (e.g., prefix.idempiere.html).
	 */
	@Override
	protected void prepare() {
	    for (ProcessInfoParameter para : getParameter()) {
	        String name = para.getParameterName();
	        if ("jettyPath".equals(name)) {
	            p_jettyPath = para.getParameterAsString();
	        } else if ("Prefix".equals(name)) {
	            p_theme_prefix = para.getParameterAsString();
	        } else {
	            MProcessPara.validateUnknownParameter(getProcessInfo().getAD_Process_ID(), para);
	        }
	    }		
	}

	/**
	 * Locates the iDempiere home directory, determines the Jetty path based on the current build,
	 * copies attached theme files to their respective destinations, modifies home.properties, and extracts template ZIP files.
	 *
	 * @return Success message indicating process completion.
	 * @throws Exception if source or destination paths are invalid, or if file operations fail.
	 */
	@Override
	protected String doIt() throws Exception {
        String idempiereHome = Ini.findAdempiereHome();
        String server = "";
        String idempierePort = "";
        String idempiereCompilation = "";
        String jettyPath ="";
        String bmlaurusPath ="";
        String templatePath = "file://";
        String osName = System.getProperty("os.name").toLowerCase();
        
        String fileHtml        = p_theme_prefix + ".idempiere.html";
        String fileJsp         = p_theme_prefix + ".idempiere.jsp";
        String fileHomeTmpl    = p_theme_prefix + ".home.properties.template";
        String fileZip         = p_theme_prefix + ".template.zip";

        
        if (osName.contains("windows")) {
        	templatePath="file:///";
        }
        if (idempiereHome != null) {
	        String propertiesPath = idempiereHome + "/idempiereEnv.properties";
	
	        try (FileInputStream propertiesFile = new FileInputStream(propertiesPath)) {
	            Properties properties = new Properties();
	            properties.load(propertiesFile);
	            idempierePort = properties.getProperty("ADEMPIERE_SSL_PORT");
	            server = properties.getProperty("ADEMPIERE_APPS_SERVER");
	            server = server.replace('.', '_');
	        } catch (IOException e) {
	            e.printStackTrace();
	        }        
	        MSystem sy = MSystem.get(Env.getCtx());
	        idempiereCompilation = sy.getLastBuildInfo();
	        //idempiereCompilation = idempiereCompilation.replace('.', '_');        
	        /*jettyPath = idempiereHome
	        		.concat("/jettyhome/work/jetty-")
	        		.concat(server)
	        		.concat("-")
	        		.concat(idempierePort)
	        		.concat("-org_adempiere_server_")
	        		.concat(idempiereCompilation)
	        		.concat("_jar-_-any-/webapp/");
	        
	        if(p_jettyPath!=null)
		        if(p_jettyPath.length()>0)
		        	jettyPath=idempiereHome.concat(p_jettyPath);*/

	        //On Idempiere 12 the route was changed
	        jettyPath = idempiereHome
	        		.concat("/plugins/")
	        		.concat("org.adempiere.server_")
	        		.concat(idempiereCompilation);
	        bmlaurusPath = jettyPath.concat("/org/bmlaurus/home");
        }
        else
        	return null;
        
        MProcess process = MProcess.get(this.getProcessInfo().getAD_Process_ID());
        MAttachment att = process.getAttachment();
        MAttachmentEntry[] entries = att.getEntries();
        for (MAttachmentEntry entry : entries) {
        	if (entry.getName().equalsIgnoreCase(fileHtml)
        			 || entry.getName().equalsIgnoreCase(fileJsp)) {
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		//Path destino = Paths.get(jettyPath+"/"+entry.getName());
        		Path destino = Paths.get(jettyPath + "/" + entry.getName().replace(p_theme_prefix + ".", ""));

        		if(!Files.exists(origen))
        			throw new AdempiereException("Ruta de Origen no existe: "+origen);
        		if(!Files.exists(destino))
        			throw new AdempiereException("Ruta de Destino no existe: "+destino);
        		Files.copy(origen, destino,StandardCopyOption.REPLACE_EXISTING);
        	}
        	else if(entry.getName().compareToIgnoreCase("home.properties")==0) {
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		Path destino = Paths.get(bmlaurusPath+"/"+entry.getName());
        		Files.copy(origen, destino,StandardCopyOption.REPLACE_EXISTING);
        	}
        	//else if(entry.getName().compareToIgnoreCase("home.properties.template")==0) {
        	else if (entry.getName().equalsIgnoreCase(fileHomeTmpl)) {
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		Path destino = Paths.get(idempiereHome + "/home.properties");

        		Files.copy(origen, destino,StandardCopyOption.REPLACE_EXISTING);
        		//leo el archivo para cambiar ruta del template path
    	        try (FileInputStream propertiesFile = new FileInputStream(destino.toString())) {
    	            Properties properties = new Properties();
    	            properties.load(propertiesFile);
    	            templatePath = templatePath.concat(idempiereHome).concat("/lirionTemplate/");
    	            properties.setProperty("TemplatePath", templatePath);
    	            
    	            // Guardar los cambios en el archivo de propiedades
    	            try (FileOutputStream outputStream = new FileOutputStream(destino.toString())) {
    	                properties.store(outputStream, "Archivo de propiedades editado");
    	                System.out.println("Cambios guardados exitosamente.");
    	            } catch (IOException e) {
    	                e.printStackTrace();
    	            }
    	            
    	        } catch (IOException e) {
    	            e.printStackTrace();
    	        }     
        	}
        	//else if(entry.getName().compareToIgnoreCase("lirionTemplate.zip")==0) {
        	else if (entry.getName().equalsIgnoreCase(fileZip)) {
        		//descomprimir y mover carpeta
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		/*Path destino = Paths.get(idempiereHome+"/"+entry.getName());
        		
                // Eliminar el directorio de destino si existe
                File destinoDir = new File(idempiereHome.concat("/lirionTemplate/"));*/
                
                Path destino = Paths.get(idempiereHome + "/" + p_theme_prefix + ".template.zip");
                File destinoDir = new File(idempiereHome + "/" + p_theme_prefix + "Template/");

                if (destinoDir.exists()) {
                    eliminarDirectorio(destinoDir);
                    log.warning("Borrado Directorio Template Anterior");
                }
                
                // Eliminar el archivo ZIP si existe
                File archivoZip = new File(idempiereHome+"/"+entry.getName());
                if (archivoZip.exists()) {
                	archivoZip.delete();
                	log.warning("Borrado Archivo Zip Anterior");
                }
        		
        		Files.copy(origen, destino,StandardCopyOption.REPLACE_EXISTING);
        		String directorioDestino = idempiereHome;
                // Crear el directorio de destino si no existe
                destinoDir = new File(directorioDestino);
                if (!destinoDir.exists()) {
                    //destinoDir.mkdirs();
                }
                try {
                	 // Abrir el archivo ZIP
                    FileInputStream fis = new FileInputStream(destino.toString());
                    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

                    // Iterar sobre las entradas del archivo ZIP
                    ZipEntry entrada;
                    while ((entrada = zis.getNextEntry()) != null) {
                        String nombreArchivo = entrada.getName();
                        File archivo = new File(directorioDestino, nombreArchivo);
                        
                        // Crear directorios si la entrada es un directorio
                        if (entrada.isDirectory()) {
                            archivo.mkdirs();
                        } else {
                            // Crear el archivo y escribir los datos
                            FileOutputStream fos = new FileOutputStream(archivo);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                            byte[] buffer = new byte[1024];
                            int longitud;
                            while ((longitud = zis.read(buffer)) != -1) {
                                bos.write(buffer, 0, longitud);
                            }
                            bos.close();
                        }
                    }
                    zis.close();
                    log.warning("Archivo ZIP descomprimido exitosamente.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        	}
        }
        return "Proceso Finalizado Exitosamente";
	}
	
    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param directorio the directory file to be deleted.
     */
    private static void eliminarDirectorio(File directorio) {
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    eliminarDirectorio(archivo);
                } else {
                    archivo.delete();
                }
            }
        }
        directorio.delete();
    }
}