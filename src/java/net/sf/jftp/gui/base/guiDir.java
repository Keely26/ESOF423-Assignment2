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
    private static final String deleteString = "rm";
    private static final String mkdirString = "mkdir";
    private static final String refreshString = "fresh";
    private static final String cdString = "cd";
    private static final String cmdString = "cmd";
    private static final String downloadString = "<-";
    private static final String uploadString = "->";
    static final String rnString = "rn";
    private static final String cdUpString = "cdUp";

    HImageButton deleteButton;
    HImageButton mkdirButton;
    HImageButton cmdButton;
    HImageButton refreshButton;
    HImageButton cdButton;
    HImageButton uploadButton;
    HImageButton cdUpButton;
    HImageButton rnButton;

    public DirCanvas label = new DirCanvas(this);
    boolean pathChanged = true;
    boolean firstGui = true;
    public JPanel p = new JPanel();
    JToolBar buttonPanel = new JToolBar() {
        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    private JToolBar currDirPanel = new JToolBar() {
        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    DefaultListModel jlm;
    private JScrollPane jsp = new JScrollPane(jl);
    int tmpindex = -1;
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem props = new JMenuItem("Properties");
    DirEntry currentPopup = null;
    String sortMode = null;
    private String[] sortTypes = new String[]{"Normal", "Reverse", "Size", "Size/Re"};
    JComboBox<String> sorter = new JComboBox<>(sortTypes);
    boolean dateEnabled = false;

    // --- RemoteDir ----------------------------------------------------------------
    private static final String queueString = "que";
    HImageButton downloadButton;
    HImageButton queueButton;

    public HImageButton list = new HImageButton(Settings.listImage, "list",
            "Show remote listing...", (ActionListener) this);
    HImageButton transferType = new HImageButton(Settings.typeImage,
            "type",
            "Toggle transfer type...",
            (ActionListener) this);
    // ------------------------------------------------------------------------------

    // --- LocalDir -----------------------------------------------------------------
    private static final String zipString = "zip";
    private static final String cpString = "cp";
    HImageButton zipButton;
    HImageButton cpButton;
    JMenuItem runFile = new JMenuItem("Launch file");
    JMenuItem viewFile = new JMenuItem("View file");
    // ------------------------------------------------------------------------------


    /**
     *
     */
    void guiInit(FlowLayout f) {
        currDirPanel.setFloatable(false);
        buttonPanel.setFloatable(false);
        MouseListener mouseListener = mouseListenerInit();
        AdjustmentListener adjustmentListener = adjustmentListenerInit();
        f.setHgap(1);
        f.setVgap(2);
        buttonPanelInit(f);
        jScrollPanelInit(mouseListener, adjustmentListener);
    }

    /**
     * Defines size, margins, and location of button panel
     */
    private void buttonPanelInit(FlowLayout f) {
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

    /**
     * Defines all button declarations including the button label, the image displayed
     * and tool tip text.
     */
    private void buttonDeclarations() {
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

    /**
     * Handles functionality allowing users to scroll through table
     */
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

    /**
     * Defines a new mouse listener
     */
    private MouseListener mouseListenerInit() {
        return new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (JFtp.uiBlocked) return;

                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int index = jl.getSelectedIndex() - 1;
                    if (index < -1) return;
                    if (index < 0 || (dirEntry == null) || (dirEntry.length < index) ||
                            (dirEntry[index] == null)) {
                    } else {
                        currentPopup = dirEntry[index];
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            public void mouseClicked(MouseEvent e) {
                if (JFtp.uiBlocked) return;

                TableUtils.copyTableSelectionsToJList(jl, table);
                if (e.getClickCount() == 2) {
                    int index = jl.getSelectedIndex() - 1;

                    if (index < -1) return;                                     // mousewheel bug fix

                    String tgt = jl.getSelectedValue().toString();

                    if (index < 0) {
                        doChdir(path + tgt);
                    } else if ((dirEntry == null) || (dirEntry.length < index) ||
                            (dirEntry[index] == null)) {
                    } else if (dirEntry[index].isDirectory()) {
                        doChdir(path + tgt);
                    } else if (dirEntry[index].isLink()) {
                        if (!con.chdir(path + tgt)) {
                            showContentWindow(path +
                                            dirEntry[index].toString(),
                                    dirEntry[index]);
                        }
                    } else {
                        showContentWindow(path + dirEntry[index].toString(),
                                dirEntry[index]);
                    }
                }
            }
        };
    }

    /**
     * Defines a new adjustment listener
     */
    private AdjustmentListener adjustmentListenerInit() {
        return e -> {
            jsp.repaint();
            jsp.revalidate();
        };
    }

    public void doChdir(String path) {
    }

    public void showContentWindow(String url, DirEntry d) {
    }
}