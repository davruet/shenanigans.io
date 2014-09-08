package io.shenanigans.persistence;

public class PersistEntityEvent {
	
	
	public PersistEntityEvent(Object entity) {
		this.entity = entity;
	}
	
	public PersistEntityEvent(){
		
	}

	protected Object entity;
	
	public static void translate(PersistEntityEvent source, long id, PersistEntityEvent another){ // FIXME this should be static
		// FIXME - also, if we aren't doing a deep copy of the entity, are we doing something bad?
		source.entity = another.entity;
	}
}