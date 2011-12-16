/*  Copyright (C) 2011  Nicholas Wright
	
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

import javax.swing.JPanel;
import javax.swing.JFrame;

import filter.FilterModifiable;

import java.awt.List;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.Button;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Displays lists of filtered keywords, and lets the user edit them.
 */
public class Filterlist extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private List lstFileName = null;
	private List lstPostContent = null;
	private TextField editBox = null;
	private Button btnAddFile = null;
	private Button btnRemove = null;
	private Label lblPostCont = null;
	private Label lblFileName = null;
	private Button btnAddPost = null;
	private FilterModifiable filter;

	/**
	 * Creates a new Frame that can be used to manage
	 * the filters.
	 * @param filter
	 */
	public Filterlist(FilterModifiable filter) {
		super();
		this.filter = filter;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(373, 294);
		this.setLocation(600, 100);
		this.setContentPane(getJContentPane());
		this.setTitle("FilterList");
		refreshLists();
		refreshLists();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblFileName = new Label();
			lblFileName.setBounds(new Rectangle(15, 6, 151, 20));
			lblFileName.setText("Filename");
			lblPostCont = new Label();
			lblPostCont.setBounds(new Rectangle(188, 6, 151, 20));
			lblPostCont.setText("Post content");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getLstFileName(), null);
			jContentPane.add(getLstPostContent(), null);
			jContentPane.add(getEditBox(), null);
			jContentPane.add(getBtnAddFile(), null);
			jContentPane.add(getBtnRemove(), null);
			jContentPane.add(lblPostCont, null);
			jContentPane.add(lblFileName, null);
			jContentPane.add(getBtnAddPost(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes lstFileName	
	 * 	
	 * @return java.awt.List	
	 */
	private List getLstFileName() {
		if (lstFileName == null) {
			lstFileName = new List();
			lstFileName.setBounds(new Rectangle(15, 30, 151, 182));
			lstFileName.addActionListener(this);
		}
		return lstFileName;
	}

	/**
	 * This method initializes lstPostContent	
	 * 	
	 * @return java.awt.List	
	 */
	private List getLstPostContent() {
		if (lstPostContent == null) {
			lstPostContent = new List();
			lstPostContent.setBounds(new Rectangle(188, 30, 151, 182));
			lstPostContent.addActionListener(this);
		}
		return lstPostContent;
	}

	/**
	 * This method initializes editBox	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getEditBox() {
		if (editBox == null) {
			editBox = new TextField();
			editBox.setBounds(new Rectangle(16, 222, 151, 28));
		}
		return editBox;
	}

	/**
	 * This method initializes btnAddFile	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtnAddFile() {
		if (btnAddFile == null) {
			btnAddFile = new Button();
			btnAddFile.setBounds(new Rectangle(188, 222, 39, 28));
			btnAddFile.setLabel("Add");
			btnAddFile.addActionListener(this);
		}
		return btnAddFile;
	}

	/**
	 * This method initializes btnRemove	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtnRemove() {
		if (btnRemove == null) {
			btnRemove = new Button();
			btnRemove.setBounds(new Rectangle(270, 222, 69, 28));
			btnRemove.setLabel("Remove");
			btnRemove.addActionListener(this);
		}
		return btnRemove;
	}

	/**
	 * This method initializes btnAddPost	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtnAddPost() {
		if (btnAddPost == null) {
			btnAddPost = new Button();
			btnAddPost.setBounds(new Rectangle(229, 222, 39, 28));
			btnAddPost.setLabel("Add");
			btnAddPost.addActionListener(this);
		}
		return btnAddPost;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnAddFile)
			filter.addFileNameFilterItem(editBox.getText());
		
		if(e.getSource() == btnAddPost)
			filter.addPostContentFilterItem(editBox.getText());
		
		if(e.getSource() == btnRemove){
			filter.removeFileNameFilterItem(editBox.getText());
			filter.removePostContentFilterItem(editBox.getText());
		}
		
		if(e.getSource() == lstFileName || lstFileName.getSelectedItem() != null)
			editBox.setText(lstFileName.getSelectedItem());
		
		if(e.getSource() == lstPostContent || lstPostContent.getSelectedItem() != null)
			editBox.setText(lstPostContent.getSelectedItem());
		
		refreshLists();
	}
	
	private void refreshLists(){
		lstFileName.removeAll();
		lstPostContent.removeAll();
		
		for(String s : filter.getFileNameFilterItem())
			lstFileName.add(s);
		for(String s : filter.getPostContentFilterItem())
			lstPostContent.add(s);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
