/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meu.rir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Date;
import java.awt.Cursor;
import java.awt.Desktop;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.xml.sax.helpers.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import com.ibm.icu.text.Transliterator;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import javax.swing.Box;
import meu.config.Config;
import java.io.FileInputStream;
import java.io.FileWriter;
import static java.lang.Integer.valueOf;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.lang.Math;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import static org.apache.commons.lang3.Range.is;
import java.util.logging.FileHandler;
import java.util.Calendar;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

/**
 *
 * @author t.hristov
 */
public class RirToEuro extends javax.swing.JFrame {

    JFrame f;
    public Config config;
    public String taText = "";  // TextArea
    public String slText = "";  // StatusLabel
    public ArrayList<Asset> listAsset = null;
    public ArrayList<SupportHistory> listSupportHistory = null;
    public ArrayList<YearlyPlan> listYearlyPlan = null;
    public ArrayList<FEthernetInfrastructure> listFEthernetInfrastructure = null;
    public ArrayList<FHardwareAsset> listFHardwareAsset = null;
    public ArrayList<FSoftwareAsset> listFSoftwareAsset = null;
    public ArrayList<FSupportHistory> listFSupportHistory = null;
    public ArrayList<FYearlyPlan> listFYearlyPlan = null;
    public StringBuilder sbBefore = null;
    public StringBuilder sbAfter = null;
    public Date currentDate = null;
    public static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");  // ("d/M/yyyy")
    public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String urlLocalItop = Config.URL_LOCAL_ITOP.get();
    private final String urlLocalDaeuReports = Config.URL_LOCAL_DAEUREPORTS.get();
    private final String urlTestItop = Config.URL_TEST_ITOP.get();
    private final String urlTestDaeuReports = Config.URL_TEST_DAEUREPORTS.get();
    private final String urlProdItop = Config.URL_PROD_ITOP.get();
    private final String urlProdDaeuReports = Config.URL_PROD_DAEUREPORTS.get();
    private final String unmLocal = Config.UNM_LOCAL.get();
    private final String psdLocal = Config.PSD_LOCAL.get();
    private final String unmTest = Config.UNM_TEST.get();
    private final String psdTest = Config.PSD_TEST.get();
    private final String unmProd = Config.UNM_PROD.get();
    private final String psdProd = Config.PSD_PROD.get();
    private final String driverMariadb = Config.DRIVER_MARIADB.get();
    private static final Logger log = Logger.getLogger(RirToEuro.class.getName());
    private FileHandler fh = null;
    private final String basePathLog = Config.BASE_PATH_LOG.get();

    /**
     * Creates new form RirToEuro
     */
    public RirToEuro() {
        // +------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
        // | Host: 127.0.0.1 - Local	> | Превалутиране на DB: itop		> | Таблица: Asset (local_Itop_Asset_)                                              | getCurrencyItopAsset()				| updateItopAsset()				|
        // | (menuHost_Local)		  | (menuLocal_Itop)                      | Таблица: Support_history (local_Itop_SupportHistory_)                           | getCurrencyItopSupportHistory()			| updateItopSupportHistory()			|
        // | 				  |					  | Таблица: Yearly_plan (local_Itop_YearlyPlan_)                                   | getCurrencyItopYearlyPlan()			| updateItopYearlyPlan()			|
        // | 				  |				  	  | Проверка за връзка към БД (local_Itop_ConnectionChecking_)                      | 							| 						|
        // | 				  | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
        // | 				  | Превалутиране на DB: daeu_reports	> | Таблица: F_Ethernet_infrastructure (local_DaeuReports_FEthernetInfrastructure_) | getCurrencyDaeuReportsFEthernetInfrastructure()   | updateDaeuReportsFEthernetInfrastructure()	|
        // | 				  | (menuLocal_DaeuReports)		  | Таблица: F_HardwareAsset (local_DaeuReports_FHardwareAsset_)                    | getCurrencyDaeuReportsFHardwareAsset()		| updateDaeuReportsFHardwareAsset()		|
        // | 				  |					  | Таблица: F_SoftwareAsset (local_DaeuReports_FSoftwareAsset_)                    | getCurrencyDaeuReportsFSoftwareAsset()		| updateDaeuReportsFSoftwareAsset()		|
        // | 				  |					  | Таблица: F_Support_history (local_DaeuReports_FSupportHistory_)                 | getCurrencyDaeuReportsFSupportHistory()		| updateDaeuReportsFSupportHistory()		|
        // | 				  |					  | Таблица: F_Yearly_plan (local_DaeuReports_FYearlyPlan_)                         | getCurrencyDaeuReportsFYearlyPlan()		| updateDaeuReportsFYearlyPlan()		|
        // | 				  |				 	  | Проверка за връзка към БД (local_DaeuReports_ConnectionChecking_)               |							|						|
        // | ---------------------------- | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
        // | Host: XXX.XX.XXX.XX - Test	> | Превалутиране на DB: itop		> | Таблица: Asset (test_Itop_Asset_)                                               | getCurrencyItopAsset()				| updateItopAsset()				|
        // | (menuHost_Test)		  | (menuTest_Itop)			  | Таблица: Support_history (test_Itop_SupportHistory_)                            | getCurrencyItopSupportHistory()			| updateItopSupportHistory()			|
        // | 				  |					  | Таблица: Yearly_plan (test_Itop_YearlyPlan_)                                    | getCurrencyItopYearlyPlan()			| updateItopYearlyPlan()			|
        // | 				  |				  	  | Проверка за връзка към БД (test_Itop_ConnectionChecking_)                       | 							| 						|
        // | 				  | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
        // | 				  | Превалутиране на DB: daeu_reports	> | Таблица: F_Ethernet_infrastructure (test_DaeuReports_FEthernetInfrastructure_)  | getCurrencyDaeuReportsFEthernetInfrastructure()	| updateDaeuReportsFEthernetInfrastructure()	|
        // | 				  | (menuTest_DaeuReports)		  | Таблица: F_HardwareAsset (test_DaeuReports_FHardwareAsset_)                     | getCurrencyDaeuReportsFHardwareAsset()		| updateDaeuReportsFHardwareAsset()		|
        // | 				  |					  | Таблица: F_SoftwareAsset (test_DaeuReports_FSoftwareAsset_)                     | getCurrencyDaeuReportsFSoftwareAsset()		| updateDaeuReportsFSoftwareAsset()		|
        // | 				  |					  | Таблица: F_Support_history (test_DaeuReports_FSupportHistory_)                  | getCurrencyDaeuReportsFSupportHistory()		| updateDaeuReportsFSupportHistory()		|
        // | 				  |					  | Таблица: F_Yearly_plan (test_DaeuReports_FYearlyPlan_)                          | getCurrencyDaeuReportsFYearlyPlan()		| updateDaeuReportsFYearlyPlan()		|
        // | 				  |				 	  | Проверка за връзка към БД (test_DaeuReports_ConnectionChecking_)                |							|						|
        // | ---------------------------- | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
        // | Host: XXX.XX.XXX.XX - Prod> | Превалутиране на DB: itop		> | Таблица: Asset (prod_Itop_Asset_)                                               | getCurrencyItopAsset()				| updateItopAsset()				|
        // | (menuHost_Prod)		  | (menuProd_Itop)			  | Таблица: Support_history (prod_Itop_SupportHistory_)                            | getCurrencyItopSupportHistory()			| updateItopSupportHistory()			|
        // | 				  |					  | Таблица: Yearly_plan (prod_Itop_YearlyPlan_)                                    | getCurrencyItopYearlyPlan()			| updateItopYearlyPlan()			|
        // | 				  |				  	  | Проверка за връзка към БД (prod_Itop_ConnectionChecking_)                       | 							| 						|
        // | 				  | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
        // | 				  | Превалутиране на DB: daeu_reports	> | Таблица: F_Ethernet_infrastructure (prod_DaeuReports_FEthernetInfrastructure_)  | getCurrencyDaeuReportsFEthernetInfrastructure()	| updateDaeuReportsFEthernetInfrastructure()	|
        // | 				  | (menuProd_DaeuReports)		  | Таблица: F_HardwareAsset (prod_DaeuReports_FHardwareAsset_)                     | getCurrencyDaeuReportsFHardwareAsset()		| updateDaeuReportsFHardwareAsset()		|
        // | 				  |					  | Таблица: F_SoftwareAsset (prod_DaeuReports_FSoftwareAsset_)                     | getCurrencyDaeuReportsFSoftwareAsset()		| updateDaeuReportsFSoftwareAsset()		|
        // | 				  |					  | Таблица: F_Support_history (prod_DaeuReports_FSupportHistory_)                  | getCurrencyDaeuReportsFSupportHistory()		| updateDaeuReportsFSupportHistory()		|
        // | 				  |					  | Таблица: F_Yearly_plan (prod_DaeuReports_FYearlyPlan_)                          | getCurrencyDaeuReportsFYearlyPlan()		| updateDaeuReportsFYearlyPlan()		|
        // | 				  |				 	  | Проверка за връзка към БД (prod_DaeuReports_ConnectionChecking_)                |							|						|
        // +------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
        // 
        // String userName = "";
        // String password = "";
        // String url = "jdbc:mariadb://127.0.0.1:3306/itop";
        // String url = "jdbc:mariadb://127.0.0.1:3306/daeu_reports";
        // 
        // String driver = "org.mariadb.jdbc.Driver";

        initComponents();
        f = new JFrame();

        Calendar calendar;
        Date dDate = null;
        Date dCurrentDate = null;
        java.sql.Date sqlCurrentDate = null;
        String sCurrentDate = "";
        String sSqlCurrentDate = "";
        calendar = Calendar.getInstance();
        dDate = calendar.getTime();
        setCurrentDate(dDate);
        dCurrentDate = getCurrentDate();
        sCurrentDate = dateFormat.format(dCurrentDate);
        sqlCurrentDate = new java.sql.Date(dCurrentDate.getTime());
        sSqlCurrentDate = sqlCurrentDate.toString();

        SimpleDateFormat format_log = new SimpleDateFormat("yyyy_MM");
        try {
            log.setLevel(Level.ALL);
            fh = new FileHandler(basePathLog + "RIR_" + format_log.format(Calendar.getInstance().getTime()) + ".log", true);
        } catch (IOException | SecurityException e) {
            System.out.println("IOException: " + e.getMessage());
            log.log(Level.WARNING, "IOException: " + e.getMessage());
        }
        fh.setFormatter(new SimpleFormatter());
        fh.setLevel(Level.ALL);
        log.addHandler(fh);
        log.setUseParentHandlers(false);
        log.info("RIR: " + sCurrentDate + "");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        generalSebraScrollPane = new javax.swing.JScrollPane();
        gsTextArea = new javax.swing.JTextArea();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        sebraMenuBar = new javax.swing.JMenuBar();
        menuChoiceFile = new javax.swing.JMenu();
        menuHost_Local = new javax.swing.JMenu();
        menuLocal_Itop = new javax.swing.JMenu();
        local_Itop_Asset_ = new javax.swing.JMenuItem();
        local_Itop_SupportHistory_ = new javax.swing.JMenuItem();
        local_Itop_YearlyPlan_ = new javax.swing.JMenuItem();
        local_Itop_ConnectionChecking_ = new javax.swing.JMenuItem();
        menuLocal_DaeuReports = new javax.swing.JMenu();
        local_DaeuReports_FEthernetInfrastructure_ = new javax.swing.JMenuItem();
        local_DaeuReports_FHardwareAsset_ = new javax.swing.JMenuItem();
        local_DaeuReports_FSoftwareAsset_ = new javax.swing.JMenuItem();
        local_DaeuReports_FSupportHistory_ = new javax.swing.JMenuItem();
        local_DaeuReports_FYearlyPlan_ = new javax.swing.JMenuItem();
        local_DaeuReports_ConnectionChecking_ = new javax.swing.JMenuItem();
        menuHost_Test = new javax.swing.JMenu();
        menuTest_Itop = new javax.swing.JMenu();
        test_Itop_Asset_ = new javax.swing.JMenuItem();
        test_Itop_SupportHistory_ = new javax.swing.JMenuItem();
        test_Itop_YearlyPlan_ = new javax.swing.JMenuItem();
        test_Itop_ConnectionChecking_ = new javax.swing.JMenuItem();
        menuTest_DaeuReports = new javax.swing.JMenu();
        test_DaeuReports_FEthernetInfrastructure_ = new javax.swing.JMenuItem();
        test_DaeuReports_FHardwareAsset_ = new javax.swing.JMenuItem();
        test_DaeuReports_FSoftwareAsset_ = new javax.swing.JMenuItem();
        test_DaeuReports_FSupportHistory_ = new javax.swing.JMenuItem();
        test_DaeuReports_FYearlyPlan_ = new javax.swing.JMenuItem();
        test_DaeuReports_ConnectionChecking_ = new javax.swing.JMenuItem();
        menuHost_Prod = new javax.swing.JMenu();
        menuProd_Itop = new javax.swing.JMenu();
        prod_Itop_Asset_ = new javax.swing.JMenuItem();
        prod_Itop_SupportHistory_ = new javax.swing.JMenuItem();
        prod_Itop_YearlyPlan_ = new javax.swing.JMenuItem();
        prod_Itop_ConnectionChecking_ = new javax.swing.JMenuItem();
        menuProd_DaeuReports = new javax.swing.JMenu();
        prod_DaeuReports_FEthernetInfrastructure_ = new javax.swing.JMenuItem();
        prod_DaeuReports_FHardwareAsset_ = new javax.swing.JMenuItem();
        prod_DaeuReports_FSoftwareAsset_ = new javax.swing.JMenuItem();
        prod_DaeuReports_FSupportHistory_ = new javax.swing.JMenuItem();
        prod_DaeuReports_FYearlyPlan_ = new javax.swing.JMenuItem();
        prod_DaeuReports_ConnectionChecking_ = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        About = new javax.swing.JMenu();
        menuAbout = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Превалутиране на таблиците от ИС РИР");
        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(24, 24));
        setPreferredSize(new java.awt.Dimension(1000, 500));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        mainPanel.setPreferredSize(new java.awt.Dimension(400, 340));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        generalSebraScrollPane.setMaximumSize(null);
        generalSebraScrollPane.setMinimumSize(null);

