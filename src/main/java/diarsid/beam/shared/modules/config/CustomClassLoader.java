/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.shared.modules.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 *
 * @author Diarsid
 */
class CustomClassLoader extends ClassLoader {
    
    private final String[] saxApis = 
            {".XmlSax", "javax.xml", "w3c", "xml", "sax", ".xerces."};
    
    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }
    
    @Override
    public synchronized Class loadClass(String className) 
            throws ClassNotFoundException {
        
        if (isSaxApiClass(className)) {
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream input = getResourcesFor(className);)
                {
                    
                byte[] data = new byte[2048];
                int read = input.read(data);
                while(read != -1){
                    buffer.write(data, 0, read);
                    read = input.read(data);
                }

                byte[] classData = buffer.toByteArray();
                
                Class c = defineClass(
                        className, classData, 0, classData.length, null);                
                if (c == null) {
                    throw new NoClassDefFoundError("Class.defineClass() in " + 
                            this.getClass().getCanonicalName() + 
                            " failure.");
                }
                return c;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return super.loadClass(className, false);
        }
        return null;
    }
    
    private InputStream getResourcesFor(String className) {
        return  ClassLoader
                .getSystemResourceAsStream(className.replace(".", "/")+".class");
    }
    
    boolean isSaxApiClass(String className) {
        for (String saxApi : this.saxApis) {
            if (className.contains(saxApi)) {
                return true;
            }
        }
        return false;
    }
}
