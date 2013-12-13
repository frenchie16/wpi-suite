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

package edu.wpi.cs.wpisuitetng.modules.calendar;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import edu.wpi.cs.wpisuitetng.janeway.modules.AbstractJanewayModule;
import edu.wpi.cs.wpisuitetng.janeway.modules.IJanewayModule;
import edu.wpi.cs.wpisuitetng.janeway.modules.JanewayTabModel;
import edu.wpi.cs.wpisuitetng.modules.calendar.controllers.AddEventController;
import edu.wpi.cs.wpisuitetng.modules.calendar.controllers.AddEventRequestObserver;
import edu.wpi.cs.wpisuitetng.modules.calendar.controllers.GetEventController;
import edu.wpi.cs.wpisuitetng.modules.calendar.models.EventListModel;
import edu.wpi.cs.wpisuitetng.modules.calendar.views.MainView;
import edu.wpi.cs.wpisuitetng.modules.calendar.views.ToolbarView;

public class CalendarModule extends AbstractJanewayModule {
	
	List<JanewayTabModel> tabs;
	
	public CalendarModule() {
		
		tabs = new ArrayList<JanewayTabModel>();
		
		MainView mainView = new MainView();
		ToolbarView tbView = new ToolbarView();
		
		// Give the toolbar access to the Calendar and Tab Views
		tbView.getCalendar(mainView.getCalendar());
		tbView.getTabView(mainView.getTabView());
		
		// Also Give calendar access to the controllers so they can push an update from server to GUI - Craig
		GetEventController.getInstance().AssignCalendarView(mainView.getCalendar());
		AddEventController.getInstance().AssignCalendarView(mainView.getCalendar());
		
		
		
		
		// Create a JPanel to hold the toolbar
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.add(tbView);
		
		// Create a JPanel to hold the main contents of the tab
		JPanel mainPanel = new JPanel();
		//mainPanel.add(new JLabel("YOLOSWAG"));
		mainPanel.add(mainView);
		//mainPanel.setBorder(BorderFactory.createLineBorder(Color.green, 2));

		// Create a tab model that contains the toolbar panel and the main content panel
		JanewayTabModel tab1 = new JanewayTabModel(getName(), new ImageIcon(), toolbarPanel, mainPanel);

		// Add the tab to the list of tabs owned by this module
		tabs.add(tab1);
		
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Calendar";
	}

	@Override
	public List<JanewayTabModel> getTabs() {
		// TODO Auto-generated method stub
		return tabs;
	}
	
	@Override
	public void onLoginComplete(){
		EventListModel.getInstance().LoginSuccess();
		EventListModel.getInstance().getEvents();
	}

}
