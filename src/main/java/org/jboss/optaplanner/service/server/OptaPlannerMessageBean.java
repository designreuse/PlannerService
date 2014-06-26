package org.jboss.optaplanner.service.server;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.optaplanner.entities.Task;
import org.jboss.optaplanner.entities.TaskStatus;
import org.jboss.optaplanner.service.solver.ProblemSolver;
import org.jboss.optaplanner.service.util.Util;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@MessageDriven(mappedName = "MatchesMessageBean", activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/queue/OptaPlanner") })
public class OptaPlannerMessageBean implements MessageListener {

	private final Logger log = LoggerFactory.getLogger(OptaPlannerMessageBean.class);

	@PersistenceContext
    private EntityManager em;

    private int computeProgress(int score) {
    	return Math.max(100 - (score / 3), 0);
    }
    
    private long computeEta(int score) {
    	return Math.round(340 - (340 * (computeProgress(score) / 100.0))); 
    }
    
	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			try {
				String msg = ((TextMessage) message).getText();
				Task task = em.find(Task.class, new Long(msg));
				
				try {
					Thread.sleep(500); // wait a little bit not to interfere with the WAITING status
				} catch (InterruptedException e1) {
					//do nothing
				} 

				if (task == null) {
					throw new EJBException(String.format("Task with id %s not found in the database.", msg));
				}
				
				task.setStatus(TaskStatus.IN_PROGRESS);
				em.merge(task);
				
				log.info(String.format("Processing a new tournament with id %s.", msg));

				int score = Integer.MIN_VALUE;
	
				ProblemSolver p = new ProblemSolver(task.getXmlFile(), task.getType().getConfiguration());
				p.execute();
				while (p.isRunning() && TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					} // do nothing

					if (p.getScore() != null) {
						score = ((SimpleScore) p.getScore()).getScore();
						if (log.isDebugEnabled()) {
							log.debug("Best score so far: " + score);
						}
					
						em.refresh(task);
					
						task.setETA(computeEta(score));
						task.setProgress(computeProgress(score));

						em.merge(task);
					}
				}
				p.stop();
				
				String result = Util.toXml(p.getBestSolution());

				task.setETA(0);
				task.setProgress(100);
				task.setXmlFile(result);
				task.setStatus(TaskStatus.COMPLETE);
				
				em.merge(task);

				if (log.isDebugEnabled()) {
					log.debug(String.format("Successfully finished processing tournament with id %s.", msg));
				}
			} catch (JMSException e) {
				throw new EJBException(e);
			}
		}
	}

}
