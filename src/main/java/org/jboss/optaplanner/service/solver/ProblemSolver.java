package org.jboss.optaplanner.service.solver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.optaplanner.service.util.Util;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.XmlSolverFactory;
import org.optaplanner.core.impl.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@SuppressWarnings("rawtypes")
public final class ProblemSolver {

	private final String xmlInput;
	private final ExecutorService es = Executors.newCachedThreadPool();
	private final Execution e;
	private final Logger log = LoggerFactory.getLogger(ProblemSolver.class);

	private class Execution implements Runnable {

		private volatile long time = System.nanoTime();
		private Solver solver;
		private Solution bestSolution = null;

		private Solution getInitialSolution() {
			Solution sol = Util.fromXml(xmlInput);
			return sol;
		}

		@Override
		public void run() {
			final XmlSolverFactory configurer = new XmlSolverFactory();
			configurer.configure(this.getClass().getResourceAsStream("/org/optaplanner/examples/nqueens/solver/nqueensSolverConfig.xml"));
			solver = configurer.buildSolver();
			solver.setPlanningProblem(getInitialSolution());
			time = System.nanoTime();
			solver.solve();
		}

		public boolean isRunning() {
			return (solver == null) ? true : solver.isSolving();
		}

		public void stop() {
			solver.terminateEarly();
			time = System.nanoTime() - time;

			// report results
			log.info(String.format("Done in: %d second(s).%n", Math.max(1, Math.round(time / 1000 / 1000 / 1000))));
			bestSolution = solver.getBestSolution();
		}
	}

	public ProblemSolver(String xmlInput) {
		this.xmlInput = xmlInput;
		System.setProperty("drools.lrUnlinkingEnabled", "true");
		e = new Execution();
	}

	public void execute() {
		es.execute(e);
	}

	public void stop() {
		e.stop();
		es.shutdownNow();
	}

	public boolean isRunning() {
		return e.isRunning();
	}

	public Score getScore() {
		if (e.solver != null && e.solver.getBestSolution() != null) {
			return e.solver.getBestSolution().getScore();
		} else {
			return null;
		}
	}

	public Solution getBestSolution() {
		return e.bestSolution;
	}

}