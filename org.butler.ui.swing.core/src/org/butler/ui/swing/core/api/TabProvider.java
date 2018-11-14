package org.butler.ui.swing.core.api;

import javax.swing.Icon;
import javax.swing.JPanel;

public interface TabProvider {
	/**
	 *
	 * @return name of the tab
	 */
	public String getName();

	/**
	 *
	 * @return icon for the tab
	 */
	public Icon getIcon();

	/**
	 * Returns singleton JPane which contains the content of the tab.
	 *
	 * @return content pane of the tab
	 */
	public JPanel getContentPane();

	/**
	 *
	 * @return tool tip of the tab
	 */
	public String getToolTip();

	/**
	 *
	 * @return priority of the tab
	 */
	public Integer getPriority();
}
