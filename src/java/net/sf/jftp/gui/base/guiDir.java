package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.dir.DirCanvas;
import net.sf.jftp.gui.base.dir.DirComponent;
import net.sf.jftp.gui.base.dir.DirEntry;
import net.sf.jftp.gui.base.dir.TableUtils;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

public class guiDir extends DirComponent {
    static final String deleteString = "rm";
    static final String mkdirString = "mkdir";
    static final String refreshString = "fresh";
    static final String cdString = "cd";
    static final String cmdString = "cmd";
    static final String downloadString = "<-";
    static final String uploadString = "->";
    static final String rnString = "rn";
    static final String cdUpString = "cdUp";

    HImageButton deleteButton;
    HImageButton mkdirButton;
    HImageButton cmdButton;
    HImageButton refreshButton;
    HImageButton cdButton;
    HImageButton uploadButton;
    HImageButton cdUpButton;
    HImageButton rnButton;

    public DirCanvas label = new DirCanvas(this);
    public boolean pathChanged = true;
    public boolean firstGui = true;
    public int pos = 0;
    public JPanel p = new JPanel();
    public JToolBar buttonPanel = new JToolBar() {
        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    public JToolBar currDirPanel = new JToolBar() {
        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    public DefaultListModel jlm;
    public JScrollPane jsp = new JScrollPane(jl);
    public int tmpindex = -1;
    public JPopupMenu popupMenu = new JPopupMenu();
    public JMenuItem props = new JMenuItem("Properties");
    public DirEntry currentPopup = null;
    public String sortMode = null;
    String[] sortTypes = new String[]{"Normal", "Reverse", "Size", "Size/Re"};
    public JComboBox sorter = new JComboBox(sortTypes);
    //    HImageButton cdUpButton;
    public boolean dateEnabled = false;

    // --- RemoteDir ----------------------------------------------------------------
    static final String queueString = "que";
    HImageButton downloadButton;
    HImageButton queueButton;

    public HImageButton list = new HImageButton(Settings.listImage, "list",
            "Show remote listing...", (ActionListener) this);
    public HImageButton transferType = new HImageButton(Settings.typeImage,
            "type",
            "Toggle transfer type...",
            (ActionListener) this);
    // ------------------------------------------------------------------------------

    // --- LocalDir -----------------------------------------------------------------
    static final String zipString = "zip";
    static final String cpString = "cp";
    HImageButton zipButton;
    HImageButton cpButton;
    public Hashtable dummy = new Hashtable();
    public JMenuItem runFile = new JMenuItem("Launch file");
    public JMenuItem viewFile = new JMenuItem("View file");
    // ------------------------------------------------------------------------------


    public void guiInit(FlowLayout f) {
        currDirPanel.setFloatable(false);
        buttonPanel.setFloatable(false);
        MouseListener mouseListener = mouseListenerInit();
        AdjustmentListener adjustmentListener = adjustmentListenerInit();
        flowLayoutInit(f);
        buttonPanelInit(f);
        jScrollPanelInit(mouseListener, adjustmentListener);
    }

    public void buttonPanelInit(FlowLayout f) {
        buttonPanel.setLayout(f);
        buttonPanel.setMargin(new Insets(0, 0, 0, 0));
        buttonDeclarations();
        label.setSize(getSize().width - 10, 24);
        currDirPanel.add(label);
        currDirPanel.setSize(getSize().width - 10, 32);
        label.setSize(getSize().width - 20, 24);

        p.setLayout(new BorderLayout());
        p.add("North", currDirPanel);
    }

    public void buttonDeclarations() {
        deleteButton = new HImageButton(Settings.deleteImage, deleteString,
                "Delete  selected", (ActionListener) this);
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

        cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString,
                "Go to Parent Directory", (ActionListener) this);
        cdUpButton.setToolTipText("Go to Parent Directory");

        // --- RemoteDir ----------------------------------------------------------------
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

        // ------------------------------------------------------------------------------

        // --- LocalDir -----------------------------------------------------------------
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
        // ------------------------------------------------------------------------------
    }

    private void jScrollPanelInit(MouseListener mouseListener, AdjustmentListener adjustmentListener) {
        jsp = new JScrollPane(table);
        table.getSelectionModel().addListSelectionListener(this);
        table.addMouseListener(mouseListener);

        jsp.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
        jsp.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);


        jsp.setSize(getSize().width - 20, getSize().height - 72);
        add("Center", jsp);
        jsp.setVisible(true);

        TableUtils.tryToEnableRowSorting(table);

        setVisible(true);
    }

    void flowLayoutInit(FlowLayout f) {
        f.setHgap(1);
        f.setVgap(2);
    }

    private MouseListener mouseListenerInit() {
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

                    if (index < 0) {
                    } else if ((dirEntry == null) || (dirEntry.length < index) ||
                            (dirEntry[index] == null)) {
                        return;
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
        return mouseListener;
    }

    private AdjustmentListener adjustmentListenerInit() {
        AdjustmentListener adjustmentListener = new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                jsp.repaint();
                jsp.revalidate();
            }
        };
        return adjustmentListener;
    }

    public void doChdir(String path) { }

    public void showContentWindow(String url, DirEntry d) {
    }


}