/*
 * Created by JFormDesigner on Tue Mar 12 13:40:50 AEST 2019
 */

package ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.*;

/**
 * @author Craig White
 */
public class frmRunningAccounts extends JFrame {

    DefaultListModel<ProcessLink> model;
    ArrayList<ProcessLink> list;

    public frmRunningAccounts() {
        initComponents();
//        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - this.getWidth();
        int y = 30;
        this.setLocation(x, y);
        this.setVisible(true);

        lstAccounts.removeAll();
        list = new ArrayList<>();
        model = new javax.swing.DefaultListModel<>();
        lstAccounts.setModel(model);

        lstAccounts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Object selectedValue = lstAccounts.getSelectedValue();
                if (!(selectedValue instanceof ProcessLink))
                    return;
                ProcessLink process = (ProcessLink)selectedValue;
                process.BringToFocus();
            }
        });
    }

    public void addAccount(Process process, String text) {
        ProcessLink pl = new ProcessLink(process, text);
        list.add(pl);
        Collections.sort(list);
        resetModel();
    }
    void resetModel(){
        model.removeAllElements();
        for (ProcessLink x : list)
            model.addElement(x);
    }

//    public void sortList(JList list) {
//        ListModel model = list.getModel();
//        int n = model.getSize();
//        String[] data = new String[n];
//
//        for (int i=0; i<n; i++) {
//            data[i] = (String) model.getElementAt(i);
//        }
//        Arrays.sort(data);
//        list.setListData(data);
//    }
    public void removeAccount(Process process) {
        for (ProcessLink pl : Collections.list(model.elements())){
            if (pl.process != process)
                continue;
            model.removeElement(pl);
            list.remove(pl);
            return;
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Craig White
        scrollPane1 = new JScrollPane();
        lstAccounts = new JList();

        //======== this ========
        setAlwaysOnTop(true);
        setResizable(false);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(lstAccounts);
        }
        contentPane.add(scrollPane1);
        scrollPane1.setBounds(0, 0, 170, 405);

        { // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Craig White
    private JScrollPane scrollPane1;
    private JList lstAccounts;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
