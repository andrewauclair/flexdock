/*
 * Copyright (c) 2005 FlexDock Development Team. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE.
 */
package org.flexdock.perspective;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.event.hierarchy.DockingPortTracker;
import org.flexdock.docking.state.*;
import org.flexdock.docking.state.LayoutManager;
import org.flexdock.event.EventManager;
import org.flexdock.event.RegistrationEvent;
import org.flexdock.perspective.event.*;
import org.flexdock.perspective.persist.FilePersistenceHandler;
import org.flexdock.perspective.persist.PersistenceHandler;
import org.flexdock.perspective.persist.PerspectiveModel;
import org.flexdock.util.RootWindow;
import org.flexdock.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Mateusz Szczap
 */
public class PerspectiveManager implements LayoutManager {
	
	private static final String EMPTY_PERSPECTIVE = "PerspectiveManager.EMPTY_PERSPECTIVE";
	private static final String DEFAULT_PERSISTENCE_KEY_VALUE = "perspectiveFile.data";
	private static final PerspectiveManager SINGLETON = new PerspectiveManager();
	private static final DockingStateListener UPDATE_LISTENER = new DockingStateListener();
	
	private final HashMap<String, Perspective> perspectives = new HashMap<>();
	private PerspectiveFactory perspectiveFactory;
	private String defaultPerspective;
	private String currentPerspective;
	private PersistenceHandler persistHandler;
	private boolean restoreFloatingOnLoad;
	private String defaultPersistenceKey;
	
	static {
		initialize();
	}
	
	private static void initialize() {
		// TODO: Add logic to add and remove event handlers based on whether
		// the perspective manager is currently installed.  Right now, we're
		// just referencing DockingManager.class to ensure the class is properly
		// initialized before we add our event handlers.  This should be
		// called indirectly form within DockingManager, and we should have
		// uninstall capability as well.
		Class c = DockingManager.class;
		
		EventManager.addHandler(new RegistrationHandler());
		EventManager.addHandler(PerspectiveEventHandler.getInstance());
		EventManager.addHandler(new LayoutEventHandler());
		
		EventManager.addListener(UPDATE_LISTENER);
		
		String pKey = System.getProperty(DockingConstants.DEFAULT_PERSISTENCE_KEY);
		setPersistenceHandler(FilePersistenceHandler.createDefault(DEFAULT_PERSISTENCE_KEY_VALUE));
		getInstance().setDefaultPersistenceKey(pKey);
	}
	
	public static PerspectiveManager getInstance() {
		return SINGLETON;
	}
	
	public static void setFactory(PerspectiveFactory factory) {
		getInstance().perspectiveFactory = factory;
	}
	
	public static void setPersistenceHandler(PersistenceHandler handler) {
		getInstance().persistHandler = handler;
	}
	
	public static PersistenceHandler getPersistenceHandler() {
		return getInstance().persistHandler;
	}
	
	
	private PerspectiveManager() {
		setDefaultPerspective(EMPTY_PERSPECTIVE);
		loadPerspective(this.defaultPerspective, (DockingPort) null);
	}
	
	public void add(Perspective perspective) {
		add(perspective, false);
	}
	
	public void add(Perspective perspective, boolean isDefault) {
		if (perspective == null) {
			throw new NullPointerException("perspective cannot be null");
		}
		
		this.perspectives.put(perspective.getPersistentId(), perspective);
		if (isDefault) {
			setDefaultPerspective(perspective.getPersistentId());
		}
		
		EventManager.dispatch(new RegistrationEvent(perspective, this, true));
	}
	
	public void remove(String perspectiveId) {
		if (perspectiveId == null) {
			throw new NullPointerException("perspectiveId cannot be null");
		}
		
		Perspective perspective = getPerspective(perspectiveId);
		if (perspective == null) {
			return;
		}
		
		this.perspectives.remove(perspectiveId);
		
		//set defaultPerspective
		if (this.defaultPerspective.equals(perspectiveId)) {
			setDefaultPerspective(EMPTY_PERSPECTIVE);
		}
		
		EventManager.dispatch(new RegistrationEvent(perspective, this, false));
	}
	
	public Perspective getPerspective(String perspectiveId) {
		if (perspectiveId == null) {
			return null;
		}
		
		Perspective perspective = this.perspectives.get(perspectiveId);
		if (perspective == null) {
			perspective = createPerspective(perspectiveId);
			if (perspective != null) {
				add(perspective);
			}
		}
		return perspective;
	}
	
