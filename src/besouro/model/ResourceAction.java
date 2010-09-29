package besouro.model;

import java.util.Date;

import org.eclipse.core.resources.IResource;

/**
 * Defines generic software development actions, which is an independent
 * activity taken by software developer. For instance, unit test creation,
 * invocation, file edit, refactoring etc are all actions in Zorro system.
 * 
 * @author Hongbing Kou
 */
public abstract class ResourceAction extends Action {
	
	private String resource;

	public ResourceAction(Date clock, String resourceName) {
		super(clock);
		this.resource = resourceName;
	}

	public String getResource() {
		return this.resource;
	}

	public String toString() {
		return this.resource;
	}

	public String getActionValue() {
		return this.resource;
	}
}
