package org.jboss.optaplanner.service.server;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.EntityManager;

import org.jboss.optaplanner.entities.Task;
import org.jboss.optaplanner.entities.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@WebService
@Stateless
public class OptaPlannerWebService {

	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:/queue/OptaPlanner")
	private Destination queue;
	private Connection connection;

	@Resource
	private EntityManager em;

	private final Logger log = LoggerFactory.getLogger(OptaPlannerWebService.class);

	@WebMethod
	public long createTask(String text) throws Exception {
		Task task = new Task();
		task.setXmlFile(text);
		em.persist(task);

		log.info(String.format("Stored new task with id %d.",
				task.getId()));

		return task.getId();
	}
	
	@WebMethod
	public void pauseTask(long id) throws Exception {
		Task task = em.find(Task.class, id);
		task.setStatus(TaskStatus.PAUSED);
		em.merge(task);
		
		log.info(String.format("Paused task with it %d.", task.getId()));
	}

	@WebMethod
	public void startTask(long id) throws Exception {
		try {
			Task task = em.find(Task.class, id);

			connection = connectionFactory.createConnection();
			connection.start();
			Session session = null;
			MessageProducer sender = null;

			try {
				session = connection.createSession(true,
						Session.AUTO_ACKNOWLEDGE);
				sender = session.createProducer(queue);
				sender.setDeliveryMode(DeliveryMode.PERSISTENT);

				TextMessage message = session.createTextMessage(Long
						.toString(id));
				sender.send(message);
				
				task.setStatus(TaskStatus.WAITING);
				em.merge(task);

				log.info(String.format("Task with id %d requested to be started.", id));
			} finally {
				try {
					if (sender != null) {
						sender.close();
					}
				} finally {
					if (session != null) {
						session.commit();
						session.close();
					}
				}
			}

		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

}
