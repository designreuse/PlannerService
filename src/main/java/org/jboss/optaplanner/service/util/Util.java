package org.jboss.optaplanner.service.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.NativeFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;
import org.optaplanner.examples.travelingtournament.domain.TravelingTournament;

import java.io.*;

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

	public static void toXStream(Solution sol, OutputStreamWriter osw) {
		XStream xs = getXStream();
		xs.toXML(sol, osw);
	}

	public static String toXml(Solution sol) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8")) {
			toXStream(sol, osw);
			return baos.toString("UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException("UTF-8 should have been supported: ", e);
		}
	}

	public static Solution fromXml(String xml) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
			return fromXStream(new InputStreamReader(bais, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 should have been supported: ", e);      
		}
	}

	private static XStream getXStream() {
		XStream xs = new XStream();
		xs.setMode(XStream.ID_REFERENCES);
		xs.processAnnotations(NQueens.class);
		xs.processAnnotations(CloudBalance.class);
		xs.processAnnotations(Schedule.class);
		xs.processAnnotations(TravelingTournament.class);
		return xs;
	}

	public static Solution fromXStream(InputStreamReader r) {
		return (Solution) getXStream().fromXML(r);

	}

}
