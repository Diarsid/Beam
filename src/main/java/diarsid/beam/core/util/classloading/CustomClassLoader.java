/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util.classloading;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import diarsid.beam.core.exceptions.ModuleInitializationException;


/**
 *
 * @author Diarsid
 */
public class CustomClassLoader extends ClassLoader {
    
    private final ClassResourceLoader resourceLoader;
    private final String[] saxApis = 
            {".XmlSax", "javax.xml", "w3c", "xml", "sax", ".xerces."};
    
    CustomClassLoader(ClassLoader parent, ClassResourceLoader resourceLoader) {
        super(parent);
        this.resourceLoader = resourceLoader;
    }
    
    public static CustomClassLoader getCustomLoader(ClassLoader parentLoader) {
        ClassResourceLoadStrategyProvider strategiesProvider = 
                new ClassResourceLoadStrategyProvider(
                        new SystemClassResourceLoadStrategy(),
                        new ModuleClassResourceLoadStrategy());
        ClassResourceLoader resourceLoader = new ClassResourceLoader(strategiesProvider);
        return new CustomClassLoader(parentLoader, resourceLoader);
    }
    
    @Override
    public synchronized Class loadClass(String className) 
            throws ClassNotFoundException {
        
        if ( this.isSaxApiClass(className) ) {
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream input = this.resourceLoader.getResourcesAsStream(className)) {
                    
                byte[] data = new byte[2048];
                int read = input.read(data);
                while( read != -1 ) {
                    buffer.write(data, 0, read);
                    read = input.read(data);
                }

                byte[] classData = buffer.toByteArray();
                
                Class c = defineClass(
                        className, classData, 0, classData.length, null);                
                if ( c == null ) {
                    throw new NoClassDefFoundError("Class.defineClass() in " + 
                            this.getClass().getCanonicalName() + 
                            " failure.");
                }
                return c;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassResourceLoadFailedException e) {
                throw new ModuleInitializationException(
                        "Dynamic SAX Parser classloading in config module failed.");
            }
        } else {
            return super.loadClass(className, false);
        }
        return null;
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