	private Perspective createPerspective(String perspectiveId) {
		if (EMPTY_PERSPECTIVE.equals(perspectiveId)) {
			return new Perspective(EMPTY_PERSPECTIVE, EMPTY_PERSPECTIVE) {
				public void load(DockingPort port, boolean defaultSetting) {
					// noop
				}
			};
		}
		
		Perspective p = null;
		
		if (perspectiveFactory != null) {
			p = perspectiveFactory.getPerspective(perspectiveId);
			
			//this code ensures that perspective factory create perspectives that return the correct id
			//otherwise a NPE appears extremely far away in the code during the first docking operation
			if (!p.getPersistentId().equals(perspectiveId)) {
				//TODO create a good exception for this
				throw new IllegalStateException("Factory created perspective does not match intended ID: " + perspectiveId);
			}
		}
		
		return p;
	}
	
	private Perspective[] getPerspectives() {
		synchronized (this.perspectives) {
			ArrayList<Perspective> list = new ArrayList<>(this.perspectives.values());
			return list.toArray(new Perspective[0]);
		}
	}
	
	public void addListener(PerspectiveListener perspectiveListener) {
		EventManager.addListener(perspectiveListener);
	}
	
	public void removeListener(PerspectiveListener perspectiveListener) {
		EventManager.removeListener(perspectiveListener);
	}
	
	public PerspectiveListener[] getPerspectiveListeners() {
		return PerspectiveEventHandler.getInstance().getListeners();
	}
	
	private void setDefaultPerspective(String perspectiveId) {
		this.defaultPerspective = perspectiveId;
	}
	
	public void setCurrentPerspective(String perspectiveId) {
		setCurrentPerspective(perspectiveId, false);
	}
	
	private String getCurrentPerspectiveName() {
		return this.currentPerspective;
	}
	
	private void setCurrentPerspectiveName(String name) {
		this.currentPerspective = "".equals(name) ? null : name;
	}
	
	public void setCurrentPerspective(String perspectiveId, boolean asDefault) {
		perspectiveId = perspectiveId == null ? this.defaultPerspective : perspectiveId;
		setCurrentPerspectiveName(perspectiveId);
		if (asDefault) {
			setDefaultPerspective(perspectiveId);
		}
	}
	
	public Perspective getDefaultPerspective() {
		return getPerspective(this.defaultPerspective);
	}
	
	public Perspective getCurrentPerspective() {
		return getPerspective(getCurrentPerspectiveName());
	}
	
	
	@Override
	public DockingState getDockingState(Dockable dockable) {
		return getCurrentPerspective().getDockingState(dockable);
	}
	
	@Override
	public DockingState getDockingState(String dockable) {
		return getCurrentPerspective().getDockingState(dockable);
	}
	
	public DockingState getDockingState(Dockable dockable, boolean load) {
		return getCurrentPerspective().getDockingState(dockable, load);
	}
	
	public DockingState getDockingState(String dockable, boolean load) {
		return getCurrentPerspective().getDockingState(dockable, load);
	}
	
	
	@Override
	public FloatManager getFloatManager() {
		return getCurrentPerspective().getLayout();
	}
	
	private void reset() {
		RootWindow[] windows = DockingManager.getDockingWindows();
		if (windows.length != 0) {
			reset(windows[0].getRootContainer());
		}
	}
	
	private void reset(Component window) {
		if (window == null) {
			reset();
		}
		else {
			DockingPort port = DockingManager.getRootDockingPort(window);
			reset(port);
		}
	}
	
	private void reset(DockingPort rootPort) {
		loadPerspectiveImpl(getCurrentPerspectiveName(), rootPort, true);
	}
	
	/**
	 * PerspectiveManager#getMainApplicationWindow returns the first
	 * window where #getOwner == null. This is especially a problem for apps with
	 * multiple frames. To display a perspective for a specified window
	 * it is highly recommended to use #reload(Window w) instead of #reload()
	 * which is the same as DockingManager#restoreLayout().
	 * You can use #restoreLayout when the application does not need multiple
	 * independent docking windows.
	 */
	public void reload(Window w) {
		reload(w, true);
	}
	
	// use to load parentless frames
	private void reload(Window w, boolean reset) {
		String current = getCurrentPerspectiveName();
		// if the current perspective is null, use the default value
		String key = current == null ? this.defaultPerspective : current;
		
		// null-out the current perspective name to force a reload
		// otherwise, the loadPerspective() call will short-circuit since
		// it'll detect that the requested perspective is already loaded.
		setCurrentPerspectiveName(null);
		
		DockingPort port = DockingManager.getRootDockingPort(w);
		Perspective[] perspectives = getPerspectives();
		for (Perspective perspective : perspectives) {
			String id = perspective.getPersistentId();
			if (!id.equals(EMPTY_PERSPECTIVE)) {
				//TODO reset layout, maybe there is a better way
				if (reset) {
					perspective.getLayout().setRestorationLayout(null);
					//p.unload();
					//p.reset(port);
				}
			}
		}
		loadPerspectiveImpl(key, port, reset);
		
		// if perspective load fails, then rollback the perspective name
		// to its previous value (instead of null)
		if (!Utilities.isEqual(getCurrentPerspectiveName(), key)) {
			setCurrentPerspectiveName(current);
		}
	}
	
