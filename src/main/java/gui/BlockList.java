/*  Copyright (C) 2012  Nicholas Wright
	
	part of 'Aid', an imageboard downloader.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gui;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import filter.FilterItem;

public class BlockList extends JFrame implements ListSelectionListener,ActionListener{
	/**
	 * Class for displaying blocked items and allows the user to select how to handle them.
	 * Can load URL into the clipboard and fetch thumbnails from the database.
	 */
	private static final long serialVersionUID = 1L;
	
	private Dimension thumbPanelDim = new Dimension(700,500);

	private JButton btnAllow = new JButton("Allow");
	private JButton btnDeny = new JButton("Deny");
	private JButton btnUpdate = new JButton("Update");

	private JPanel panFilterControl = new JPanel();
	private JPanel panThumbs = new JPanel();
	private JList<FilterItem> lstFilter;
	private filter.Filter filter;
	private static Logger logger = LoggerFactory.getLogger(BlockList.class);
	private BlockListDataModel blockListModel;

	public BlockList(filter.Filter filter, BlockListDataModel blockListModel){
		this.blockListModel = blockListModel;
		this.filter = filter;
		setTitle("BlockList");
		setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE);
		setSize( 700, 800 );
		setVisible( false );

		lstFilter = new JList<>(blockListModel);

		panFilterControl.setLayout(new GridLayout());

		panFilterControl.add(btnAllow);
		panFilterControl.add(btnDeny);
		panFilterControl.add(btnUpdate);


		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbcList = new GridBagConstraints();
		GridBagConstraints gbcButtons = new GridBagConstraints();
		GridBagConstraints gbcThumbs = new GridBagConstraints();
		
		gbl.preferredLayoutSize(this);
		
//		gbcList.fill = GridBagConstraints.BOTH;
		gbcList.gridheight = 2;
		gbcList.gridwidth = GridBagConstraints.REMAINDER;
		gbcList.weighty = 1.0;
		gbcList.weightx = 1.0;

//		gbcButtons.fill = GridBagConstraints.BOTH;
		gbcButtons.gridheight = 1;
		gbcButtons.gridwidth = GridBagConstraints.REMAINDER;
		gbcButtons.weighty = 1.0;

		gbcThumbs.gridheight = 6;
		gbcThumbs.gridwidth = GridBagConstraints.REMAINDER;
		gbcThumbs.weighty = 1.0;

		panThumbs.setPreferredSize(thumbPanelDim);
		panThumbs.setSize(thumbPanelDim);

		panThumbs.setLayout(new FlowLayout());

		setLayout(gbl);
		JScrollPane scrlList = new JScrollPane(lstFilter);
		scrlList.setPreferredSize(new Dimension(300,200));
		scrlList.setMinimumSize(new Dimension(300,100));
		
		gbl.setConstraints(scrlList, gbcList);
		gbl.setConstraints(panFilterControl, gbcButtons);
		gbl.setConstraints(panThumbs, gbcThumbs);



		add(scrlList);
		add(panFilterControl);
		add(panThumbs);

		lstFilter.addListSelectionListener(this);

		btnAllow.addActionListener(this);
		btnDeny.addActionListener(this);
		btnUpdate.addActionListener(this);
		
		btnAllow.setEnabled(false);
		btnDeny.setEnabled(false);
		pack();
		revalidate();
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnAllow && lstFilter.getSelectedIndex() != -1){
			int selected = lstFilter.getSelectedIndex();
			filter.setAllow(blockListModel.getUrl(selected));
			blockListModel.remove(selected);
			checkButtons();
			setSelection(selected);
		}
		
		if(e.getSource() == btnDeny && lstFilter.getSelectedIndex() != -1){
			int selected = lstFilter.getSelectedIndex();
			filter.setDeny(blockListModel.getUrl(selected));
			blockListModel.remove(selected);
			checkButtons();
			setSelection(selected);
		}
		
		if(e.getSource() == btnUpdate){
			filter.refreshList();
		}
		
		if(e.getSource() == lstFilter){
			displayThumbs();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		checkButtons();
		displayThumbs();
	}
	
	private void displayThumbs(){
		if(lstFilter.getSelectedIndex() == -1)
			return;
			
		int selection = lstFilter.getSelectedIndex();
		try{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(blockListModel.getUrl(selection).toString()),null);
		}catch(IllegalStateException ise){
			logger.warn(ise.getMessage());
		}

		ArrayList<Image> images = filter.getThumbs(blockListModel.getUrl(selection).toString());
		panThumbs.removeAll();
		panThumbs.setLayout(new FlowLayout());
		panThumbs.setSize(thumbPanelDim);
		panThumbs.setPreferredSize(thumbPanelDim);


		for(Image i : images){
			if(i != null)
				panThumbs.add(new JLabel(new ImageIcon(i),JLabel.CENTER));
		}

		pack();
		panThumbs.validate();
		panThumbs.repaint();
	}
	
	private void clearThumbs(){
		panThumbs.removeAll(); // clear thumb display
		pack();
		panThumbs.validate();
		panThumbs.repaint();
	}
	
	private void checkButtons(){
		if(blockListModel.isEmpty()){
			btnAllow.setEnabled(false);
			btnDeny.setEnabled(false);
		}else{
			btnAllow.setEnabled(true);
			btnDeny.setEnabled(true);
		}
	}
	
	private void setSelection(int index){
		clearThumbs();
		if(! blockListModel.isEmpty()){
			if(index >= blockListModel.size()){
				lstFilter.setSelectedIndex(index-1);
			}else{
				lstFilter.setSelectedIndex(index);
			}
		}
	}
}