        gsTextArea.setEditable(false);
        gsTextArea.setColumns(20);
        gsTextArea.setRows(5);
        gsTextArea.setMaximumSize(null);
        gsTextArea.setMinimumSize(null);
        gsTextArea.setName(""); // NOI18N
        generalSebraScrollPane.setViewportView(gsTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(generalSebraScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mainPanel, gridBagConstraints);

        statusPanel.setBackground(new java.awt.Color(250, 250, 250));
        statusPanel.setLayout(new java.awt.GridBagLayout());

        statusLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        statusLabel.setPreferredSize(new java.awt.Dimension(300, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        statusPanel.add(statusLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        getContentPane().add(statusPanel, gridBagConstraints);

        menuChoiceFile.setText("  Избор на таблица ");
        menuChoiceFile.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuChoiceFile.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        menuChoiceFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuChoiceFileMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                menuChoiceFileMousePressed(evt);
            }
        });

        menuHost_Local.setText("Host: 127.0.0.1 - Local");
        menuHost_Local.setToolTipText("");
        menuHost_Local.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuHost_Local.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        menuLocal_Itop.setText("Превалутиране на DB: itop");
        menuLocal_Itop.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuLocal_Itop.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        local_Itop_Asset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_Itop_Asset_.setText("Таблица: Asset");
        local_Itop_Asset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_Itop_Asset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_Itop_Asset_ActionPerformed(evt);
            }
        });
        menuLocal_Itop.add(local_Itop_Asset_);

        local_Itop_SupportHistory_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_Itop_SupportHistory_.setText("Таблица: Support_history");
        local_Itop_SupportHistory_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_Itop_SupportHistory_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_Itop_SupportHistory_ActionPerformed(evt);
            }
        });
        menuLocal_Itop.add(local_Itop_SupportHistory_);

        local_Itop_YearlyPlan_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_Itop_YearlyPlan_.setText("Таблица: Yearly_plan");
        local_Itop_YearlyPlan_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_Itop_YearlyPlan_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_Itop_YearlyPlan_ActionPerformed(evt);
            }
        });
        menuLocal_Itop.add(local_Itop_YearlyPlan_);

        local_Itop_ConnectionChecking_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_Itop_ConnectionChecking_.setText("Проверка за връзка към БД");
        local_Itop_ConnectionChecking_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_Itop_ConnectionChecking_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_Itop_ConnectionChecking_ActionPerformed(evt);
            }
        });
        menuLocal_Itop.add(local_Itop_ConnectionChecking_);

        menuHost_Local.add(menuLocal_Itop);

        menuLocal_DaeuReports.setText("Превалутиране на DB: daeu_reports");
        menuLocal_DaeuReports.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuLocal_DaeuReports.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        local_DaeuReports_FEthernetInfrastructure_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_DaeuReports_FEthernetInfrastructure_.setText("Таблица: F_Ethernet_infrastructure");
        local_DaeuReports_FEthernetInfrastructure_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_DaeuReports_FEthernetInfrastructure_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_DaeuReports_FEthernetInfrastructure_ActionPerformed(evt);
            }
        });
        menuLocal_DaeuReports.add(local_DaeuReports_FEthernetInfrastructure_);

        local_DaeuReports_FHardwareAsset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_DaeuReports_FHardwareAsset_.setText("Таблица: F_HardwareAsset");
        local_DaeuReports_FHardwareAsset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_DaeuReports_FHardwareAsset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_DaeuReports_FHardwareAsset_ActionPerformed(evt);
            }
        });
        menuLocal_DaeuReports.add(local_DaeuReports_FHardwareAsset_);

        local_DaeuReports_FSoftwareAsset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_DaeuReports_FSoftwareAsset_.setText("Таблица: F_SoftwareAsset");
        local_DaeuReports_FSoftwareAsset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_DaeuReports_FSoftwareAsset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_DaeuReports_FSoftwareAsset_ActionPerformed(evt);
            }
        });
        menuLocal_DaeuReports.add(local_DaeuReports_FSoftwareAsset_);

        local_DaeuReports_FSupportHistory_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_DaeuReports_FSupportHistory_.setText("Таблица: F_Support_history");
        local_DaeuReports_FSupportHistory_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_DaeuReports_FSupportHistory_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_DaeuReports_FSupportHistory_ActionPerformed(evt);
            }
        });
        menuLocal_DaeuReports.add(local_DaeuReports_FSupportHistory_);

        local_DaeuReports_FYearlyPlan_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_DaeuReports_FYearlyPlan_.setText("Таблица: F_Yearly_plan");
        local_DaeuReports_FYearlyPlan_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_DaeuReports_FYearlyPlan_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_DaeuReports_FYearlyPlan_ActionPerformed(evt);
            }
        });
        menuLocal_DaeuReports.add(local_DaeuReports_FYearlyPlan_);

        local_DaeuReports_ConnectionChecking_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        local_DaeuReports_ConnectionChecking_.setText("Проверка за връзка към БД");
        local_DaeuReports_ConnectionChecking_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        local_DaeuReports_ConnectionChecking_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_DaeuReports_ConnectionChecking_ActionPerformed(evt);
            }
        });
        menuLocal_DaeuReports.add(local_DaeuReports_ConnectionChecking_);

        menuHost_Local.add(menuLocal_DaeuReports);

        menuChoiceFile.add(menuHost_Local);

        menuHost_Test.setText("Host: XXX.XX.XXX.XX - Test");
        menuHost_Test.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuHost_Test.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        menuTest_Itop.setText("Превалутиране на DB: itop");
        menuTest_Itop.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuTest_Itop.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        test_Itop_Asset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_Itop_Asset_.setText("Таблица: Asset");
        test_Itop_Asset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_Itop_Asset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_Itop_Asset_ActionPerformed(evt);
            }
        });
        menuTest_Itop.add(test_Itop_Asset_);

        test_Itop_SupportHistory_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_Itop_SupportHistory_.setText("Таблица: Support_history");
        test_Itop_SupportHistory_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_Itop_SupportHistory_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_Itop_SupportHistory_ActionPerformed(evt);
            }
        });
        menuTest_Itop.add(test_Itop_SupportHistory_);

        test_Itop_YearlyPlan_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_Itop_YearlyPlan_.setText("Таблица: Yearly_plan");
        test_Itop_YearlyPlan_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_Itop_YearlyPlan_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_Itop_YearlyPlan_ActionPerformed(evt);
            }
        });
        menuTest_Itop.add(test_Itop_YearlyPlan_);

        test_Itop_ConnectionChecking_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_Itop_ConnectionChecking_.setText("Проверка за връзка към БД");
        test_Itop_ConnectionChecking_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_Itop_ConnectionChecking_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_Itop_ConnectionChecking_ActionPerformed(evt);
            }
        });
        menuTest_Itop.add(test_Itop_ConnectionChecking_);

        menuHost_Test.add(menuTest_Itop);

        menuTest_DaeuReports.setText("Превалутиране на DB: daeu_reports");
        menuTest_DaeuReports.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuTest_DaeuReports.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        test_DaeuReports_FEthernetInfrastructure_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_DaeuReports_FEthernetInfrastructure_.setText("Таблица: F_Ethernet_infrastructure");
        test_DaeuReports_FEthernetInfrastructure_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_DaeuReports_FEthernetInfrastructure_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_DaeuReports_FEthernetInfrastructure_ActionPerformed(evt);
            }
        });
        menuTest_DaeuReports.add(test_DaeuReports_FEthernetInfrastructure_);

        test_DaeuReports_FHardwareAsset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_DaeuReports_FHardwareAsset_.setText("Таблица: F_HardwareAsset");
        test_DaeuReports_FHardwareAsset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_DaeuReports_FHardwareAsset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_DaeuReports_FHardwareAsset_ActionPerformed(evt);
            }
        });
        menuTest_DaeuReports.add(test_DaeuReports_FHardwareAsset_);

        test_DaeuReports_FSoftwareAsset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_DaeuReports_FSoftwareAsset_.setText("Таблица: F_SoftwareAsset");
        test_DaeuReports_FSoftwareAsset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_DaeuReports_FSoftwareAsset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_DaeuReports_FSoftwareAsset_ActionPerformed(evt);
            }
        });
        menuTest_DaeuReports.add(test_DaeuReports_FSoftwareAsset_);

        test_DaeuReports_FSupportHistory_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_DaeuReports_FSupportHistory_.setText("Таблица: F_Support_history");
        test_DaeuReports_FSupportHistory_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_DaeuReports_FSupportHistory_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_DaeuReports_FSupportHistory_ActionPerformed(evt);
            }
        });
        menuTest_DaeuReports.add(test_DaeuReports_FSupportHistory_);

        test_DaeuReports_FYearlyPlan_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_DaeuReports_FYearlyPlan_.setText("Таблица: F_Yearly_plan");
        test_DaeuReports_FYearlyPlan_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_DaeuReports_FYearlyPlan_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_DaeuReports_FYearlyPlan_ActionPerformed(evt);
            }
        });
        menuTest_DaeuReports.add(test_DaeuReports_FYearlyPlan_);

        test_DaeuReports_ConnectionChecking_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        test_DaeuReports_ConnectionChecking_.setText("Проверка за връзка към БД");
        test_DaeuReports_ConnectionChecking_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        test_DaeuReports_ConnectionChecking_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_DaeuReports_ConnectionChecking_ActionPerformed(evt);
            }
        });
        menuTest_DaeuReports.add(test_DaeuReports_ConnectionChecking_);

        menuHost_Test.add(menuTest_DaeuReports);

        menuChoiceFile.add(menuHost_Test);

        menuHost_Prod.setText("Host: XXX.XX.XXX.XX - Prod");
        menuHost_Prod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuHost_Prod.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        menuProd_Itop.setText("Превалутиране на DB: itop");
        menuProd_Itop.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuProd_Itop.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        prod_Itop_Asset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_Itop_Asset_.setText("Таблица: Asset");
        prod_Itop_Asset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_Itop_Asset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_Itop_Asset_ActionPerformed(evt);
            }
        });
        menuProd_Itop.add(prod_Itop_Asset_);

        prod_Itop_SupportHistory_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_Itop_SupportHistory_.setText("Таблица: Support_history");
        prod_Itop_SupportHistory_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_Itop_SupportHistory_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_Itop_SupportHistory_ActionPerformed(evt);
            }
        });
        menuProd_Itop.add(prod_Itop_SupportHistory_);

        prod_Itop_YearlyPlan_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_Itop_YearlyPlan_.setText("Таблица: Yearly_plan");
        prod_Itop_YearlyPlan_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_Itop_YearlyPlan_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_Itop_YearlyPlan_ActionPerformed(evt);
            }
        });
        menuProd_Itop.add(prod_Itop_YearlyPlan_);

        prod_Itop_ConnectionChecking_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_Itop_ConnectionChecking_.setText("Проверка за връзка към БД");
        prod_Itop_ConnectionChecking_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_Itop_ConnectionChecking_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_Itop_ConnectionChecking_ActionPerformed(evt);
            }
        });
        menuProd_Itop.add(prod_Itop_ConnectionChecking_);

        menuHost_Prod.add(menuProd_Itop);

        menuProd_DaeuReports.setText("Превалутиране на DB: daeu_reports");
        menuProd_DaeuReports.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuProd_DaeuReports.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N

        prod_DaeuReports_FEthernetInfrastructure_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_DaeuReports_FEthernetInfrastructure_.setText("Таблица: F_Ethernet_infrastructure");
        prod_DaeuReports_FEthernetInfrastructure_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_DaeuReports_FEthernetInfrastructure_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_DaeuReports_FEthernetInfrastructure_ActionPerformed(evt);
            }
        });
        menuProd_DaeuReports.add(prod_DaeuReports_FEthernetInfrastructure_);

        prod_DaeuReports_FHardwareAsset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_DaeuReports_FHardwareAsset_.setText("Таблица: F_HardwareAsset");
        prod_DaeuReports_FHardwareAsset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_DaeuReports_FHardwareAsset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_DaeuReports_FHardwareAsset_ActionPerformed(evt);
            }
        });
        menuProd_DaeuReports.add(prod_DaeuReports_FHardwareAsset_);

        prod_DaeuReports_FSoftwareAsset_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_DaeuReports_FSoftwareAsset_.setText("Таблица: F_SoftwareAsset");
        prod_DaeuReports_FSoftwareAsset_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_DaeuReports_FSoftwareAsset_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_DaeuReports_FSoftwareAsset_ActionPerformed(evt);
            }
        });
        menuProd_DaeuReports.add(prod_DaeuReports_FSoftwareAsset_);

        prod_DaeuReports_FSupportHistory_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_DaeuReports_FSupportHistory_.setText("Таблица: F_Support_history");
        prod_DaeuReports_FSupportHistory_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_DaeuReports_FSupportHistory_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_DaeuReports_FSupportHistory_ActionPerformed(evt);
            }
        });
        menuProd_DaeuReports.add(prod_DaeuReports_FSupportHistory_);

        prod_DaeuReports_FYearlyPlan_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_DaeuReports_FYearlyPlan_.setText("Таблица: F_Yearly_plan");
        prod_DaeuReports_FYearlyPlan_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_DaeuReports_FYearlyPlan_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_DaeuReports_FYearlyPlan_ActionPerformed(evt);
            }
        });
        menuProd_DaeuReports.add(prod_DaeuReports_FYearlyPlan_);

        prod_DaeuReports_ConnectionChecking_.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        prod_DaeuReports_ConnectionChecking_.setText("Проверка за връзка към БД");
        prod_DaeuReports_ConnectionChecking_.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        prod_DaeuReports_ConnectionChecking_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prod_DaeuReports_ConnectionChecking_ActionPerformed(evt);
            }
        });
        menuProd_DaeuReports.add(prod_DaeuReports_ConnectionChecking_);

        menuHost_Prod.add(menuProd_DaeuReports);

        menuChoiceFile.add(menuHost_Prod);

        sebraMenuBar.add(menuChoiceFile);

        jMenu1.setText("  |  ");
        jMenu1.setEnabled(false);
        sebraMenuBar.add(jMenu1);

        sebraMenuBar.add(Box.createHorizontalGlue());
        About.setText("  |   ");
        About.setEnabled(false);
        sebraMenuBar.add(About);

        menuAbout.setText("About  ");
        menuAbout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuAbout.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        menuAbout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuAboutMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                menuAboutMousePressed(evt);
            }
        });
        sebraMenuBar.add(menuAbout);

        setJMenuBar(sebraMenuBar);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void menuChoiceFileMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuChoiceFileMouseEntered
        slText = "<html>" + "&nbsp;&nbsp;<b><FONT COLOR=RED>1.</FONT></b>&nbsp;<b>Изберете:</b>&nbsp;<i><FONT COLOR=BLUE>Хост!</FONT></i>" + "&nbsp;&nbsp;&nbsp;&nbsp;<b><FONT COLOR=RED>2.</FONT></b>&nbsp;<b>Изберете:</b>&nbsp;<i><FONT COLOR=BLUE>База&nbsp;Данни!</FONT></i>" + "&nbsp;&nbsp;&nbsp;&nbsp;<b><FONT COLOR=RED>3.</FONT></b>&nbsp;<b>Изберете:</b>&nbsp;<i><FONT COLOR=BLUE>Таблица&nbsp;за&nbsp;превалутиране!</FONT></i>" + "</html>";
        setStatusLabel(slText);
    }//GEN-LAST:event_menuChoiceFileMouseEntered

    private void menuChoiceFileMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuChoiceFileMousePressed
        slText = "<html>" + "&nbsp;&nbsp;<b><FONT COLOR=RED>1.</FONT></b>&nbsp;<b>Изберете:</b>&nbsp;<i><FONT COLOR=BLUE>Хост!</FONT></i>" + "&nbsp;&nbsp;&nbsp;&nbsp;<b><FONT COLOR=RED>2.</FONT></b>&nbsp;<b>Изберете:</b>&nbsp;<i><FONT COLOR=BLUE>База&nbsp;Данни!</FONT></i>" + "&nbsp;&nbsp;&nbsp;&nbsp;<b><FONT COLOR=RED>3.</FONT></b>&nbsp;<b>Изберете:</b>&nbsp;<i><FONT COLOR=BLUE>Таблица&nbsp;за&nbsp;превалутиране!</FONT></i>" + "</html>";
        setStatusLabel(slText);
    }//GEN-LAST:event_menuChoiceFileMousePressed

    private void menuAboutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuAboutMouseEntered
        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN>&copy;&nbsp;</FONT></b><b><FONT COLOR=BLUE>2025 Ministry&nbsp;of&nbsp;e-Governance.&nbsp;All&nbsp;rights&nbsp;reserved.</FONT>&nbsp;&nbsp;<FONT COLOR=GREEN>Ver.1.03</FONT></b>&nbsp;&nbsp;</html>";
        setStatusLabel(slText);
    }//GEN-LAST:event_menuAboutMouseEntered

    private void menuAboutMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuAboutMousePressed
        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN>&copy;&nbsp;</FONT></b><b><FONT COLOR=BLUE>2025 Ministry&nbsp;of&nbsp;e-Governance.&nbsp;All&nbsp;rights&nbsp;reserved.</FONT>&nbsp;&nbsp;<FONT COLOR=GREEN>Ver.1.03</FONT></b>&nbsp;&nbsp;</html>";
        setStatusLabel(slText);
    }//GEN-LAST:event_menuAboutMousePressed

    private void local_Itop_Asset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_Itop_Asset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalItop;
        host = "Local Host";
        db = "itop";
        table = "Asset";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_Itop_Asset_ActionPerformed

    private void local_Itop_SupportHistory_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_Itop_SupportHistory_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalItop;
        host = "Local Host";
        db = "itop";
        table = "Support_history";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_Itop_SupportHistory_ActionPerformed

    private void local_Itop_YearlyPlan_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_Itop_YearlyPlan_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalItop;
        host = "Local Host";
        db = "itop";
        table = "Yearly_plan";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_Itop_YearlyPlan_ActionPerformed

    private void local_DaeuReports_FEthernetInfrastructure_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_DaeuReports_FEthernetInfrastructure_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalDaeuReports;
        host = "Local Host";
        db = "daeu_reports";
        table = "F_Ethernet_infrastructure";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_DaeuReports_FEthernetInfrastructure_ActionPerformed

    private void local_DaeuReports_FHardwareAsset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_DaeuReports_FHardwareAsset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalDaeuReports;
        host = "Local Host";
        db = "daeu_reports";
        table = "F_HardwareAsset";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_DaeuReports_FHardwareAsset_ActionPerformed

    private void local_DaeuReports_FSoftwareAsset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_DaeuReports_FSoftwareAsset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalDaeuReports;
        host = "Local Host";
        db = "daeu_reports";
        table = "F_SoftwareAsset";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_DaeuReports_FSoftwareAsset_ActionPerformed

    private void local_DaeuReports_FSupportHistory_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_DaeuReports_FSupportHistory_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalDaeuReports;
        host = "Local Host";
        db = "daeu_reports";
        table = "F_Support_history";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_DaeuReports_FSupportHistory_ActionPerformed

    private void local_DaeuReports_FYearlyPlan_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_DaeuReports_FYearlyPlan_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlLocalDaeuReports;
        host = "Local Host";
        db = "daeu_reports";
        table = "F_Yearly_plan";
        unm = unmLocal;
        psd = psdLocal;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_local_DaeuReports_FYearlyPlan_ActionPerformed

    private void test_Itop_Asset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_Itop_Asset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestItop;
        host = "Test Host";
        db = "itop";
        table = "Asset";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_Itop_Asset_ActionPerformed

    private void test_Itop_SupportHistory_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_Itop_SupportHistory_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestItop;
        host = "Test Host";
        db = "itop";
        table = "Support_history";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_Itop_SupportHistory_ActionPerformed

    private void test_Itop_YearlyPlan_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_Itop_YearlyPlan_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestItop;
        host = "Test Host";
        db = "itop";
        table = "Yearly_plan";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_Itop_YearlyPlan_ActionPerformed

    private void test_DaeuReports_FEthernetInfrastructure_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_DaeuReports_FEthernetInfrastructure_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestDaeuReports;
        host = "Test Host";
        db = "daeu_reports";
        table = "F_Ethernet_infrastructure";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_DaeuReports_FEthernetInfrastructure_ActionPerformed

    private void test_DaeuReports_FHardwareAsset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_DaeuReports_FHardwareAsset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestDaeuReports;
        host = "Test Host";
        db = "daeu_reports";
        table = "F_HardwareAsset";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_DaeuReports_FHardwareAsset_ActionPerformed

    private void test_DaeuReports_FSoftwareAsset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_DaeuReports_FSoftwareAsset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestDaeuReports;
        host = "Test Host";
        db = "daeu_reports";
        table = "F_SoftwareAsset";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_DaeuReports_FSoftwareAsset_ActionPerformed

    private void test_DaeuReports_FSupportHistory_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_DaeuReports_FSupportHistory_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestDaeuReports;
        host = "Test Host";
        db = "daeu_reports";
        table = "F_Support_history";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_DaeuReports_FSupportHistory_ActionPerformed

    private void test_DaeuReports_FYearlyPlan_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_DaeuReports_FYearlyPlan_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlTestDaeuReports;
        host = "Test Host";
        db = "daeu_reports";
        table = "F_Yearly_plan";
        unm = unmTest;
        psd = psdTest;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_test_DaeuReports_FYearlyPlan_ActionPerformed

    private void prod_Itop_Asset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_Itop_Asset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdItop;
        host = "Prod Host";
        db = "itop";
        table = "Asset";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_Itop_Asset_ActionPerformed

    private void prod_Itop_SupportHistory_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_Itop_SupportHistory_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdItop;
        host = "Prod Host";
        db = "itop";
        table = "Support_history";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_Itop_SupportHistory_ActionPerformed

    private void prod_Itop_YearlyPlan_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_Itop_YearlyPlan_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdItop;
        host = "Prod Host";
        db = "itop";
        table = "Yearly_plan";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_Itop_YearlyPlan_ActionPerformed

    private void prod_DaeuReports_FEthernetInfrastructure_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_DaeuReports_FEthernetInfrastructure_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdDaeuReports;
        host = "Prod Host";
        db = "daeu_reports";
        table = "F_Ethernet_infrastructure";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_DaeuReports_FEthernetInfrastructure_ActionPerformed

    private void prod_DaeuReports_FHardwareAsset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_DaeuReports_FHardwareAsset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdDaeuReports;
        host = "Prod Host";
        db = "daeu_reports";
        table = "F_HardwareAsset";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_DaeuReports_FHardwareAsset_ActionPerformed

    private void prod_DaeuReports_FSoftwareAsset_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_DaeuReports_FSoftwareAsset_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdDaeuReports;
        host = "Prod Host";
        db = "daeu_reports";
        table = "F_SoftwareAsset";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_DaeuReports_FSoftwareAsset_ActionPerformed

    private void prod_DaeuReports_FSupportHistory_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_DaeuReports_FSupportHistory_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdDaeuReports;
        host = "Prod Host";
        db = "daeu_reports";
        table = "F_Support_history";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_DaeuReports_FSupportHistory_ActionPerformed

    private void prod_DaeuReports_FYearlyPlan_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_DaeuReports_FYearlyPlan_ActionPerformed
        String url = "";    // URL
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String unm = "";    // Username
        String psd = "";    // Password

        url = urlProdDaeuReports;
        host = "Prod Host";
        db = "daeu_reports";
        table = "F_Yearly_plan";
        unm = unmProd;
        psd = psdProd;

        connectingToDb(url, host, db, table, unm, psd);
    }//GEN-LAST:event_prod_DaeuReports_FYearlyPlan_ActionPerformed

    private void local_Itop_ConnectionChecking_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_Itop_ConnectionChecking_ActionPerformed
        Connection conn = null;
        String msg = "";
        String error = "";
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String url = "";    // URL
        String unm = "";    // Username
        String psd = "";    // Password

        host = "Local Host";    // "Local Host" | "Test Host" | "Prod Host"
        db = "itop";            // "itop" | "daeu_reports"
        table = "Asset";        // "Asset" | "F_SoftwareAsset"
        url = urlLocalItop;     // urlLocalItop | urlLocalDaeuReports | urlTestItop | urlTestDaeuReports | urlProdItop | urlProdDaeuReports
        unm = unmLocal;         // unmLocal | unmTest | unmProd
        psd = psdLocal;         // psdLocal | psdTest | psdProd

        try {
            Class.forName(driverMariadb);
            conn = DriverManager.getConnection(url, unm, psd);

            taText = " • Успешно свързване към: " + host + " | DB: " + db + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно свързване към:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + "</FONT></i></html>";
            setStatusLabel(slText);
			
            System.out.println("Connection is successful to: " + host + " | DB: " + db + "!");
            log.info("Connection is successful to: " + host + " | DB: " + db + "!");
        } catch (Exception e) {
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            setStatusLabel(slText);

            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            JOptionPane.showMessageDialog(f, msg);

            error = e.getMessage();
            taText = " • Error: " + error + "!";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);
			
            System.out.println("Error: " + error);
            log.log(Level.WARNING, "Error: " + error);
        }
    }//GEN-LAST:event_local_Itop_ConnectionChecking_ActionPerformed

    private void local_DaeuReports_ConnectionChecking_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_local_DaeuReports_ConnectionChecking_ActionPerformed
        Connection conn = null;
        String msg = "";
        String error = "";
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String url = "";    // URL
        String unm = "";    // Username
        String psd = "";    // Password

        host = "Local Host";        // "Local Host" | "Test Host" | "Prod Host"
        db = "daeu_reports";        // "itop" | "daeu_reports"
        table = "F_SoftwareAsset";  // "Asset" | "F_SoftwareAsset"
        url = urlLocalDaeuReports;  // urlLocalItop | urlLocalDaeuReports | urlTestItop | urlTestDaeuReports | urlProdItop | urlProdDaeuReports
        unm = unmLocal;             // unmLocal | unmTest | unmProd
        psd = psdLocal;             // psdLocal | psdTest | psdProd

        try {
            Class.forName(driverMariadb);
            conn = DriverManager.getConnection(url, unm, psd);

            taText = " • Успешно свързване към: " + host + " | DB: " + db + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно свързване към:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + "</FONT></i></html>";
            setStatusLabel(slText);
			
            System.out.println("Connection is successful to: " + host + " | DB: " + db + "!");
            log.info("Connection is successful to: " + host + " | DB: " + db + "!");
        } catch (Exception e) {
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            setStatusLabel(slText);

            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            JOptionPane.showMessageDialog(f, msg);

            error = e.getMessage();
            taText = " • Error: " + error + "!";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);
			
            System.out.println("Error: " + error);
            log.log(Level.WARNING, "Error: " + error);
        }
    }//GEN-LAST:event_local_DaeuReports_ConnectionChecking_ActionPerformed

    private void test_Itop_ConnectionChecking_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_Itop_ConnectionChecking_ActionPerformed
        Connection conn = null;
        String msg = "";
        String error = "";
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String url = "";    // URL
        String unm = "";    // Username
        String psd = "";    // Password

        host = "Test Host";     // "Local Host" | "Test Host" | "Prod Host"
        db = "itop";            // "itop" | "daeu_reports"
        table = "Asset";        // "Asset" | "F_SoftwareAsset"
        url = urlTestItop;      // urlLocalItop | urlLocalDaeuReports | urlTestItop | urlTestDaeuReports | urlProdItop | urlProdDaeuReports
        unm = unmTest;          // unmLocal | unmTest | unmProd
        psd = psdTest;          // psdLocal | psdTest | psdProd

        try {
            Class.forName(driverMariadb);
            conn = DriverManager.getConnection(url, unm, psd);

            taText = " • Успешно свързване към: " + host + " | DB: " + db + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно свързване към:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + "</FONT></i></html>";
            setStatusLabel(slText);
			
            System.out.println("Connection is successful to: " + host + " | DB: " + db + "!");
            log.info("Connection is successful to: " + host + " | DB: " + db + "!");
        } catch (Exception e) {
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            setStatusLabel(slText);

            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            JOptionPane.showMessageDialog(f, msg);

            error = e.getMessage();
            taText = " • Error: " + error + "!";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);
			
            System.out.println("Error: " + error);
            log.log(Level.WARNING, "Error: " + error);
        }
    }//GEN-LAST:event_test_Itop_ConnectionChecking_ActionPerformed

    private void test_DaeuReports_ConnectionChecking_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_DaeuReports_ConnectionChecking_ActionPerformed
        Connection conn = null;
        String msg = "";
        String error = "";
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String url = "";    // URL
        String unm = "";    // Username
        String psd = "";    // Password

        host = "Test Host";         // "Local Host" | "Test Host" | "Prod Host"
        db = "daeu_reports";        // "itop" | "daeu_reports"
        table = "F_SoftwareAsset";  // "Asset" | "F_SoftwareAsset"
        url = urlTestDaeuReports;   // urlLocalItop | urlLocalDaeuReports | urlTestItop | urlTestDaeuReports | urlProdItop | urlProdDaeuReports
        unm = unmTest;              // unmLocal | unmTest | unmProd
        psd = psdTest;              // psdLocal | psdTest | psdProd

        try {
            Class.forName(driverMariadb);
            conn = DriverManager.getConnection(url, unm, psd);

            taText = " • Успешно свързване към: " + host + " | DB: " + db + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно свързване към:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + "</FONT></i></html>";
            setStatusLabel(slText);
			
            System.out.println("Connection is successful to: " + host + " | DB: " + db + "!");
            log.info("Connection is successful to: " + host + " | DB: " + db + "!");
        } catch (Exception e) {
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            setStatusLabel(slText);

            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            JOptionPane.showMessageDialog(f, msg);

            error = e.getMessage();
            taText = " • Error: " + error + "!";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);
			
            System.out.println("Error: " + error);
            log.log(Level.WARNING, "Error: " + error);
        }
    }//GEN-LAST:event_test_DaeuReports_ConnectionChecking_ActionPerformed

    private void prod_Itop_ConnectionChecking_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_Itop_ConnectionChecking_ActionPerformed
        Connection conn = null;
        String msg = "";
        String error = "";
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String url = "";    // URL
        String unm = "";    // Username
        String psd = "";    // Password

        host = "Prod Host";     // "Local Host" | "Test Host" | "Prod Host"
        db = "itop";            // "itop" | "daeu_reports"
        table = "Asset";        // "Asset" | "F_SoftwareAsset"
        url = urlProdItop;      // urlLocalItop | urlLocalDaeuReports | urlTestItop | urlTestDaeuReports | urlProdItop | urlProdDaeuReports
        unm = unmProd;          // unmLocal | unmTest | unmProd
        psd = psdProd;          // psdLocal | psdTest | psdProd

        try {
            Class.forName(driverMariadb);
            conn = DriverManager.getConnection(url, unm, psd);

            taText = " • Успешно свързване към: " + host + " | DB: " + db + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно свързване към:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + "</FONT></i></html>";
            setStatusLabel(slText);
			
            System.out.println("Connection is successful to: " + host + " | DB: " + db + "!");
            log.info("Connection is successful to: " + host + " | DB: " + db + "!");
        } catch (Exception e) {
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            setStatusLabel(slText);

            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            JOptionPane.showMessageDialog(f, msg);

            error = e.getMessage();
            taText = " • Error: " + error + "!";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);
			
            System.out.println("Error: " + error);
            log.log(Level.WARNING, "Error: " + error);
        }
    }//GEN-LAST:event_prod_Itop_ConnectionChecking_ActionPerformed

    private void prod_DaeuReports_ConnectionChecking_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prod_DaeuReports_ConnectionChecking_ActionPerformed
        Connection conn = null;
        String msg = "";
        String error = "";
        String host = "";   // Host
        String db = "";     // DB
        String table = "";  // Table
        String url = "";    // URL
        String unm = "";    // Username
        String psd = "";    // Password

        host = "Prod Host";         // "Local Host" | "Test Host" | "Prod Host"
        db = "daeu_reports";        // "itop" | "daeu_reports"
        table = "F_SoftwareAsset";  // "Asset" | "F_SoftwareAsset"
        url = urlProdDaeuReports;   // urlLocalItop | urlLocalDaeuReports | urlTestItop | urlTestDaeuReports | urlProdItop | urlProdDaeuReports
        unm = unmProd;              // unmLocal | unmTest | unmProd
        psd = psdProd;              // psdLocal | psdTest | psdProd

        try {
            Class.forName(driverMariadb);
            conn = DriverManager.getConnection(url, unm, psd);

            taText = " • Успешно свързване към: " + host + " | DB: " + db + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно свързване към:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + "</FONT></i></html>";
            setStatusLabel(slText);
			
            System.out.println("Connection is successful to: " + host + " | DB: " + db + "!");
            log.info("Connection is successful to: " + host + " | DB: " + db + "!");
        } catch (Exception e) {
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            setStatusLabel(slText);

            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + "</i></FONT></html>";
            JOptionPane.showMessageDialog(f, msg);

            error = e.getMessage();
            taText = " • Error: " + error + "!";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);
			
            System.out.println("Error: " + error);
            log.log(Level.WARNING, "Error: " + error);
        }
    }//GEN-LAST:event_prod_DaeuReports_ConnectionChecking_ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RirToEuro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RirToEuro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RirToEuro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RirToEuro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RirToEuro().setVisible(true);
            }
        });
    }

    public void connectingToDb(String url, String host, String db, String table, String unm, String psd) {
        Connection conn = null;
        String msg = "";
        String error = "";

        try {
            Class.forName(driverMariadb);
            conn = DriverManager.getConnection(url, unm, psd);

            taText = " • Успешно свързване към: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно свързване към:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "</FONT></i></html>";
            setStatusLabel(slText);

            Object[] options = {"Да, моля", "Няма начин!"};
            msg = "<html><i><b><FONT COLOR=BLUE>Да стартира ли превалутиране BGN -> EURO?</FONT></b></i></html>";
            int num_opt = JOptionPane.showOptionDialog(f, msg, "Уместен въпрос", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (num_opt == JOptionPane.YES_OPTION) {
                switch (table) {
                    case "Asset":  // DB: itop
                        getCurrencyItopAsset(conn, host, db, table);  // -> updateItopAsset(conn, host, db, table);
                        break;
                    case "Support_history":  // DB: itop
                        getCurrencyItopSupportHistory(conn, host, db, table);  // -> updateItopSupportHistory(conn, host, db, table);
                        break;
                    case "Yearly_plan":  // DB: itop
                        getCurrencyItopYearlyPlan(conn, host, db, table);  // -> updateItopYearlyPlan(conn, host, db, table);
                        break;
                    case "F_Ethernet_infrastructure":  // DB daeu_reports
                        getCurrencyDaeuReportsFEthernetInfrastructure(conn, host, db, table);  // -> updateDaeuReportsFEthernetInfrastructure(conn, host, db, table);
                        break;
                    case "F_HardwareAsset":  // DB daeu_reports
                        getCurrencyDaeuReportsFHardwareAsset(conn, host, db, table);  // -> updateDaeuReportsFHardwareAsset(conn, host, db, table);
                        break;
                    case "F_SoftwareAsset":  // DB daeu_reports
                        getCurrencyDaeuReportsFSoftwareAsset(conn, host, db, table);  // -> updateDaeuReportsFSoftwareAsset(conn, host, db, table);
                        break;
                    case "F_Support_history":  // DB daeu_reports
                        getCurrencyDaeuReportsFSupportHistory(conn, host, db, table);  // -> updateDaeuReportsFSupportHistory(conn, host, db, table);
                        break;
                    case "F_Yearly_plan":  // DB daeu_reports
                        getCurrencyDaeuReportsFYearlyPlan(conn, host, db, table);  // -> updateDaeuReportsFYearlyPlan(conn, host, db, table);
                        break;
                    default:
                        break;
                }
            } else if (num_opt == JOptionPane.NO_OPTION) {
                msg = "<html><i><b><FONT COLOR=BLUE>Отказът Ви е одобрен!</FONT></b></i></html>";
                JOptionPane.showMessageDialog(f, msg);
                clearStatusLabel();
            } else {
                msg = "<html><i><b><FONT COLOR=BLUE>Отказът Ви е одобрен!</FONT></b></i></html>";
                JOptionPane.showMessageDialog(f, msg);
                clearStatusLabel();
            }
            System.out.println("Connection is successful to: " + host + " | DB: " + db + " | Table: " + table + "!");
            log.info("Connection is successful to: " + host + " | DB: " + db + "!");
        } catch (Exception e) {
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + " | Table: " + table + "</i></FONT></html>";
            setStatusLabel(slText);

            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Неуспешно свързване към:&nbsp;&nbsp;</b><i>" + host + " | DB: " + db + " | Table: " + table + "</i></FONT></html>";
            JOptionPane.showMessageDialog(f, msg);

            error = e.getMessage();
            taText = " • Error: " + error + "!";
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);
            System.out.println("Error: " + error);
            log.log(Level.WARNING, "Error: " + error);
        }
    }

    // ----- Asset ---------------------------------------------------------------------
    public void getCurrencyItopAsset(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer id = 0;
        String inventory_key = "";
        Double acquiring_price = 0.00d;

        String sId = "";
        String sAcquiringPrice = "";

        String line = "";
        Asset dataAsset = null;
        listAsset = new ArrayList<>();
        BufferedWriter bw = null;

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        Double euroExchangeRate = 1.95583d;
        Integer places = 2;
        Double newAacquiringPrice = 0.00d;
        String sNewAacquiringPrice = "";

        sql = "SELECT id, inventory_key, acquiring_price "
                + "FROM itop.Asset "
                + "ORDER BY id ASC";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                id = resultSet.getInt("id");
                if (resultSet.getString("inventory_key") != null) {
                    inventory_key = resultSet.getString("inventory_key");
                } else {
                    inventory_key = "";
                }
                acquiring_price = resultSet.getDouble("acquiring_price");
                if (acquiring_price == null) {  // acquiring_price.isNaN() - Is Not-a-Number
                    acquiring_price = 0.00d;
                }

                dataAsset = new Asset();
                dataAsset.id = id;
                dataAsset.inventory_key = inventory_key;
                dataAsset.acquiring_price = acquiring_price;
                listAsset.add(dataAsset);
            }

            sbBefore = new StringBuilder();
            line = "id | " + "inventory_key | " + "acquiring_price";
            sbBefore.append(line).append("\n");

            for (Asset list : listAsset) {
                id = list.id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sId = String.valueOf(id);
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sId + " | " + inventory_key + " | " + sAcquiringPrice;
                sbBefore.append(line).append("\n");
                // System.out.println("| " + sId + " | " + inventory_key + " | " + sAcquiringPrice + " |");

                newAacquiringPrice = (acquiring_price / euroExchangeRate);
                newAacquiringPrice = round(newAacquiringPrice, places);
                sNewAacquiringPrice = new DecimalFormat("#0.00").format(newAacquiringPrice);
                list.setAcquiring_price(newAacquiringPrice);
                // System.out.println("| id: " + id + " | sAcquiringPrice: " + sAcquiringPrice + " | sNewAacquiringPrice: " + sNewAacquiringPrice + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "id | " + "inventory_key | " + "acquiring_price";
            sbAfter.append(line).append("\n");

            for (Asset list : listAsset) {
                id = list.id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sId = String.valueOf(id);
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sId + " | " + inventory_key + " | " + sAcquiringPrice;
                sbAfter.append(line).append("\n");
                // System.out.println("| " + sId + " | " + inventory_key + " | " + acquiring_way + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateItopAsset(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }
    }

    public void updateItopAsset(Connection conn, String host, String db, String table) {
        String msg = "";
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;
        Integer id = 0;
        String inventory_key = "";
        Double acquiring_price = 0.00d;

        String sId = "";
        String sAcquiringPrice = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE itop.Asset "
                    + "SET acquiring_price = ? "
                    + "WHERE id = ?";
            pstmt = conn.prepareStatement(query);

            for (Asset list : listAsset) {
                id = list.id;
                acquiring_price = list.acquiring_price;

                conn.setAutoCommit(false);
                pstmt.setDouble(1, acquiring_price);
                pstmt.setInt(2, id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- Asset ---------------------------------------------------------------------
    // ----- Support_history -----------------------------------------------------------
    public void getCurrencyItopSupportHistory(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer id = 0;
        Integer asset_id = 0;
        Double resource_cost = 0.00d;
        Double resource_cost_parts = 0.00d;
        
        String sId = "";
        String sAssetId = "";
        String sResourceCost = "";
        String sResourceCostParts = "";

        String line = "";
        SupportHistory dataSupportHistory = null;
        listSupportHistory = new ArrayList<>();
        BufferedWriter bw = null;

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        Double euroExchangeRate = 1.95583d;
        Integer places = 2;
        Double newResourceCost = 0.00d;
        Double newResourceCostParts = 0.00d;
        String sNewResourceCost = "";
        String sNewResourceCostParts = "";

        sql = "SELECT id, asset_id, resource_cost,resource_cost_parts "
                + "FROM itop.Support_history "
                + "ORDER BY id ASC";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                id = resultSet.getInt("id");
                asset_id = resultSet.getInt("asset_id");
                if (asset_id == null) {
                    asset_id = 0;
                }
                resource_cost = resultSet.getDouble("resource_cost");
                if (resource_cost == null) {  // resource_cost.isNaN() - Is Not-a-Number
                    resource_cost = 0.00d;
                }
                resource_cost_parts = resultSet.getDouble("resource_cost_parts");
                if (resource_cost_parts == null) {  // resource_cost_parts.isNaN() - Is Not-a-Number
                    resource_cost_parts = 0.00d;
                }

                dataSupportHistory = new SupportHistory();
                dataSupportHistory.id = id;
               dataSupportHistory.asset_id = asset_id;
                dataSupportHistory.resource_cost = resource_cost;
                dataSupportHistory.resource_cost_parts = resource_cost_parts;
                listSupportHistory.add(dataSupportHistory);
            }

            sbBefore = new StringBuilder();
            line = "id | " + "asset_id | " + "resource_cost | " + "resource_cost_parts";
            sbBefore.append(line).append("\n");

            for (SupportHistory list : listSupportHistory) {
                id = list.id;
                asset_id = list.asset_id;
                resource_cost = list.resource_cost;
                resource_cost_parts = list.resource_cost_parts;

                sId = String.valueOf(id);
                sAssetId = String.valueOf(asset_id);
                sResourceCost = new DecimalFormat("#0.00").format(resource_cost);
                sResourceCostParts = new DecimalFormat("#0.00").format(resource_cost_parts);
                sResourceCost = sResourceCost.replace(',', '.');
                sResourceCostParts = sResourceCostParts.replace(',', '.');

                line = sId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts;
                sbBefore.append(line).append("\n");
                // System.out.println("| " + sId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts + " |");

                newResourceCost = (resource_cost / euroExchangeRate);
                newResourceCost = round(newResourceCost, places);
                sNewResourceCost = new DecimalFormat("#0.00").format(newResourceCost);
                list.setResource_cost(newResourceCost);

                newResourceCostParts = (resource_cost_parts / euroExchangeRate);
                newResourceCostParts = round(newResourceCostParts, places);
                sNewResourceCostParts = new DecimalFormat("#0.00").format(newResourceCostParts);
                list.setResource_cost_parts(newResourceCostParts);

                // System.out.println("| id: " + id + " | sResourceCost: " + sResourceCost + " | sNewResourceCost: " + sNewResourceCost + " | sResourceCostParts: " + sResourceCostParts + " | sNewResourceCostParts: " + sNewResourceCostParts + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "id | " + "asset_id | " + "resource_cost | " + "resource_cost_parts";
            sbAfter.append(line).append("\n");

            for (SupportHistory list : listSupportHistory) {
                id = list.id;
                asset_id = list.asset_id;
                resource_cost = list.resource_cost;
                resource_cost_parts = list.resource_cost_parts;

                sId = String.valueOf(id);
                sAssetId = String.valueOf(asset_id);
                sResourceCost = new DecimalFormat("#0.00").format(resource_cost);
                sResourceCostParts = new DecimalFormat("#0.00").format(resource_cost_parts);
                sResourceCost = sResourceCost.replace(',', '.');
                sResourceCostParts = sResourceCostParts.replace(',', '.');

                line = sId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts;
                sbAfter.append(line).append("\n");
                // System.out.println("| " + sId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateItopSupportHistory(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }
    }

    public void updateItopSupportHistory(Connection conn, String host, String db, String table) {
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;

        Integer id = 0;
        Integer asset_id = 0;
        Double resource_cost = 0.00d;
        Double resource_cost_parts = 0.00d;

        String sId = "";
        String sAssetId = "";
        String sResourceCost = "";
        String sResourceCostParts = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE itop.Support_history "
                    + "SET resource_cost = ?, resource_cost_parts = ? "
                    + "WHERE id = ?";
            pstmt = conn.prepareStatement(query);

            for (SupportHistory list : listSupportHistory) {
                id = list.id;
                resource_cost = list.resource_cost;
                resource_cost_parts = list.resource_cost_parts;

                conn.setAutoCommit(false);
                pstmt.setDouble(1, resource_cost);
                pstmt.setDouble(2, resource_cost_parts);
                pstmt.setInt(3, id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- Support_history -----------------------------------------------------------
    // ----- Yearly_plan ---------------------------------------------------------------
    public void getCurrencyItopYearlyPlan(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer id = 0;
        String month = "";
        Integer year = 0;
        Long planned_resources = 0L;
        Long newPlannedResources = 0L;

        String sId = "";
        String sMonth = "";
        String sYear = "";
        String sPlannedResources = "";
        String sNewPlannedResources = "";

        String line = "";
        YearlyPlan dataYearlyPlan = null;
        listYearlyPlan = new ArrayList<>();
        BufferedWriter bw = null;

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        Double dblNewPlannedResources = 0.00d;
        Double euroExchangeRate = 1.95583d;
        Integer places = 0;

        sql = "SELECT id, month, year, planned_resources "
                + "FROM itop.Yearly_plan "
                + "ORDER BY id ASC";
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                id = resultSet.getInt("id");
                if (resultSet.getString("month") != null) {
                    month = resultSet.getString("month");
                } else {
                    month = "";
                }
                year = resultSet.getInt("year");
                if (year == null) {
                    year = 0;
                }
                planned_resources = resultSet.getLong("planned_resources");
                if (planned_resources == null) {
                    planned_resources = 0L;
                }

                dataYearlyPlan = new YearlyPlan();
                dataYearlyPlan.id = id;
                dataYearlyPlan.month = month;
                dataYearlyPlan.year = year;
                dataYearlyPlan.planned_resources = planned_resources;
                listYearlyPlan.add(dataYearlyPlan);
            }

            sbBefore = new StringBuilder();
            line = "id | " + "month | " + "year | " + "planned_resources";
            sbBefore.append(line).append("\n");

            for (YearlyPlan list : listYearlyPlan) {
                id = list.id;
                month = list.month;
                year = list.year;
                planned_resources = list.planned_resources;

                sId = String.valueOf(id);
                sMonth = month;
                sYear = String.valueOf(year);
                sPlannedResources = String.valueOf(planned_resources);

                line = sId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources;
                sbBefore.append(line).append("\n");
                // System.out.println("| " + sId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources + " |");

                dblNewPlannedResources = ((double) planned_resources / euroExchangeRate);
                dblNewPlannedResources = round(dblNewPlannedResources, places);
                newPlannedResources = dblNewPlannedResources.longValue();
                sNewPlannedResources = String.valueOf(newPlannedResources);

                list.setPlanned_resources(newPlannedResources);
                // System.out.println("| id: " + sId + " | planned_resources: " + sPlannedResources + " | newPlannedResources: " + sNewPlannedResources + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "id | " + "month | " + "year | " + "planned_resources";
            sbAfter.append(line).append("\n");

            for (YearlyPlan list : listYearlyPlan) {
                id = list.id;
                month = list.month;
                year = list.year;
                planned_resources = list.planned_resources;

                sId = String.valueOf(id);
                sMonth = month;
                sYear = String.valueOf(year);
                sPlannedResources = String.valueOf(planned_resources);

                line = sId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources;
                sbAfter.append(line).append("\n");
                // System.out.println("| " + sId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateItopYearlyPlan(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }
    }

    public void updateItopYearlyPlan(Connection conn, String host, String db, String table) {
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;

        Integer id = 0;
        String month = "";
        Integer year = 0;
        Long planned_resources = 0L;

        String sId = "";
        String sMonth = "";
        String sYear = "";
        String sPlannedResources = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE itop.Yearly_plan "
                    + "SET planned_resources = ? "
                    + "WHERE id = ?";
            pstmt = conn.prepareStatement(query);

            for (YearlyPlan list : listYearlyPlan) {
                id = list.id;
                planned_resources = list.planned_resources;

                conn.setAutoCommit(false);
                pstmt.setLong(1, planned_resources);
                pstmt.setInt(2, id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- Yearly_plan ---------------------------------------------------------------
    // ----- F_Ethernet_infrastructure -------------------------------------------------
    public void getCurrencyDaeuReportsFEthernetInfrastructure(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer record_id = 0;
        String inventory_key = "";
        Double acquiring_price = 0.00d;

        String sRecordId = "";
        String sInventoryKey = "";
        String sAcquiringPrice = "";

        String line = "";
        FEthernetInfrastructure dataFEthernetInfrastructure = null;
        listFEthernetInfrastructure = new ArrayList<>();
        BufferedWriter bw = null;

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        Double euroExchangeRate = 1.95583d;
        Integer places = 2;
        Double newAacquiringPrice = 0.00d;
        String sNewAacquiringPrice = "";

        sql = "SELECT record_id, inventory_key, acquiring_price "
                + "FROM daeu_reports.F_Ethernet_infrastructure "
                + "ORDER BY record_id ASC";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                record_id = resultSet.getInt("record_id");
                if (resultSet.getString("inventory_key") != null) {
                    inventory_key = resultSet.getString("inventory_key");
                } else {
                    inventory_key = "";
                }
                acquiring_price = resultSet.getDouble("acquiring_price");
                if (acquiring_price == null) {  // acquiring_price.isNaN() - Is Not-a-Number
                    acquiring_price = 0.00d;
                }

                dataFEthernetInfrastructure = new FEthernetInfrastructure();
                dataFEthernetInfrastructure.record_id = record_id;
                dataFEthernetInfrastructure.inventory_key = inventory_key;
                dataFEthernetInfrastructure.acquiring_price = acquiring_price;
                listFEthernetInfrastructure.add(dataFEthernetInfrastructure);
            }

            sbBefore = new StringBuilder();
            line = "record_id | " + "inventory_key | " + "acquiring_price";
            sbBefore.append(line).append("\n");

            for (FEthernetInfrastructure list : listFEthernetInfrastructure) {
                record_id = list.record_id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sRecordId = String.valueOf(record_id);
                sInventoryKey = inventory_key;
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice;
                sbBefore.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice + " |");

                newAacquiringPrice = (acquiring_price / euroExchangeRate);
                newAacquiringPrice = round(newAacquiringPrice, places);
                sNewAacquiringPrice = new DecimalFormat("#0.00").format(newAacquiringPrice);
                list.setAcquiring_price(newAacquiringPrice);
                // System.out.println("| record_id: " + sRecordId + " | sAcquiringPrice: " + sAcquiringPrice + " | sNewAacquiringPrice: " + sNewAacquiringPrice + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "record_id | " + "inventory_key | " + "acquiring_price";
            sbAfter.append(line).append("\n");

            for (FEthernetInfrastructure list : listFEthernetInfrastructure) {
                record_id = list.record_id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sRecordId = String.valueOf(record_id);
                sInventoryKey = inventory_key;
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice;
                sbAfter.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateDaeuReportsFEthernetInfrastructure(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }
    }

    public void updateDaeuReportsFEthernetInfrastructure(Connection conn, String host, String db, String table) {
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;

        Integer record_id = 0;
        String inventory_key = "";
        Double acquiring_price = 0.00d;

        String sRecordId = "";
        String sInventoryKey = "";
        String sAcquiringPrice = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE daeu_reports.F_Ethernet_infrastructure "
                    + "SET acquiring_price = ? "
                    + "WHERE record_id = ?";
            pstmt = conn.prepareStatement(query);

            for (FEthernetInfrastructure list : listFEthernetInfrastructure) {
                record_id = list.record_id;
                acquiring_price = list.acquiring_price;

                conn.setAutoCommit(false);
                pstmt.setDouble(1, acquiring_price);
                pstmt.setInt(2, record_id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- F_Ethernet_infrastructure -------------------------------------------------
    // ----- F_HardwareAsset -----------------------------------------------------------
    public void getCurrencyDaeuReportsFHardwareAsset(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer record_id = 0;
        String inventory_key = "";
        Double acquiring_price = 0.00d;

        String sRecordId = "";
        String sInventoryKey = "";
        String sAcquiringPrice = "";

        String line = "";
        FHardwareAsset dataFHardwareAsset = null;
        listFHardwareAsset = new ArrayList<>();
        BufferedWriter bw = null;

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        Double euroExchangeRate = 1.95583d;
        Integer places = 2;
        Double newAacquiringPrice = 0.00d;
        String sNewAacquiringPrice = "";

        sql = "SELECT record_id, inventory_key, acquiring_price "
                + "FROM daeu_reports.F_HardwareAsset "
                + "ORDER BY record_id ASC";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                record_id = resultSet.getInt("record_id");
                if (resultSet.getString("inventory_key") != null) {
                    inventory_key = resultSet.getString("inventory_key");
                } else {
                    inventory_key = "";
                }
                acquiring_price = resultSet.getDouble("acquiring_price");
                if (acquiring_price == null) {  // acquiring_price.isNaN() - Is Not-a-Number
                    acquiring_price = 0.00d;
                }

                dataFHardwareAsset = new FHardwareAsset();
                dataFHardwareAsset.record_id = record_id;
                dataFHardwareAsset.inventory_key = inventory_key;
                dataFHardwareAsset.acquiring_price = acquiring_price;
                listFHardwareAsset.add(dataFHardwareAsset);
            }

            sbBefore = new StringBuilder();
            line = "record_id | " + "inventory_key | " + "acquiring_price";
            sbBefore.append(line).append("\n");

            for (FHardwareAsset list : listFHardwareAsset) {
                record_id = list.record_id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sRecordId = String.valueOf(record_id);
                sInventoryKey = inventory_key;
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice;
                sbBefore.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice + " |");

                newAacquiringPrice = (acquiring_price / euroExchangeRate);
                newAacquiringPrice = round(newAacquiringPrice, places);
                sNewAacquiringPrice = new DecimalFormat("#0.00").format(newAacquiringPrice);
                list.setAcquiring_price(newAacquiringPrice);
                // System.out.println("| record_id: " + sRecordId + " | sAcquiringPrice: " + sAcquiringPrice + " | sNewAacquiringPrice: " + sNewAacquiringPrice + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "record_id | " + "inventory_key | " + "acquiring_price";
            sbAfter.append(line).append("\n");

            for (FHardwareAsset list : listFHardwareAsset) {
                record_id = list.record_id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sRecordId = String.valueOf(record_id);
                sInventoryKey = inventory_key;
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice;
                sbAfter.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateDaeuReportsFHardwareAsset(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }
    }

    public void updateDaeuReportsFHardwareAsset(Connection conn, String host, String db, String table) {
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;

        Integer record_id = 0;
        Double acquiring_price = 0.00d;

        String sRecordId = "";
        String sAcquiringPrice = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE daeu_reports.F_HardwareAsset "
                    + "SET acquiring_price = ? "
                    + "WHERE record_id = ?";
            pstmt = conn.prepareStatement(query);

            for (FHardwareAsset list : listFHardwareAsset) {
                record_id = list.record_id;
                acquiring_price = list.acquiring_price;

                conn.setAutoCommit(false);
                pstmt.setDouble(1, acquiring_price);
                pstmt.setInt(2, record_id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- F_HardwareAsset -----------------------------------------------------------
    // ----- F_SoftwareAsset -----------------------------------------------------------
    public void getCurrencyDaeuReportsFSoftwareAsset(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer record_id = 0;
        String inventory_key = "";
        Double acquiring_price = 0.00d;
        
        String sRecordId = "";
        String sInventoryKey = "";
        String sAcquiringPrice = "";

        String line = "";
        FSoftwareAsset dataFSoftwareAsset = null;
        listFSoftwareAsset = new ArrayList<>();
        BufferedWriter bw = null;

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        Double euroExchangeRate = 1.95583d;
        Integer places = 2;
        Double newAacquiringPrice = 0.00d;
        String sNewAacquiringPrice = "";

        sql = "SELECT record_id, inventory_key, acquiring_price "
                + "FROM daeu_reports.F_SoftwareAsset "
                + "ORDER BY record_id ASC";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                record_id = resultSet.getInt("record_id");
                if (resultSet.getString("inventory_key") != null) {
                    inventory_key = resultSet.getString("inventory_key");
                } else {
                    inventory_key = "";
                }
                acquiring_price = resultSet.getDouble("acquiring_price");
                if (acquiring_price == null) {  // acquiring_price.isNaN() - Is Not-a-Number
                    acquiring_price = 0.00d;
                }

                dataFSoftwareAsset = new FSoftwareAsset();
                dataFSoftwareAsset.record_id = record_id;
                dataFSoftwareAsset.inventory_key = inventory_key;
                dataFSoftwareAsset.acquiring_price = acquiring_price;
                listFSoftwareAsset.add(dataFSoftwareAsset);
            }

            sbBefore = new StringBuilder();
            line = "record_id | " + "inventory_key | " + "acquiring_price";
            sbBefore.append(line).append("\n");

            for (FSoftwareAsset list : listFSoftwareAsset) {
                record_id = list.record_id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sRecordId = String.valueOf(record_id);
                sInventoryKey = inventory_key;
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice;
                sbBefore.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice + " |");

                newAacquiringPrice = (acquiring_price / euroExchangeRate);
                newAacquiringPrice = round(newAacquiringPrice, places);
                sNewAacquiringPrice = new DecimalFormat("#0.00").format(newAacquiringPrice);
                list.setAcquiring_price(newAacquiringPrice);
                // System.out.println("| record_id: " + sRecordId + " | sAcquiringPrice: " + sAcquiringPrice + " | sNewAacquiringPrice: " + sNewAacquiringPrice + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "record_id | " + "inventory_key | " + "acquiring_price";
            sbAfter.append(line).append("\n");

            for (FSoftwareAsset list : listFSoftwareAsset) {
                record_id = list.record_id;
                inventory_key = list.inventory_key;
                acquiring_price = list.acquiring_price;

                sRecordId = String.valueOf(record_id);
                sInventoryKey = inventory_key;
                sAcquiringPrice = new DecimalFormat("#0.00").format(acquiring_price);
                sAcquiringPrice = sAcquiringPrice.replace(',', '.');

                line = sRecordId + " | " + sInventoryKey + " | " + sAcquiringPrice;
                sbAfter.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sInventoryKey + " |" + sAcquiringPrice + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateDaeuReportsFSoftwareAsset(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }

    }

    public void updateDaeuReportsFSoftwareAsset(Connection conn, String host, String db, String table) {
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;

        Integer record_id = 0;
        String inventory_key = "";
        Double acquiring_price = 0.00d;

        String sRecordId = "";
        String sInventoryKey = "";
        String sAcquiringPrice = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE daeu_reports.F_SoftwareAsset "
                    + "SET acquiring_price = ? "
                    + "WHERE record_id = ?";
            pstmt = conn.prepareStatement(query);

            for (FSoftwareAsset list : listFSoftwareAsset) {
                record_id = list.record_id;
                acquiring_price = list.acquiring_price;

                conn.setAutoCommit(false);
                pstmt.setDouble(1, acquiring_price);
                pstmt.setInt(2, record_id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- F_SoftwareAsset -----------------------------------------------------------
    // ----- F_Support_history ---------------------------------------------------------
    public void getCurrencyDaeuReportsFSupportHistory(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";
        String line = "";
        FSupportHistory dataFSupportHistory = null;
        listFSupportHistory = new ArrayList<>();
        BufferedWriter bw = null;

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer record_id = 0;
        Integer asset_id = 0;
        Double resource_cost = 0.00d;
        Double resource_cost_parts = 0.00d;

        String sRecordId = "";
        String sAssetId = "";
        String sResourceCost = "";
        String sResourceCostParts = "";

        Date parsedStartDate = null;
        Date parsedEndDate = null;
        Date parsedCreatedOn = null;
        Date parsedUpdatedOn = null;
        Double euroExchangeRate = 1.95583d;
        Integer places = 2;
        Double newResourceCost = 0.00d;
        Double newResourceCostParts = 0.00d;
        String sNewResourceCost = "";
        String sNewResourceCostParts = "";

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        sql = "SELECT record_id, asset_id, resource_cost, resource_cost_parts "
                + "FROM daeu_reports.F_Support_history "
                + "ORDER BY record_id ASC";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                record_id = resultSet.getInt("record_id");
                asset_id = resultSet.getInt("asset_id");
                if (asset_id == null) {
                    asset_id = 0;
                }
                resource_cost = resultSet.getDouble("resource_cost");
                if (resource_cost == null) {  // resource_cost.isNaN() - Is Not-a-Number
                    resource_cost = 0.00d;
                }
                resource_cost_parts = resultSet.getDouble("resource_cost_parts");
                if (resource_cost_parts == null) {  // resource_cost_parts.isNaN() - Is Not-a-Number
                    resource_cost_parts = 0.00d;
                }

                dataFSupportHistory = new FSupportHistory();
                dataFSupportHistory.record_id = record_id;
                dataFSupportHistory.asset_id = asset_id;
                dataFSupportHistory.resource_cost = resource_cost;
                dataFSupportHistory.resource_cost_parts = resource_cost_parts;
                listFSupportHistory.add(dataFSupportHistory);

            }

            sbBefore = new StringBuilder();
            line = "record_id | " + "asset_id | " + "resource_cost | " + "resource_cost_parts";
            sbBefore.append(line).append("\n");

            for (FSupportHistory list : listFSupportHistory) {
                record_id = list.record_id;
                asset_id = list.asset_id;
                resource_cost = list.resource_cost;
                resource_cost_parts = list.resource_cost_parts;

                sRecordId = String.valueOf(record_id);
                sAssetId = String.valueOf(asset_id);
                sResourceCost = new DecimalFormat("#0.00").format(resource_cost);
                sResourceCostParts = new DecimalFormat("#0.00").format(resource_cost_parts);
                sResourceCost = sResourceCost.replace(',', '.');
                sResourceCostParts = sResourceCostParts.replace(',', '.');

                line = sRecordId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts;
                sbBefore.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts + " |");

                newResourceCost = (resource_cost / euroExchangeRate);
                newResourceCost = round(newResourceCost, places);
                sNewResourceCost = new DecimalFormat("#0.00").format(newResourceCost);
                list.setResource_cost(newResourceCost);

                newResourceCostParts = (resource_cost_parts / euroExchangeRate);
                newResourceCostParts = round(newResourceCostParts, places);
                sNewResourceCostParts = new DecimalFormat("#0.00").format(newResourceCostParts);
                list.setResource_cost_parts(newResourceCostParts);

                // System.out.println("| record_id: " + sRecordId + " | sResourceCost: " + sResourceCost + " | sNewResourceCost: " + sNewResourceCost      + " | sResourceCostParts: " + sResourceCostParts + " | sNewResourceCostParts: " + sNewResourceCostParts + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "record_id | " + "asset_id | " + "resource_cost | " + "resource_cost_parts";
            sbAfter.append(line).append("\n");

            for (FSupportHistory list : listFSupportHistory) {
                record_id = list.record_id;
                asset_id = list.asset_id;
                resource_cost = list.resource_cost;
                resource_cost_parts = list.resource_cost_parts;

                sRecordId = String.valueOf(record_id);
                sAssetId = String.valueOf(asset_id);
                sResourceCost = new DecimalFormat("#0.00").format(resource_cost);
                sResourceCostParts = new DecimalFormat("#0.00").format(resource_cost_parts);
                sResourceCost = sResourceCost.replace(',', '.');
                sResourceCostParts = sResourceCostParts.replace(',', '.');

                line = sRecordId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts;
                sbAfter.append(line).append("\n");
                // System.out.println("| " + sRecordId + " | " + sAssetId + " | " + sResourceCost + " | " + sResourceCostParts + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateDaeuReportsFSupportHistory(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }
    }

    public void updateDaeuReportsFSupportHistory(Connection conn, String host, String db, String table) {
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;

        Integer record_id = 0;
        Integer asset_id = 0;
        Double resource_cost = 0.00d;
        Double resource_cost_parts = 0.00d;

        String sRecordId = "";
        String sAssetId = "";
        String sResourceCost = "";
        String sResourceCostParts = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE daeu_reports.F_Support_history "
                    + "SET resource_cost = ?, resource_cost_parts = ? "
                    + "WHERE record_id = ?";
            pstmt = conn.prepareStatement(query);

            for (FSupportHistory list : listFSupportHistory) {
                record_id = list.record_id;
                resource_cost = list.resource_cost;
                resource_cost_parts = list.resource_cost_parts;

                conn.setAutoCommit(false);
                pstmt.setDouble(1, resource_cost);
                pstmt.setDouble(2, resource_cost_parts);
                pstmt.setInt(3, record_id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- F_Support_history ---------------------------------------------------------
    // ----- F_Yearly_plan -------------------------------------------------------------
    public void getCurrencyDaeuReportsFYearlyPlan(Connection conn, String host, String db, String table) {
        Statement stmt = null;
        String sql = "";
        ResultSet resultSet = null;
        String msg = "";

        Date startDate = null;
        Date endDate = null;
        Long timeDifference = 0L;
        Long secondsDifference = 0L;
        Long minutesDifferense = 0L;
        Long hoursDifference = 0L;

        Integer record_id = 0;
        String month = "";
        Integer year = 0;
        Long planned_resources = 0L;

        String sRecordId = "";
        String sMonth = "";
        String sYear = "";
        String sPlannedResources = "";

        Date parsedCreatedOn = null;
        Date parsedUpdatedOn = null;
        Double dblNewPlannedResources = 0.00d;
        Long newPlannedResources = 0L;
        String sNewPlannedResources = "";
        Double euroExchangeRate = 1.95583d;
        Integer places = 0;

        String line = "";
        FYearlyPlan dataFYearlyPlan = null;
        listFYearlyPlan = new ArrayList<>();
        BufferedWriter bw = null;

        String txtHost = "";
        String txtDb = "";
        String txtTable = "";
        switch (host) {
            case "Local Host":
                txtHost = "Local";
                break;
            case "Test Host":
                txtHost = "Test";
                break;
            case "Prod Host":
                txtHost = "Prod";
                break;
            default:
                break;
        }
        switch (db) {
            case "itop":
                txtDb = "Itop";
                break;
            case "daeu_reports":
                txtDb = "DaeuReports";
                break;
            default:
                break;
        }
        switch (table) {
            case "Asset":
                txtTable = "Asset";
                break;
            case "Support_history":
                txtTable = "SupportHistory";
                break;
            case "Yearly_plan":
                txtTable = "YearlyPlan";
                break;
            case "F_Ethernet_infrastructure":
                txtTable = "FEthernetInfrastructure";
                break;
            case "F_HardwareAsset":
                txtTable = "FHardwareAsset";
                break;
            case "F_SoftwareAsset":
                txtTable = "FSoftwareAsset";
                break;
            case "F_Support_history":
                txtTable = "FSupportHistory";
                break;
            case "F_Yearly_plan":
                txtTable = "FYearlyPlan";
                break;
            default:
                break;
        }
        String txtFile_Before = "before" + txtHost + txtDb + txtTable;
        String filePath_Before = "./output/" + txtFile_Before + ".txt";
        File file_Before = new File(filePath_Before);
        String txtFile_After = "after" + txtHost + txtDb + txtTable;
        String filePath_After = "./output/" + txtFile_After + ".txt";
        File file_After = new File(filePath_After);

        sql = "SELECT record_id, month, year, planned_resources "
                + "FROM daeu_reports.F_Yearly_plan "
                + "ORDER BY record_id ASC";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startDate = generateCurrentDate();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                record_id = resultSet.getInt("record_id");
                if (resultSet.getString("month") != null) {
                    month = resultSet.getString("month");
                } else {
                    month = "";
                }
                year = resultSet.getInt("year");
                if (year == null) {
                    year = 0;
                }
                planned_resources = resultSet.getLong("planned_resources");
                if (planned_resources == null) {
                    planned_resources = 0L;
                }
                
                dataFYearlyPlan = new FYearlyPlan();
                dataFYearlyPlan.record_id = record_id;
                dataFYearlyPlan.month = month;
                dataFYearlyPlan.year = year;
                dataFYearlyPlan.planned_resources = planned_resources;
                listFYearlyPlan.add(dataFYearlyPlan);
            }

            sbBefore = new StringBuilder();
            line = "record_id | " + "month | " + "year | " + "planned_resources";
            sbBefore.append(line).append("\n");

            for (FYearlyPlan list : listFYearlyPlan) {
                record_id = list.record_id;
                month = list.month;
                year = list.year;
                planned_resources = list.planned_resources;

                sRecordId = String.valueOf(record_id);
                sMonth = month;
                sYear = String.valueOf(year);
                sPlannedResources = String.valueOf(planned_resources);

                line = sRecordId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources;
                sbBefore.append(line).append("\n");
                // System.out.println("BEFORE: | " + sRecordId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources+ " |");

                dblNewPlannedResources = ((double) planned_resources / euroExchangeRate);
                dblNewPlannedResources = round(dblNewPlannedResources, places);
                newPlannedResources = dblNewPlannedResources.longValue();
                sNewPlannedResources = String.valueOf(newPlannedResources);

                list.setPlanned_resources(newPlannedResources);
                // System.out.println("NEW SET: | record_id: " + sRecordId + " | planned_resources: " + sPlannedResources + " | newPlannedResources: " + sNewPlannedResources + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_Before));
                bw.write(sbBefore.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_Before + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_Before + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_Before + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_Before + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            // ................................................................
            
            sbAfter = new StringBuilder();
            line = "record_id | " + "month | " + "year | " + "planned_resources";
            sbAfter.append(line).append("\n");

            for (FYearlyPlan list : listFYearlyPlan) {
                record_id = list.record_id;
                month = list.month;
                year = list.year;
                planned_resources = list.planned_resources;

                sRecordId = String.valueOf(record_id);
                sMonth = month;
                sYear = String.valueOf(year);
                sPlannedResources = String.valueOf(planned_resources);

                line = sRecordId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources;
                sbAfter.append(line).append("\n");
                // System.out.println("AFTER: | " + sRecordId + " | " + sMonth + " | " + sYear + " | " + sPlannedResources + " |");
            }

            try {
                bw = new BufferedWriter(new FileWriter(file_After));
                bw.write(sbAfter.toString());
                bw.flush();

                taText = " • Успешно създаден файл: " + txtFile_After + ".txt!";
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно създаден файл:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                log.info("Успешно създаден файл: " + txtFile_After + ".txt");
            } catch (IOException e) {
                this.setCursor(Cursor.getDefaultCursor());
                taText = " • Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error generate TXT file:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + txtFile_After + ".txt!" + "</FONT></i></html>";
                setStatusLabel(slText);

                System.out.println("Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
                log.log(Level.WARNING, "Error generate TXT file: " + txtFile_After + ".txt || " + e.getMessage());
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            updateDaeuReportsFYearlyPlan(conn, host, db, table);

            endDate = generateCurrentDate();
            timeDifference = (endDate.getTime() - startDate.getTime());
            secondsDifference = ((timeDifference / 1000) % 60);
            minutesDifferense = ((timeDifference / (1000 * 60)) % 60);
            hoursDifference = ((timeDifference / (1000 * 60 * 60)) % 24);

            taText = " • Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "";
            setDataGeneralStatisticsTextArea(taText);
            taText = "==================================================================================================================";
            setDataGeneralStatisticsTextArea(taText);
            
            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            setStatusLabel(slText);
            
            msg = "<html>&nbsp;&nbsp;<b><FONT COLOR=GREEN> • Успешно превалутиране на:&nbsp;&nbsp;</FONT></b><i><FONT COLOR=BLUE>" + host + " | DB: " + db + " | Table: " + table + "" + "</FONT></i></html>";
            JOptionPane.showMessageDialog(f, msg);

            log.info("Успешно превалутиране на: " + host + " | DB: " + db + " | Table: " + table + "");
            this.setCursor(Cursor.getDefaultCursor());
        } catch (SQLException se) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + se.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + se.getMessage());
            log.log(Level.WARNING, "Error: " + se.getMessage());
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            taText = " • Error: " + e.getMessage();
            setDataGeneralStatisticsTextArea(taText);
            taText = "------------------------------------------------------------------------------------------------------------------";
            setDataGeneralStatisticsTextArea(taText);

            slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
            setStatusLabel(slText);

            System.out.println("Error: " + e.getMessage());
            log.log(Level.WARNING, "Error: " + e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                taText = " • Error: " + se.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error: " + se.getMessage());
                log.log(Level.WARNING, "Error: " + se.getMessage());
            }
        }
    }

    public void updateDaeuReportsFYearlyPlan(Connection conn, String host, String db, String table) {
        PreparedStatement pstmt = null;
        String query = "";
        Integer resultUpdate = 0;

        Integer record_id = 0;
        String month = "";
        Integer year = 0;
        Long planned_resources = 0L;

        String sRecordId = "";
        String sMonth = "";
        String sYear = "";
        String sPlannedResources = "";

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            query = "UPDATE daeu_reports.F_Yearly_plan "
                    + "SET planned_resources = ? "
                    + "WHERE record_id = ?";
            pstmt = conn.prepareStatement(query);
 
            for (FYearlyPlan list : listFYearlyPlan) {
                record_id = list.record_id;
                planned_resources = list.planned_resources;

                conn.setAutoCommit(false);
                pstmt.setLong(1, planned_resources);
                pstmt.setInt(2, record_id);
                resultUpdate = pstmt.executeUpdate();
                if (resultUpdate > 0) {
                    conn.commit();
                } else {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        this.setCursor(Cursor.getDefaultCursor());
                        taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                        setDataGeneralStatisticsTextArea(taText);
                        taText = "------------------------------------------------------------------------------------------------------------------";
                        setDataGeneralStatisticsTextArea(taText);

                        slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                        setStatusLabel(slText);

                        System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                        log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                    }
                    this.setCursor(Cursor.getDefaultCursor());
                    taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + "!";
                    setDataGeneralStatisticsTextArea(taText);
                    taText = "------------------------------------------------------------------------------------------------------------------";
                    setDataGeneralStatisticsTextArea(taText);

                    slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                    setStatusLabel(slText);

                    System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");
                    log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + "!");

                    return;
                }
            }
        } catch (Exception e) {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                taText = " • Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Rollback!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
                log.log(Level.WARNING, "Error Rollback: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage());
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
            try {
                pstmt.close();
            } catch (SQLException ex) {
                taText = " • Error Update: " + host + " | DB: " + db + " | Table: " + table + " || " + ex.getMessage();
                setDataGeneralStatisticsTextArea(taText);
                taText = "------------------------------------------------------------------------------------------------------------------";
                setDataGeneralStatisticsTextArea(taText);

                slText = "<html>&nbsp;&nbsp;<b><FONT COLOR=RED> • Error Update!</FONT></b></html>";
                setStatusLabel(slText);

                System.out.println("Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
                log.log(Level.WARNING, "Error Update: " + host + " | DB: " + db + " | Table: " + table + " " + ex.getMessage());
            }
        }
    }
    // ----- F_Yearly_plan -------------------------------------------------------------
    // ----- TextArea - StatusLabel ----------------------------------------------------
    public void setDataGeneralStatisticsTextArea(String msg) {
        String newline = "\n";
        gsTextArea.append(msg + newline);
    }

    public void removeDataGeneralStatisticsTextArea() {
        gsTextArea.removeAll();
    }

    public void setStatusLabel(String msg) {
        statusLabel.setText(msg);
    }

    public void clearStatusLabel() {
        statusLabel.setText("");
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }
    // ----- TextArea - StatusLabel ----------------------------------------------------

    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public class Asset {
        public Integer id;
        public String inventory_key;
        public Double acquiring_price;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getInventory_key() {
            return inventory_key;
        }

        public void setInventory_key(String inventory_key) {
            this.inventory_key = inventory_key;
        }

        public Double getAcquiring_price() {
            return acquiring_price;
        }

        public void setAcquiring_price(Double acquiring_price) {
            this.acquiring_price = acquiring_price;
        }
    }

    public class SupportHistory {
        public Integer id;
        public Integer asset_id;
        public Double resource_cost;
        public Double resource_cost_parts;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getAsset_id() {
            return asset_id;
        }

        public void setAsset_id(Integer asset_id) {
            this.asset_id = asset_id;
        }

        public Double getResource_cost() {
            return resource_cost;
        }

        public void setResource_cost(Double resource_cost) {
            this.resource_cost = resource_cost;
        }

        public Double getResource_cost_parts() {
            return resource_cost_parts;
        }

        public void setResource_cost_parts(Double resource_cost_parts) {
            this.resource_cost_parts = resource_cost_parts;
        }
    }

    public class YearlyPlan {
        public Integer id;
        public String month;
        public Integer year;
        public Long planned_resources;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Long getPlanned_resources() {
            return planned_resources;
        }

        public void setPlanned_resources(Long planned_resources) {
            this.planned_resources = planned_resources;
        }
    }

    public class FEthernetInfrastructure {
        public Integer record_id;
        public String inventory_key;
        public Double acquiring_price;

        public Integer getRecord_id() {
            return record_id;
        }

        public void setRecord_id(Integer record_id) {
            this.record_id = record_id;
        }

        public String getInventory_key() {
            return inventory_key;
        }

        public void setInventory_key(String inventory_key) {
            this.inventory_key = inventory_key;
        }

        public Double getAcquiring_price() {
            return acquiring_price;
        }

        public void setAcquiring_price(Double acquiring_price) {
            this.acquiring_price = acquiring_price;
        }
    }

    public class FHardwareAsset {
        public Integer record_id;
        public String inventory_key;
        public Double acquiring_price;

        public Integer getRecord_id() {
            return record_id;
        }

        public void setRecord_id(Integer record_id) {
            this.record_id = record_id;
        }

        public String getInventory_key() {
            return inventory_key;
        }

        public void setInventory_key(String inventory_key) {
            this.inventory_key = inventory_key;
        }

        public Double getAcquiring_price() {
            return acquiring_price;
        }

        public void setAcquiring_price(Double acquiring_price) {
            this.acquiring_price = acquiring_price;
        }
    }

    public class FSoftwareAsset {
        public Integer record_id;
        public String inventory_key;
        public Double acquiring_price;

        public Integer getRecord_id() {
            return record_id;
        }

        public void setRecord_id(Integer record_id) {
            this.record_id = record_id;
        }

        public String getInventory_key() {
            return inventory_key;
        }

        public void setInventory_key(String inventory_key) {
            this.inventory_key = inventory_key;
        }

        public Double getAcquiring_price() {
            return acquiring_price;
        }

        public void setAcquiring_price(Double acquiring_price) {
            this.acquiring_price = acquiring_price;
        }
    }

    public class FSupportHistory {
        public Integer record_id;
        public Integer asset_id;
        public Double resource_cost;
        public Double resource_cost_parts;

        public Integer getRecord_id() {
            return record_id;
        }

        public void setRecord_id(Integer record_id) {
            this.record_id = record_id;
        }
        public Integer getAsset_id() {
            return asset_id;
        }

        public void setAsset_id(Integer asset_id) {
            this.asset_id = asset_id;
        }

        public Double getResource_cost() {
            return resource_cost;
        }

        public void setResource_cost(Double resource_cost) {
            this.resource_cost = resource_cost;
        }

        public Double getResource_cost_parts() {
            return resource_cost_parts;
        }

        public void setResource_cost_parts(Double resource_cost_parts) {
            this.resource_cost_parts = resource_cost_parts;
        }
    }

    public class FYearlyPlan {
        public Integer record_id;
        public String month;
        public Integer year;
        public Long planned_resources;

        public Integer getRecord_id() {
            return record_id;
        }

        public void setRecord_id(Integer record_id) {
            this.record_id = record_id;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Long getPlanned_resources() {
            return planned_resources;
        }

        public void setPlanned_resources(Long planned_resources) {
            this.planned_resources = planned_resources;
        }
    }
    
    public Date generateCurrentDate() {
        Date currentDate = null;
        String crntDate = "";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        crntDate = dtf.format(now);
        String get_error = "";
        try {
            currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(crntDate);
        } catch (ParseException e) {
            get_error = e.getMessage();
            log.log(Level.WARNING, "Date ParseException: " + get_error);
        }
        return currentDate;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu About;
    private javax.swing.JScrollPane generalSebraScrollPane;
    private javax.swing.JTextArea gsTextArea;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem local_DaeuReports_ConnectionChecking_;
    private javax.swing.JMenuItem local_DaeuReports_FEthernetInfrastructure_;
    private javax.swing.JMenuItem local_DaeuReports_FHardwareAsset_;
    private javax.swing.JMenuItem local_DaeuReports_FSoftwareAsset_;
    private javax.swing.JMenuItem local_DaeuReports_FSupportHistory_;
    private javax.swing.JMenuItem local_DaeuReports_FYearlyPlan_;
    private javax.swing.JMenuItem local_Itop_Asset_;
    private javax.swing.JMenuItem local_Itop_ConnectionChecking_;
    private javax.swing.JMenuItem local_Itop_SupportHistory_;
    private javax.swing.JMenuItem local_Itop_YearlyPlan_;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenu menuAbout;
    private javax.swing.JMenu menuChoiceFile;
    private javax.swing.JMenu menuHost_Local;
    private javax.swing.JMenu menuHost_Prod;
    private javax.swing.JMenu menuHost_Test;
    private javax.swing.JMenu menuLocal_DaeuReports;
    private javax.swing.JMenu menuLocal_Itop;
    private javax.swing.JMenu menuProd_DaeuReports;
    private javax.swing.JMenu menuProd_Itop;
    private javax.swing.JMenu menuTest_DaeuReports;
    private javax.swing.JMenu menuTest_Itop;
    private javax.swing.JMenuItem prod_DaeuReports_ConnectionChecking_;
    private javax.swing.JMenuItem prod_DaeuReports_FEthernetInfrastructure_;
    private javax.swing.JMenuItem prod_DaeuReports_FHardwareAsset_;
    private javax.swing.JMenuItem prod_DaeuReports_FSoftwareAsset_;
    private javax.swing.JMenuItem prod_DaeuReports_FSupportHistory_;
    private javax.swing.JMenuItem prod_DaeuReports_FYearlyPlan_;
    private javax.swing.JMenuItem prod_Itop_Asset_;
    private javax.swing.JMenuItem prod_Itop_ConnectionChecking_;
    private javax.swing.JMenuItem prod_Itop_SupportHistory_;
    private javax.swing.JMenuItem prod_Itop_YearlyPlan_;
    private javax.swing.JMenuBar sebraMenuBar;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem test_DaeuReports_ConnectionChecking_;
    private javax.swing.JMenuItem test_DaeuReports_FEthernetInfrastructure_;
    private javax.swing.JMenuItem test_DaeuReports_FHardwareAsset_;
    private javax.swing.JMenuItem test_DaeuReports_FSoftwareAsset_;
    private javax.swing.JMenuItem test_DaeuReports_FSupportHistory_;
    private javax.swing.JMenuItem test_DaeuReports_FYearlyPlan_;
    private javax.swing.JMenuItem test_Itop_Asset_;
    private javax.swing.JMenuItem test_Itop_ConnectionChecking_;
    private javax.swing.JMenuItem test_Itop_SupportHistory_;
    private javax.swing.JMenuItem test_Itop_YearlyPlan_;
    // End of variables declaration//GEN-END:variables
}
