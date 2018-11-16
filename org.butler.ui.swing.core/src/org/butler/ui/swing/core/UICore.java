package org.butler.ui.swing.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.butler.ui.swing.core.api.TabProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component
public class UICore {
	private JTabbedPane tabPane;
	private Comparator<TabProvider> tabByPriority = (t1, t2) -> (t1.getPriority().compareTo(t2.getPriority()));
	private List<TabProvider> tabProviders = new CopyOnWriteArrayList<>();

	@Activate
	protected void activate() {
		javax.swing.SwingUtilities.invokeLater(() -> {

			createAndShowUI();
		});
	}

	private void createAndShowUI() {
		setStyle();

		JFrame frame = new JFrame("Butler");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		tabPane = new JTabbedPane();
		tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		addTabs();

		frame.add(tabPane);

		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}

	private void setStyle() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			JFrame.setDefaultLookAndFeelDecorated(false);
		}
	}

	private void addTabs() {
		for (TabProvider tabProvider : tabProviders) {
			addTab(tabProvider);
		}
	}

	private void addTab(TabProvider tabProvider) {
		if (tabPane != null) {
			tabPane.addTab(tabProvider.getName(), tabProvider.getIcon(), tabProvider.getContentPane(),
					tabProvider.getToolTip());
		}
	}

	private void removeTabs() {
		for (TabProvider tabProvider : tabProviders) {
			removeTab(tabProvider);
		}
	}

	private void removeTab(TabProvider tabProvider) {
		JPanel content = tabProvider.getContentPane();
		if (content == null) {
			tabPane.remove(content);
		}
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	protected void addTabProvider(TabProvider tabProvider) {
		tabProviders.add(tabProvider);
		Collections.sort(tabProviders, tabByPriority);
		if (tabPane != null) {
			removeTabs();
			addTabs();
		}
	}

	protected void removeTabProvider(TabProvider tabProvider) {
		tabProviders.remove(tabProvider);
		if (tabPane != null) {
			removeTab(tabProvider);
		}
	}
}
