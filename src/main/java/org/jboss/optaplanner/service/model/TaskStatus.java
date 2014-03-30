package org.jboss.optaplanner.service.model;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public enum TaskStatus {
  NEW, // new task entered to the system
  WAITING, // waiting in the queue for execution by a free executor
  IN_PROGRESS, // task being computed at the moment
  PAUSED, // paused computation 
  COMPLETE, // completed computation
  MODIFIED // moodified task, this is a new task created from either PAUSED or COMPLETE task 
}
