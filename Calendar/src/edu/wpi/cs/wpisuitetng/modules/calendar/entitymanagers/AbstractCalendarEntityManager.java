/*******************************************************************************
 * Copyright (c) 2013 -- WPI Suite
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Seal Team 6
 ******************************************************************************/

package edu.wpi.cs.wpisuitetng.modules.calendar.entitymanagers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import edu.wpi.cs.wpisuitetng.Session;
import edu.wpi.cs.wpisuitetng.database.Data;
import edu.wpi.cs.wpisuitetng.exceptions.BadRequestException;
import edu.wpi.cs.wpisuitetng.exceptions.ConflictException;
import edu.wpi.cs.wpisuitetng.exceptions.NotFoundException;
import edu.wpi.cs.wpisuitetng.exceptions.NotImplementedException;
import edu.wpi.cs.wpisuitetng.exceptions.WPISuiteException;
import edu.wpi.cs.wpisuitetng.modules.EntityManager;
import edu.wpi.cs.wpisuitetng.modules.Model;
import edu.wpi.cs.wpisuitetng.modules.calendar.models.AbstractCalendarModel;

/**
 * A generic EntityManager implementation for Calendar objects (such as Commitments or Events).
 * 
 * Provides reasonable default implementations for EntityManager methods.
 *
 * @param <T> The Model which this CalendarEntityManager manages.
 */
public abstract class AbstractCalendarEntityManager<T extends AbstractCalendarModel> implements EntityManager<T> {

	//database
	private final Data db;
	
	protected Data getData(){
		return db;
	}
	
	//class of T
	private final Class<T> tClass;
	
	public AbstractCalendarEntityManager(Data db, Class<T> tClass) {
		this.db = db;
		this.tClass = tClass;
	}
	
	@Override
	public T makeEntity(Session s, String content)
			throws BadRequestException, ConflictException, WPISuiteException {
		
		//parse the JSON to get the T:
		final T newT = T.fromJSON(content, tClass);
		
		//Store it or throw an exception if storing it fails
		save(s, newT);
		
		//return the T we just stored
		return newT;
	}

	@Override
	public T[] getEntity(Session s, String id) throws NotFoundException,
			WPISuiteException {
		
		System.out.println(id);
		
		//There is a bug on this line, invalid id (keeps coming in as project)
		//Strings would work better with this. use this string to speed it up
		//UUID idAsUUID = UUID.fromString(id);
		
		//Try something like this:
		//T[] ts = db.retrieve(tClass, T.ID_FIELD_NAME, idAsUUID).toArray(new T[0]);
		//It allows us to catch exceptions because the cast is not used. (not super critical, but do it anyway)
		//@SuppressWarnings("unchecked")
		//T[] ts = (T[]) db.retrieve(tClass, T.ID_FIELD_NAME, idAsUUID).toArray();
		
		//START CHANGES HERE
		int iID = Integer.parseInt(id);
		
		@SuppressWarnings("unchecked")
		T[] ts = db.retrieve(tClass, T.ID_FIELD_NAME, iID).toArray((T[])Array.newInstance(tClass, 0));
		
		//if there are no ts in the array throw an exception
		if(ts.length < 1 || ts[0] == null) {
			throw new NotFoundException();
		}
		
		return ts;
	}

	@Override
	public T[] getAll(Session s) throws WPISuiteException {
		//Get all ts from the database.
		List<Model> ts;
		try {
			ts = db.retrieveAll(tClass.newInstance(), s.getProject());
		} catch (InstantiationException | IllegalAccessException e) {
			//This should not happen, but if somehow tClass.newInstance fails...
			throw new WPISuiteException("Failed to instantiate dummy object: " + e.getMessage());
		}
		
		//TODO remove personal events which do not belong to this user
		
		//return the list as an array
		return ts.toArray((T[]) Array.newInstance(tClass, 0));
	}

	@Override
	public T update(Session s, String content) throws WPISuiteException {
		
		//We need to update the fields one by one because that's all the Data class supports.
		//REFLECTION TO THE REFLESCUE
		
		//THIS IS HORRIBLE. db4o is entirely the wrong database to be using here.
		
		T jsonT = T.fromJSON(content, tClass);
		
		Field[] fields = tClass.getFields();
		
		for(Field f : fields){
			try {
				db.update(tClass, T.ID_FIELD_NAME, jsonT.getId(), f.getName(), f.get(jsonT));
			} catch(IllegalAccessException e) {
				throw new WPISuiteException("Illegal field access while updating entity!");
			}
		}
		return jsonT;
	}

	@Override
	public void save(Session s, T model) throws WPISuiteException {
		int biggestId = 0;
		for(T t : getAll(s)){
			biggestId = (t.getId() > biggestId) ? t.getId() : biggestId;
		}
		model.setId(biggestId+1); //set id to next id available
		if(!db.save(model, s.getProject())){
			throw new WPISuiteException("Could not save to database");
		} else System.out.println("Saved a model to the database: " + model.toJSON());
		//TODO remove preceding else once no longer required for testing
	}

	@Override
	public boolean deleteEntity(Session s, String id) throws WPISuiteException {
		T deleted = db.delete(getEntity(s, id)[0]);
		return deleted != null;
	}

	@Override
	public void deleteAll(Session s) throws WPISuiteException {
		try {
			db.deleteAll(tClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			//This should not happen, but if somehow tClass.newInstance fails...
			throw new WPISuiteException("Failed to instantiate dummy object: " + e.getMessage());
		}
	}

	@Override
	public int Count() throws WPISuiteException {
		try {
			return db.retrieveAll(tClass.newInstance()).size();
		} catch (InstantiationException | IllegalAccessException e) {
			//This should not happen, but if somehow tClass.newInstance fails...
			throw new WPISuiteException("Failed to instantiate dummy object: " + e.getMessage());
		}
	}
	
	@Override
	final public String advancedPut(Session s, String[] args, String content)
			throws WPISuiteException {
		throw new NotImplementedException();
	}

	@Override
	final public String advancedPost(Session s, String string, String content)
			throws WPISuiteException {
		throw new NotImplementedException();
	}

	@Override
	final public String advancedGet(Session s, String[] args)
			throws WPISuiteException {
		throw new NotImplementedException();
	}
}
