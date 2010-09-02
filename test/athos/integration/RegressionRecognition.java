package athos.integration;

import junit.framework.Assert;

import org.junit.Test;

import athos.listeners.mock.JUnitEventFactory;
import athos.listeners.mock.ResourceChangeEventFactory;

public class RegressionRecognition extends IntegrationTestBaseClass {

	@Test 
	public void regressionCategory1() throws Exception {
		
		// Unit test pass
		junitListener.sessionFinished(JUnitEventFactory.createPassingSession("TestFile1.java"));
		
		Assert.assertEquals(1, stream.getRecognizedEpisodes().size());
		Assert.assertEquals("[episode] regression 1", stream.getRecognizedEpisodes().get(0));
		
	}
	
	@Test 
	public void regressionCategory1_2() throws Exception {
		
		// Unit test pass
		junitListener.sessionFinished(JUnitEventFactory.createPassingSession("TestFile1.java"));
		junitListener.sessionFinished(JUnitEventFactory.createPassingSession("TestFile2.java"));
		
		Assert.assertEquals(2, stream.getRecognizedEpisodes().size());
		Assert.assertEquals("[episode] regression 1", stream.getRecognizedEpisodes().get(0));
		Assert.assertEquals("[episode] regression 1", stream.getRecognizedEpisodes().get(1));
		
	}
	
	@Test 
	public void regressionCategory2() throws Exception {
		
		// Compile error on test
		resourceListener.resourceChanged(ResourceChangeEventFactory.createBuildErrorEvent("TestFile.java", "error message"));

		// TODO [rule] its a strange case without an test edit after the compilation problem :-/
		// Unit test pass
		junitListener.sessionFinished(JUnitEventFactory.createPassingSession("TestFile.java"));
		
		Assert.assertEquals(2, stream.getRecognizedEpisodes().size());
		Assert.assertEquals("[episode] regression 2", stream.getRecognizedEpisodes().get(0));
		// TODO [rule] this second one was not considered by hongbings test
		Assert.assertEquals("[episode] regression 1", stream.getRecognizedEpisodes().get(1));
		
	}
	

	
}