	public void restore(Window w) throws IOException, PersistenceException {
		reload(w, true);
		load();
		reload(w, false);
        /*DockingPort port = DockingManager.getRootDockingPort(w);
        String current = getCurrentPerspectiveName();
        String key = current == null ? this.defaultPerspective : current;
        setCurrentPerspectiveName(null);
        loadPerspectiveImpl(key, port, false);
        if(!Utilities.isEqual(getCurrentPerspectiveName(), key))
          setCurrentPerspectiveName(current);*/
	}
	
	private void reload() {
		String current = getCurrentPerspectiveName();
		// if the current perspective is null, the use the default value
		String key = current == null ? this.defaultPerspective : current;
		// null-out the current perspective name to force a reload.
		// otherwise, the loadPerspective() call will short-circuit since
		// it'll detect that the requested perspective is already loaded.
		setCurrentPerspectiveName(null);
		// load the perspective
		loadPerspective(key);
		// if the perspective load failed, then rollback the perspective name
		// to its previous value (instead of null)
		if (!Utilities.isEqual(getCurrentPerspectiveName(), key)) {
			setCurrentPerspectiveName(current);
		}
	}
	
	public void loadPerspective() {
		loadPerspective(this.defaultPerspective);
	}
	
	public void loadPerspectiveAsDefault(String perspectiveId) {
		loadPerspectiveAsDefault(perspectiveId, false);
	}
	
	private void loadPerspectiveAsDefault(String perspectiveId, boolean reset) {
		if (perspectiveId != null) {
			setDefaultPerspective(perspectiveId);
		}
		loadPerspective(perspectiveId, reset);
	}
	
	public void loadPerspective(String perspectiveId) {
		loadPerspective(perspectiveId, false);
	}
	
	private void loadPerspective(String perspectiveId, boolean reset) {
		RootWindow window = getMainApplicationWindow();
		if (window != null) {
			loadPerspective(perspectiveId, window.getRootContainer(), reset);
			return;
		}
		
		DockingPort rootPort = findMainDockingPort();
		if (rootPort != null) {
			loadPerspective(perspectiveId, rootPort, reset);
		}
	}
	
	public void loadPerspective(String perspectiveId, Component window) {
		loadPerspective(perspectiveId, window, false);
	}
	
	private void loadPerspective(String perspectiveId, Component window, boolean reset) {
		if (window == null) {
			loadPerspective(perspectiveId, reset);
			return;
		}
		
		DockingPort port = DockingManager.getRootDockingPort(window);
		loadPerspective(perspectiveId, port, reset);
	}
	
	private void loadPerspective(String perspectiveId, DockingPort rootPort) {
		loadPerspective(perspectiveId, rootPort, false);
	}
	
	private void loadPerspective(String perspectiveId, DockingPort rootPort, boolean reset) {
		if (perspectiveId == null || perspectiveId.equals(getCurrentPerspectiveName())) {
			return;
		}
		loadPerspectiveImpl(perspectiveId, rootPort, reset);
	}
	
	private void loadPerspectiveImpl(String perspectiveId, final DockingPort rootPort, boolean reset) {
		if (perspectiveId == null) {
			return;
		}
		
		Perspective current = getCurrentPerspective();
		final Perspective perspective = getPerspective(perspectiveId);
		
		// remember the current layout state so we'll be able to
		// restore when we switch back
		if (current != null) {
			cacheLayoutState(current, rootPort);
			current.unload();
		}
		
		// if the new perspective isn't available, then we're done
		if (perspective == null) {
			return;
		}
		
		synchronized (this) {
			setCurrentPerspectiveName(perspectiveId);
			if (reset) {
				perspective.reset(rootPort);
				EventManager.dispatch(new PerspectiveEvent(perspective, current,
						PerspectiveEvent.RESET));
			}
			else {
				perspective.load(rootPort);
				EventManager.dispatch(new PerspectiveEvent(perspective, current,
						PerspectiveEvent.CHANGED));
			}
		}
		
		EventQueue.invokeLater(() -> cacheLayoutState(perspective, rootPort));
	}
	
	private void cacheLayoutState(Perspective p, DockingPort port) {
		if (p != null) {
			p.cacheLayoutState(port);
		}
	}
	
	
	@Override
	public LayoutNode createLayout(DockingPort port) {
		return LayoutBuilder.getInstance().createLayout(port);
	}
	
	@Override
	public boolean display(Dockable dockable) {
		return RestorationManager.getInstance().restore(dockable);
	}
	
	static void setDockingStateListening(boolean enabled) {
		UPDATE_LISTENER.setEnabled(enabled);
	}
	
