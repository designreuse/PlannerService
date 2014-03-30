package org.jboss.optaplanner.service.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.nqueens.domain.Column;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.domain.Queen;
import org.optaplanner.examples.nqueens.domain.Row;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.NativeFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.core.ReferenceByIdMarshallingStrategy;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@SuppressWarnings("rawtypes")
public class Util {
    
    public static void toXStream(Solution sol, File f) {
        XStream xs = getXStream(); 
        try {
            xs.toXML(sol, new FileWriter(f));
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    public static void toXStream(Solution sol, StringWriter sw) {
        XStream xs = getXStream(); 
        xs.toXML(sol, sw);
    }
    
    public static String toXml(Solution sol) {
    	StringWriter sw = new StringWriter();
    	toXStream(sol, sw);
    	sw.flush();
    	return sw.getBuffer().toString();
    }
    
    public static Solution fromXml(String xml) {
    	ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
    	return fromXStream(bais);
    }

    private static XStream getXStream() {
        XStream xs = new XStream(new PureJavaReflectionProvider(new FieldDictionary(new NativeFieldKeySorter())));
        xs.setMarshallingStrategy(new ReferenceByIdMarshallingStrategy());
        xs.processAnnotations(Column.class);
        xs.processAnnotations(Queen.class);
        xs.processAnnotations(Row.class);
        xs.processAnnotations(NQueens.class);
        return xs;
    }
    
    public static Solution fromXStream(InputStream s) {
        return (Solution)getXStream().fromXML(s);
        
    }
    
}
