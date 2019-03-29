package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
import java.util.Hashtable;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.dir.DirCanvas;
import net.sf.jftp.gui.base.dir.DirComponent;
import net.sf.jftp.gui.base.dir.DirEntry;
import net.sf.jftp.gui.base.dir.TableUtils;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class guiDir extends DirComponent {
    static final String deleteString = "rm";
    static final String mkdirString = "mkdir";
    static final String refreshString = "fresh";
    static final String cdString = "cd";
    static final String cmdString = "cmd";
    static final String downloadString = "<-";
    static final String uploadString = "->";
    static final String zipString = "zip";
    static final String queueString = "que";
    static final String cpString = "cp";
    static final String rnString = "rn";
    static final String cdUpString = "cdUp";
    HImageButton deleteButton;
    HImageButton mkdirButton;
    HImageButton cmdButton;
    HImageButton refreshButton;
    HImageButton cdButton;
    HImageButton uploadButton;
    HImageButton downloadButton;
    HImageButton queueButton;
    HImageButton cdUpButton;
    HImageButton zipButton;
    HImageButton cpButton;
    HImageButton rnButton;
    public DirCanvas label = new DirCanvas(this);
    public boolean pathChanged = true;
    public boolean firstGui = true;
    public int pos = 0;
    public JPanel p = new JPanel();
    public JToolBar buttonPanel = new JToolBar()
    {
        public Insets getInsets()
        {
            return new Insets(0, 0, 0, 0);
        }
    };

    public JToolBar currDirPanel = new JToolBar()
    {
        public Insets getInsets()
        {
            return new Insets(0, 0, 0, 0);
        }
    };

    public DefaultListModel jlm;
    public JScrollPane jsp = new JScrollPane(jl);
    public int tmpindex = -1;
    public Hashtable dummy = new Hashtable();
    public HImageButton list = new HImageButton(Settings.listImage, "list",
            "Show remote listing...", (ActionListener) this);
    public HImageButton transferType = new HImageButton(Settings.typeImage,
            "type",
            "Toggle transfer type...",
            (ActionListener) this);
    public JPopupMenu popupMenu = new JPopupMenu();
    public JMenuItem runFile = new JMenuItem("Launch file");
    public JMenuItem viewFile = new JMenuItem("View file");
    public JMenuItem props = new JMenuItem("Properties");
    public DirEntry currentPopup = null;
    public String sortMode = null;
    String[] sortTypes = new String[] { "Normal", "Reverse", "Size", "Size/Re" };
    public JComboBox sorter = new JComboBox(sortTypes);
    public boolean dateEnabled = false;

    guiDir() {
        popupMenu = new JPopupMenu();
        String[] sortTypes = new String[]{"Normal", "Reverse", "Size", "Size/Re"};
        sorter = new JComboBox<>(sortTypes);
    }

    public void guiInit() {
        setLayout(new BorderLayout());
        MouseListener mouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (JFtp.uiBlocked) {
                    return;
                }

                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int index = jl.getSelectedIndex() - 1;

                    if (index < -1) {
                        return;
                    }

                    String tgt = (String) jl.getSelectedValue().toString();

                    if (index < 0 || (dirEntry == null) || (dirEntry.length < index) ||
                            (dirEntry[index] == null)) {
                    } else {
                        currentPopup = dirEntry[index];
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            public void mouseClicked(MouseEvent e) {
                if (JFtp.uiBlocked) {
                    return;
                }

                TableUtils.copyTableSelectionsToJList(jl, table);

                //System.out.println("DirEntryListener::");
                if (e.getClickCount() == 2) {
                    //System.out.println("2xList selection: "+jl.getSelectedValue().toString());
                    int index = jl.getSelectedIndex() - 1;

                    // mousewheel bugfix, ui refresh bugfix
                    if (index < -1) {
                        return;
                    }

                    String tgt = (String) jl.getSelectedValue().toString();

                    //System.out.println("List selection: "+index);
                    if (index < 0) {
                        doChdir(path + tgt);
                    } else if ((dirEntry == null) || (dirEntry.length < index) ||
                            (dirEntry[index] == null)) {
                        return;
                    } else if (dirEntry[index].isDirectory()) {
                        doChdir(path + tgt);
                    } else {
                        showContentWindow(path + dirEntry[index].toString(),
                                dirEntry[index]);

                        //blockedTransfer(index);
                    }
                }
            }
        };
        AdjustmentListener adjustmentListener = e -> {
            jsp.repaint();
            jsp.revalidate();
        };
        buttonManager();
        JPanelManager();
        jspManager(adjustmentListener);
        table.getSelectionModel().addListSelectionListener(this);
        table.addMouseListener(mouseListener);
        TableUtils.tryToEnableRowSorting(table);

        if (Settings.IS_JAVA_1_6) buttonPanel.remove(sorter);

        setVisible(true);
    }


    private void buttonManager() {
        deleteButton = new HImageButton(Settings.deleteImage, deleteString,
                "Delete selected", (ActionListener) this);
        deleteButton.setToolTipText("Delete selected");

        mkdirButton = new HImageButton(Settings.mkdirImage, mkdirString,
                "Create a new directory", (ActionListener) this);
        mkdirButton.setToolTipText("Create directory");

        refreshButton = new HImageButton(Settings.refreshImage, refreshString,
                "Refresh current directory", (ActionListener) this);
        refreshButton.setToolTipText("Refresh directory");
        refreshButton.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.refreshImage2)));
        refreshButton.setRolloverEnabled(true);

        cdButton = new HImageButton(Settings.cdImage, cdString,
                "Change directory", (ActionListener) this);
        cdButton.setToolTipText("Change directory");

        uploadButton = new HImageButton(Settings.uploadImage, uploadString,
                "Upload selected", (ActionListener) this);
        uploadButton.setToolTipText("Upload selected");
        //uploadButton.setBackground(new Color(192,192,192));

        zipButton = new HImageButton(Settings.zipFileImage, zipString,
                "Add selected to new zip file", (ActionListener) this);
        zipButton.setToolTipText("Create zip");

        cpButton = new HImageButton(Settings.copyImage, cpString,
                "Copy selected files to another local dir",
                (ActionListener) this);
        cpButton.setToolTipText("Local copy selected");

        rnButton = new HImageButton(Settings.textFileImage, rnString,
                "Rename selected file or directory", (ActionListener) this);
        rnButton.setToolTipText("Rename selected");

        cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString,
                "Go to Parent Directory", (ActionListener) this);
        cdUpButton.setToolTipText("Go to Parent Directory");
        rnButton = new HImageButton(Settings.textFileImage, rnString,
                "Rename selected file or directory", (ActionListener) this);
        rnButton.setToolTipText("Rename selected");

        list.setToolTipText("Show remote listing...");
        transferType.setToolTipText("Toggle transfer type...");

        cmdButton = new HImageButton(Settings.cmdImage, cmdString,
                "Execute remote command", (ActionListener) this);
        cmdButton.setToolTipText("Execute remote command");

        downloadButton = new HImageButton(Settings.downloadImage,
                downloadString, "Download selected",
                (ActionListener) this);
        downloadButton.setToolTipText("Download selected");

        queueButton = new HImageButton(Settings.queueImage, queueString,
                "Queue selected", (ActionListener) this);
        queueButton.setToolTipText("Queue selected");

        cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString,
                "Go to Parent Directory", (ActionListener) this);
        cdUpButton.setToolTipText("Go to Parent Directory");

    }

    public void createButtonPanel() {
        buttonPanel.add(new JLabel("           "));
        buttonPanel.add(queueButton);

        buttonPanel.add(new JLabel("    "));

        buttonPanel.add(refreshButton);
        buttonPanel.add(new JLabel("  "));
        buttonPanel.add(rnButton);
        buttonPanel.add(mkdirButton);
        buttonPanel.add(cdButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cdUpButton);
        buttonPanel.add(new JLabel("  "));

        buttonPanel.add(cmdButton);
        buttonPanel.add(list);
        buttonPanel.add(transferType);

        buttonPanel.add(sorter);

        buttonPanel.setVisible(true);

        buttonPanel.setSize(getSize().width - 10, 32);

        //TODO: create new method accounting for changes to second button
        p.add("South", buttonPanel);

        label.setText("Filesystem: " + StringUtils.cutPath(path));

        buttonPanel.add(sorter);

        buttonPanel.add(new JLabel("  "));

        buttonPanel.add(refreshButton);
        buttonPanel.add(new JLabel("  "));

        buttonPanel.add(cpButton);
        buttonPanel.add(rnButton);
        buttonPanel.add(mkdirButton);

        buttonPanel.add(cdButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cdUpButton);
        buttonPanel.add(new JLabel("  "));

        buttonPanel.add(zipButton);
        buttonPanel.add(new JLabel("              "));

        buttonPanel.setVisible(true);

        buttonPanel.setSize(getSize().width - 10, 32);

        sorter.addActionListener((ActionListener) this);



    }


    private void JPanelManager() {
        currDirPanel.setFloatable(false);
        label.setSize(getSize().width - 10, 24); //set label width
        currDirPanel.add(label);
        currDirPanel.setSize(getSize().width - 10, 32);
        label.setSize(getSize().width - 20, 24);

        p.setLayout(new BorderLayout());
        p.add("North", currDirPanel);

    }

    private void jspManager(AdjustmentListener adjustmentListener){
        jsp = new JScrollPane(table);
        jsp.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
        jsp.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);

        jsp.setSize(getSize().width - 20, getSize().height - 72);
        add("Center", jsp);
        jsp.setVisible(true);

    }

    void flowLayoutInit(FlowLayout f, JMenuItem runFile, JMenuItem viewFile) {
        f.setHgap(1); // Sets the horizontal gap between components and between the components and the borders of the Container
        f.setVgap(2); // Sets the vertical gap between components and between the components and the borders of the Container

        buttonPanel.setLayout(f); // defines the container for the button panel
        buttonPanel.setMargin(new Insets(0, 0, 0, 0)); // sets the margin for the button panel view

        if (runFile != null && viewFile != null) {
            runFile.addActionListener((ActionListener) this); //adds an action listener to the runfile menu item
            viewFile.addActionListener((ActionListener) this); //adds an action listener to the viewfile menu item
            popupMenu.add(runFile); // adds the runfile to the J-popup-menu
            popupMenu.add(viewFile); // adds the viewfile to the J-popup-menu
        }
        props.addActionListener((ActionListener) this); // adds the properties to the J-popup-menu
        popupMenu.add(props); // adds the properties to the J-popup-menu
    }




    public void doChdir(String path) {

        JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        con.chdir(path);
        JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void showContentWindow(String url, DirEntry d) {
        try {
            if (d.getRawSize() > 200000) {
                Log.debug("File is too big - 200kb is the maximum, sorry.");

                return;
            }

            String path = JFtp.localDir.getPath();

            if (!path.endsWith("/")) {
                path = path + "/";
            }

            File file = new File(path + StringUtils.getFile(url));

            if (!(con instanceof FilesystemConnection)) {
                if (!file.exists()) {
                    con.download(url);
                } else {
                    Log.debug("\nRemote file must be downloaded to be viewed and\n" +
                            " you already have a local copy present, pleasen rename it\n" +
                            " and try again.");

                    return;
                }

                file = new File(JFtp.localDir.getPath() +
                        StringUtils.getFile(url));

                if (!file.exists()) {
                    Log.debug("File not found: " + JFtp.localDir.getPath() +
                            StringUtils.getFile(url));
                }
            } else {
                file = new File(getPath() + StringUtils.getFile(url));
            }

            HFrame f = new HFrame();
            f.setTitle(url);

            String fileUrl = ("file://" + (file.getAbsolutePath().startsWith("/") ? file.getAbsolutePath() : "/" + file.getAbsolutePath()));
            System.out.println(fileUrl);

            JEditorPane pane = new JEditorPane(fileUrl);


            if (!pane.getEditorKit().getContentType().equals("text/html") &&
                    !pane.getEditorKit().getContentType().equals("text/rtf")) {
                if (!pane.getEditorKit().getContentType().equals("text/plain")) {
                    Log.debug("Nothing to do with this filetype - use the buttons if you want to transfer files.");

                    return;
                }

                pane.setEditable(false);
            }

            JScrollPane jsp = new JScrollPane(pane);

            f.getContentPane().setLayout(new BorderLayout());
            f.getContentPane().add("Center", jsp);
            f.setModal(false);
            f.setLocation(100, 100);
            f.setSize(600, 400);

            //f.pack();
            f.show();

            dList.fresh();
            JFtp.localDir.getCon().removeFileOrDir(StringUtils.getFile(url));
            JFtp.localDir.fresh();
        } catch (Exception ex) {
            Log.debug("File error: " + ex);
        }
    }

}