	static boolean isDockingStateListening() {
		return UPDATE_LISTENER.isEnabled();
	}
	
	static void clear(DockingPort port) {
		if (port != null) {
			boolean currState = isDockingStateListening();
			setDockingStateListening(false);
			port.clear();
			setDockingStateListening(currState);
		}
	}
	
	static void updateDockingStates(final Dockable[] dockables) {
		if (dockables == null) {
			return;
		}
		
		EventQueue.invokeLater(() -> {
			for (Dockable dockable : dockables) {
				UPDATE_LISTENER.updateState(dockable);
			}
		});
	}
	
	@Override
	public synchronized boolean store() throws IOException, PersistenceException {
		return store(null);
	}
	
	@Override
	public synchronized boolean store(String persistenceKey) throws IOException, PersistenceException {
		if (this.persistHandler == null) {
			return false;
		}
		
		DockingPort rootPort = findMainDockingPort();
		cacheLayoutState(getCurrentPerspective(), rootPort);
		
		Perspective[] items = getPerspectives();
		for (int i = 0; i < items.length; i++) {
			items[i] = (Perspective) items[i].clone();
		}
		
		PerspectiveModel info = new PerspectiveModel(this.defaultPerspective, getCurrentPerspectiveName(), items);
		String pKey = persistenceKey == null ? this.defaultPersistenceKey : persistenceKey;
		return this.persistHandler.store(pKey, info);
	}
	
	@Override
	public synchronized boolean load() throws IOException, PersistenceException {
		return load(null);
	}
	
	@Override
	public synchronized boolean load(String persistenceKey) throws IOException, PersistenceException {
		if (this.persistHandler == null) {
			return false;
		}
		
		String pKey = persistenceKey == null ? this.defaultPersistenceKey : persistenceKey;
		PerspectiveModel info = this.persistHandler.load(pKey);
		if (info == null) {
			return false;
		}
		
		Perspective[] perspectives = info.getPerspectives();
		
		this.perspectives.clear();
		for (Perspective perspective : perspectives) {
			add(perspective);
		}
		setDefaultPerspective(info.getDefaultPerspective());
		setCurrentPerspectiveName(info.getCurrentPerspective());
		return true;
	}
	
	public static boolean isRestoreFloatingOnLoad() {
		return getInstance().restoreFloatingOnLoad;
	}
	
	public static void setRestoreFloatingOnLoad(boolean restoreFloatingOnLoad) {
		getInstance().restoreFloatingOnLoad = restoreFloatingOnLoad;
	}
	
	//FIXME returns wrong window (first found) for multiple frames
	private static RootWindow getMainApplicationWindow() {
		RootWindow[] windows = DockingManager.getDockingWindows();
		// if the DockingManager couldn't resolve any windows using the
		// standard mechanism, we can try our own custom search
		if (windows.length == 0) {
			windows = resolveDockingWindows();
		}
		
		// TODO: fix this code to keep track of the proper dialog owner
		RootWindow window = null;
		for (RootWindow window1 : windows) {
			window = window1;
			if (window.getOwner() == null) {
				break;
			}
		}
		return window;
	}
	
	private static RootWindow[] resolveDockingWindows() {
		// locate all the root dockingports
		Set rootPorts = DockingPortTracker.getRootDockingPorts();
		ArrayList<RootWindow> windows = new ArrayList<>(rootPorts.size());
		// for each dockingPort, resolve its root window
		for (Object rootPort : rootPorts) {
			DockingPort port = (DockingPort) rootPort;
			RootWindow window = RootWindow.getRootContainer((Component) port);
			if (window != null) {
				windows.add(window);
			}
		}
		return windows.toArray(new RootWindow[0]);
	}
	
	public static DockingPort getMainDockingPort() {
		RootWindow window = getMainApplicationWindow();
		return window == null ? null : DockingManager.getRootDockingPort(window.getRootContainer());
	}
	
	@Override
	public boolean restore(boolean loadFromStorage) throws IOException, PersistenceException {
		boolean loaded = !loadFromStorage || load();
		reload();
		return loaded;
	}
	
	@Override
	public String getDefaultPersistenceKey() {
		return this.defaultPersistenceKey;
	}
	
	@Override
	public void setDefaultPersistenceKey(String key) {
		this.defaultPersistenceKey = key;
	}
	
	private DockingPort findMainDockingPort() {
		Set rootPorts = DockingPortTracker.getRootDockingPorts();
		DockingPort rootPort = null;
		for (Object rootPort1 : rootPorts) {
			DockingPort port = (DockingPort) rootPort1;
			Window win = SwingUtilities.getWindowAncestor((Component) port);
			if (win instanceof Dialog) {
				continue;
			}
			
			rootPort = port;
			break;
		}
		return rootPort;
	}
}
