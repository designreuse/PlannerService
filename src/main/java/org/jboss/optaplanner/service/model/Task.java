package org.jboss.optaplanner.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@Entity
@XmlRootElement
public class Task implements Serializable {

	private static final long serialVersionUID = 1725713460693515509L;

	@Id
	private Long id = null;

	@NotNull
	private String source;

	private int progress = 0;
	private long eta = Long.MAX_VALUE;
	
	private TaskStatus status = TaskStatus.NEW;

	@XmlTransient
	public Long getId() {
		return id;
	}

	@XmlElement(name = "id")
	public String getIdAsString() {
		return id.toString();
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public long getEta() {
		return eta;
	}

	public void setEta(long eta) {
		this.eta = eta;
	}

}