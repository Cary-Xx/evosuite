package regression.issues.testcase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import evosuite.shell.EvoTestResult;
import evosuite.shell.TempGlobalVariables;
import regression.objectconstruction.testcase.DebugSetup;
import sf100.CommonTestUtil;

public class TestIssue138 extends DebugSetup {

	@Test
	public void run138() {
		String projectId = "35_corina";
		String[] targetMethods = new String[] { "corina.map.PngEncoderB#pngEncode(Z)[B" };

		List<EvoTestResult> results0 = new ArrayList<EvoTestResult>();
		List<EvoTestResult> results1 = new ArrayList<EvoTestResult>();
		int repeatTime = 1;
		int budget = 10000;
//			Long seed = 1556814527153L;
		Long seed = null;

		String fitnessApproach = "fbranch";
		results0 = CommonTestUtil.evoTestSingleMethod(projectId, targetMethods, fitnessApproach, repeatTime, budget,
				true, seed);
		TempGlobalVariables.seeds = checkRandomSeeds(results0);

//			String fitnessApproach = "branch";
//			results1 = CommonTestUtil.evoTestSingleMethod(projectId,  
//					targetMethods, fitnessApproach, repeatTime, budget, true, seed);

		System.out.println("fbranch" + ":");
		printResult(results0);
//			System.out.println("branch" + ":");
//			printResult(results1);
	}
}
