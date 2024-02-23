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

import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MProcess;
import org.compiere.model.MSystem;
import org.compiere.util.Env;
import org.compiere.util.Ini;

import com.cdsoftware.lirionizer.base.CustomProcess;

@org.adempiere.base.annotation.Process
public class Lirionizer extends CustomProcess{
	
	
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String doIt() throws Exception {
        String idempiereHome = Ini.findAdempiereHome();
        String server = "";
        String idempierePort = "";
        String idempiereCompilation = "";
        String jettyPath ="";
        String bmlaurusPath ="";
        String templatePath = "file://";
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
	        idempiereCompilation = idempiereCompilation.replace('.', '_');        
	        jettyPath = idempiereHome
	        		.concat("/jettyhome/work/jetty-")
	        		.concat(server)
	        		.concat("-")
	        		.concat(idempierePort)
	        		.concat("-org_adempiere_server_")
	        		.concat(idempiereCompilation)
	        		.concat("_jar-_-any-/webapp/");
	        
	        bmlaurusPath = jettyPath.concat("org/bmlaurus/home");
        }
        else
        	return null;
        
        MProcess process = MProcess.get(this.getProcessInfo().getAD_Process_ID());
        MAttachment att = process.getAttachment();
        MAttachmentEntry[] entries = att.getEntries();
        for (MAttachmentEntry entry : entries) {
        	if(entry.getName().compareToIgnoreCase("idempiere.jsp")==0
        			|| entry.getName().compareToIgnoreCase("idempiere.html")==0) {
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		Path destino = Paths.get(jettyPath+"/"+entry.getName());
        		Files.copy(origen, destino,StandardCopyOption.REPLACE_EXISTING);
        	}
        	else if(entry.getName().compareToIgnoreCase("home.properties")==0) {
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		Path destino = Paths.get(bmlaurusPath+"/"+entry.getName());
        		Files.copy(origen, destino,StandardCopyOption.REPLACE_EXISTING);
        	}
        	else if(entry.getName().compareToIgnoreCase("home.properties.template")==0) {
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		Path destino = Paths.get(idempiereHome+"/home.properties");
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
        	else if(entry.getName().compareToIgnoreCase("lirionTemplate.zip")==0) {
        		//descomprimir y mover carpeta
        		File file = entry.getFile();
        		Path origen = Paths.get(file.getPath());
        		Path destino = Paths.get(idempiereHome+"/"+entry.getName());
        		
                // Eliminar el directorio de destino si existe
                File destinoDir = new File(idempiereHome.concat("/lirionTemplate/"));
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
	
    // Método para eliminar un directorio de manera recursiva
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