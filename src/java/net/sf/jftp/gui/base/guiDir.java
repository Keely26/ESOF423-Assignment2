package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
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
    static final String rnString = "rn";
    static final String cdUpString = "cdUp";

    HImageButton deleteButton;
    HImageButton mkdirButton;
    HImageButton refreshButton;
    HImageButton cdButton;
    HImageButton rnButton;
    HImageButton cdUpButton;

    DirCanvas label = new DirCanvas(this);
    boolean pathChanged = true;
    boolean firstGui = true;
    JPanel p = new JPanel();

    JToolBar buttonPanel = new JToolBar() {
        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    JToolBar currDirPanel = new JToolBar() {
        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    DefaultListModel jlm;
    JScrollPane jsp = new JScrollPane(jl);
//    int tmpindex = -1;
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem props = new JMenuItem("Properties");
    DirEntry currentPopup = null;
    String sortMode = null;
    String[] sortTypes = new String[]{"Normal", "Reverse", "Size", "Size/Re"};
    JComboBox sorter = new JComboBox(sortTypes);
    boolean dateEnabled = false;

    public void guiInit() {
        setLayout(new BorderLayout());
        currDirPanel.setFloatable(false);
        buttonPanel.setFloatable(false);

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

        cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString,
                "Go to Parent Directory", (ActionListener) this);
        cdUpButton.setToolTipText("Go to Parent Directory");

        label.setSize(getSize().width - 10, 24);
        currDirPanel.add(label);
        currDirPanel.setSize(getSize().width - 10, 32);
        label.setSize(getSize().width - 20, 24);

        p.setLayout(new BorderLayout());
        p.add("North", currDirPanel);

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
        jsp = new JScrollPane(table);
        table.getSelectionModel().addListSelectionListener(this);
        table.addMouseListener(mouseListener);

        AdjustmentListener adjustmentListener = new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                jsp.repaint();
                jsp.revalidate();
            }
        };

        jsp.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
        jsp.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);


        jsp.setSize(getSize().width - 20, getSize().height - 72);
        add("Center", jsp);
        jsp.setVisible(true);

        TableUtils.tryToEnableRowSorting(table);

        if (Settings.IS_JAVA_1_6) {
            //sorter.setVisible(false);
            buttonPanel.remove(sorter);
        }

        setVisible(true);


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
