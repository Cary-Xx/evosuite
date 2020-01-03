/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.strategy;

import org.evosuite.ClientProcess;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.mosa.DynaMOSA;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testcase.factories.TestGenerationUtil;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test generation with MOSA
 * 
 * @author Annibale,Fitsum
 *
 */
public class MOSuiteStrategy extends TestGenerationStrategy {

	@SuppressWarnings("unchecked")
	@Override	
	public TestSuiteChromosome generateTests() {
		// Currently only LIPS uses its own Archive
		if (Properties.ALGORITHM == Properties.Algorithm.LIPS) {
			Properties.TEST_ARCHIVE = false;
		}

		// Set up search algorithm
		PropertiesSuiteGAFactory algorithmFactory = new PropertiesSuiteGAFactory();

		GeneticAlgorithm<TestSuiteChromosome> algorithm = algorithmFactory.getSearchAlgorithm();
		
		// Override chromosome factory
		// TODO handle this better by introducing generics
		ChromosomeFactory factory = new RandomLengthTestFactory();
		algorithm.setChromosomeFactory(factory);
		
		if(Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD)
			TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(algorithm);

		long startTime = System.currentTimeMillis() / 1000;

		// What's the search target
		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
		List<TestFitnessFunction> fitnessFunctions = new ArrayList<TestFitnessFunction>();
        for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
            fitnessFunctions.addAll(goalFactory.getCoverageGoals());
        }
		algorithm.addFitnessFunctions((List)fitnessFunctions);

		// if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
		algorithm.addListener(progressMonitor); // FIXME progressMonitor may cause
		// client hang if EvoSuite is
		// executed with -prefix!
		
//		List<TestFitnessFunction> goals = getGoals(true);
		LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Total number of test goals for {}: {}",
				Properties.ALGORITHM.name(), fitnessFunctions.size());
		
//		ga.setChromosomeFactory(getChromosomeFactory(fitnessFunctions.get(0))); // FIXME: just one fitness function?

//		if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
//			ga.addListener(progressMonitor); // FIXME progressMonitor may cause

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.STATEMENT) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.RHO) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.BRANCH) ||
				ArrayUtil.contains(Properties.CRITERION, Criterion.AMBIGUITY) ||
				ArrayUtil.contains(Properties.CRITERION, Criterion.FBRANCH))
			ExecutionTracer.enableTraceCalls();

		algorithm.resetStoppingConditions();
		
		TestSuiteChromosome testSuite = null;

		if (!(Properties.STOP_ZERO && fitnessFunctions.isEmpty()) || ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)) {
			// Perform search
			LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Using seed {}", Randomness.getSeed());
			LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Starting evolution");
			ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

			algorithm.generateSolution();
			
			List<TestChromosome> populationList = (List<TestChromosome>) (((DynaMOSA) algorithm).getPopulationList());
			int expProb = getExceptionTimes(populationList);
			
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter("D:\\ziheng\\SF100-clean-windows\\" + Properties.TARGET_METHOD + new Random().nextInt() + ".txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				writer.append(expProb + " ");
				writer.append(populationList.size() + " ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			testSuite = (TestSuiteChromosome) algorithm.getBestIndividual();
			if (testSuite.getTestChromosomes().isEmpty()) {
				LoggingUtils.getEvoLogger().warn(ClientProcess.getPrettyPrintIdentifier() + "Could not generate any test case");
			}
		} else {
			zeroFitness.setFinished();
			testSuite = new TestSuiteChromosome();
			for (FitnessFunction<?> ff : testSuite.getFitnessValues().keySet()) {
				testSuite.setCoverage(ff, 1.0);
			}
		}

//		long endTime = System.currentTimeMillis() / 1000;
		long endTime = Archive.getArchiveInstance().getFeasibleTime() == 0l ? startTime : Archive.getArchiveInstance().getFeasibleTime();

//		goals = getGoals(false); //recalculated now after the search, eg to handle exception fitness
//        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());
        
		// Newline after progress bar
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");
		
		String text = " statements, best individual has fitness: ";
		LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Search finished after "
				+ (endTime - startTime)
				+ "s and "
				+ algorithm.getAge()
				+ " generations, "
				+ MaxStatementsStoppingCondition.getNumExecutedStatements()
				+ text
				+ testSuite.getFitness());
		// Search is finished, send statistics
		sendExecutionStatistics();

		// We send the info about the total number of coverage goals/targets only after 
		// the end of the search. This is because the number of coverage targets may vary
		// when the criterion Properties.Criterion.EXCEPTION is used (exception coverage
		// goal are dynamically added when the generated tests trigger some exceptions
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, algorithm.getFitnessFunctions().size());
		
		
		int timeUsed = (int) (endTime - startTime);
		testSuite.setTimeUsed(timeUsed);
		
		return testSuite;
	}
	
	public int getExceptionTimes(List<TestChromosome> populationList) {
		int exceptionSize = 0;
		for (TestChromosome tc : (List<TestChromosome>) populationList) {
			TestCase test = tc.getTestCase();
			int targetMethodPosition = TestGenerationUtil.getTargetMethodPosition(test, test.size()-1);
			if (targetMethodPosition != -1) {
				if (tc.getLastExecutionResult().getFirstPositionOfThrownException() != null) {
					Integer exceptionPos = tc.getLastExecutionResult().getFirstPositionOfThrownException();
					if (exceptionPos <= targetMethodPosition) {
						exceptionSize ++;
					}
				}
			}

		}
		return exceptionSize;
	}
	
}
